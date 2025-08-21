package com.stockmarketproject.scrape;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BigparaScraperService {

    @Value("${app.scrape.url:https://bigpara.hurriyet.com.tr/borsa/canli-borsa/}")
    private String baseUrl;

    @Value("${app.scrape.pages:25}")
    private int maxPages;

    @Value("${app.scrape.userAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36}")
    private String userAgent;

    @Value("${app.scrape.timeoutMs:15000}")
    private int timeoutMs;

    private OkHttpClient http;
    private final ObjectMapper om = new ObjectMapper();

    private static final DecimalFormat TR_DEC = new DecimalFormat();
    static {
        DecimalFormatSymbols sy = new DecimalFormatSymbols(new Locale("tr","TR"));
        sy.setDecimalSeparator(',');
        sy.setGroupingSeparator('.');
        TR_DEC.setDecimalFormatSymbols(sy);
        TR_DEC.setParseBigDecimal(true);
    }

    public List<ScrapedStock> fetchAll() throws IOException {
        ensureClient();

        Map<String, ScrapedStock> acc = new LinkedHashMap<>();

        int emptyInARow = 0;
        for (int p = 1; p <= Math.max(1, maxPages); p++) {
            String url = (p == 1) ? baseUrl : baseUrl + "?sayfa=" + p;

            String html = get(url);
            if (html == null || html.isBlank()) {
                log.warn("Bigpara: page={} boş/ulaşılamadı.", p);
                emptyInARow++;
                if (emptyInARow >= 3) break;
                continue;
            }

            int before = acc.size();

            parseTable(html).forEach(s -> acc.putIfAbsent(s.symbol(), s));
            log.info("Bigpara HTML page={} (tablo) -> acc={}", p, acc.size());

            parseEmbeddedJson(html).forEach(s -> acc.putIfAbsent(s.symbol(), s));
            log.info("Bigpara HTML page={} (json)  -> acc={}", p, acc.size());

            if (acc.size() == before) {
                parseLooseRegex(html).forEach(s -> acc.putIfAbsent(s.symbol(), s));
                log.info("Bigpara HTML page={} (regex) -> acc={}", p, acc.size());
            }

            emptyInARow = (acc.size() == before) ? (emptyInARow + 1) : 0;
            if (emptyInARow >= 3) break;
        }

        if (acc.isEmpty()) {
            log.warn("Bigpara: HTML’den hiç sonuç alınamadı. CSS seçiciler veya sayfa yapısı değişmiş olabilir.");
        } else {
            log.info("Bigpara: birleşik sonuç {} kayıt.", acc.size());
        }
        return new ArrayList<>(acc.values());
    }

    private void ensureClient() {
        if (http == null) {
            http = new OkHttpClient.Builder()
                    .callTimeout(Duration.ofMillis(timeoutMs))
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .readTimeout(Duration.ofMillis(timeoutMs))
                    .build();
        }
    }

    private String get(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8")
                .header("Referer", "https://bigpara.hurriyet.com.tr/")
                .get()
                .build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful() || res.body() == null) return null;
            return res.body().string();
        }
    }

    private List<ScrapedStock> parseTable(String html) {
        List<ScrapedStock> out = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(html);

            Element table = null;
            for (Element t : doc.select("table")) {
                String thText = t.select("thead th").text().toLowerCase(Locale.ROOT);
                if (thText.contains("kod") || thText.contains("sembol") || thText.contains("hisse")) {
                    table = t; break;
                }
            }
            if (table == null) return out;

            Map<String,Integer> idx = headerIndex(table.select("thead th"));
            int iKod  = idx.getOrDefault("kod",  idx.getOrDefault("sembol", 0));
            int iHisse= idx.getOrDefault("hisse", idx.getOrDefault("adı", 1));
            int iSon  = idx.getOrDefault("son",  idx.getOrDefault("son fiyat", 2));

            for (Element tr : table.select("tbody tr")) {
                Elements tds = tr.select("td");
                int minSize = Math.max(iSon, Math.max(iKod, iHisse)) + 1;
                if (tds.size() < minSize) continue;

                String symbol = pickText(tds.get(iKod));
                if (symbol == null || symbol.length() < 3) continue;

                String name = pickText(tds.get(iHisse));
                BigDecimal last = parsePrice(pickText(tds.get(iSon)));

                out.add(new ScrapedStock(symbol, name, last));
            }
        } catch (Exception ex) {
            log.debug("parseTable err: {}", ex.toString());
        }
        return out;
    }

    private Map<String,Integer> headerIndex(Elements ths) {
        Map<String,Integer> m = new HashMap<>();
        for (int i=0;i<ths.size();i++) {
            String k = ths.get(i).text().trim().toLowerCase(Locale.ROOT);
            if (k.contains("kod")) m.put("kod", i);
            if (k.contains("sembol")) m.put("sembol", i);
            if (k.contains("hisse")) m.put("hisse", i);
            if (k.contains("adı") || k.contains("adi")) m.put("adı", i);
            if (k.contains("son fiyat")) m.put("son fiyat", i);
            else if (k.contains("son")) m.put("son", i);
        }
        return m;
    }

    private String pickText(Element el) {
        if (el == null) return null;
        Element a = el.selectFirst("a");
        String s = (a != null ? a.text() : el.text());
        return (s != null) ? s.trim().replace('\u00A0',' ') : null;
    }

    private BigDecimal parsePrice(String s) {
        if (s == null) return null;
        try {
            s = s.replaceAll("[^0-9,\\.]", "");
            if (s.isEmpty()) return null;
            return (BigDecimal) TR_DEC.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private List<ScrapedStock> parseEmbeddedJson(String html) {
        List<ScrapedStock> out = new ArrayList<>();
        try {
            Pattern p = Pattern.compile("window\\.__NUXT__\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
            Matcher m = p.matcher(html);
            if (!m.find()) {
                p = Pattern.compile("id=\"__NEXT_DATA__\"[^>]*>\\s*(\\{.*?\\})\\s*<", Pattern.DOTALL);
                m = p.matcher(html);
            }
            if (!m.find()) return out;

            String json = m.group(1);
            JsonNode root = om.readTree(json);
            List<JsonNode> nodes = new ArrayList<>();
            walk(root, nodes);

            for (JsonNode n : nodes) {
                String kod = pick(n, "kod", "sembol", "symbol", "code");
                String ad  = pick(n, "hisse", "ad", "name", "title");
                String son = pick(n, "son", "last", "price", "lastPrice");

                if (kod != null && son != null) {
                    out.add(new ScrapedStock(kod, ad, parsePrice(son)));
                }
            }
        } catch (Exception ex) {
            log.debug("parseEmbeddedJson err: {}", ex.toString());
        }
        return dedup(out);
    }

    private void walk(JsonNode node, List<JsonNode> out) {
        if (node == null) return;
        if (node.isObject()) {
            out.add(node);
            node.fields().forEachRemaining(e -> walk(e.getValue(), out));
        } else if (node.isArray()) {
            node.forEach(n -> walk(n, out));
        }
    }

    private String pick(JsonNode n, String... keys) {
        for (String k : keys) if (n.has(k)) return asText(n.get(k));
        return null;
    }

    private String asText(JsonNode n) {
        if (n == null) return null;
        if (n.isTextual() || n.isNumber()) return n.asText();
        return null;
    }

    private List<ScrapedStock> parseLooseRegex(String html) {
        List<ScrapedStock> out = new ArrayList<>();
        try {
            Pattern row = Pattern.compile(
                    "\"kod\"\\s*:\\s*\"([A-Z0-9]{3,6})\".*?\"(?:son|last|price|lastPrice)\"\\s*:\\s*\"?([0-9\\.,]+)\"?",
                    Pattern.DOTALL);
            Matcher m = row.matcher(html);
            while (m.find()) {
                String kod = m.group(1);
                String son = m.group(2);
                out.add(new ScrapedStock(kod, null, parsePrice(son)));
            }
        } catch (Exception e) {
            log.debug("parseLooseRegex err: {}", e.toString());
        }
        return dedup(out);
    }

    private List<ScrapedStock> dedup(List<ScrapedStock> in) {
        Map<String,ScrapedStock> m = new LinkedHashMap<>();
        for (ScrapedStock s : in) {
            if (s.symbol() == null) continue;
            m.putIfAbsent(s.symbol(), s);
        }
        return new ArrayList<>(m.values());
    }
}
