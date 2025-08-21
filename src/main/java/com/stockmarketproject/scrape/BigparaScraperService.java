package com.stockmarketproject.scrape;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class BigparaScraperService {

    @Value("${app.scrape.url:https://bigpara.hurriyet.com.tr/borsa/canli-borsa/}")
    private String baseUrl;

    @Value("${app.scrape.userAgent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36}")
    private String userAgent;

    @Value("${app.scrape.timeoutMs:15000}")
    private int timeoutMs;

    private Connection conn(String url) {
        return Jsoup.connect(url)
                .userAgent(userAgent)
                .referrer("https://www.google.com/")
                .timeout(timeoutMs)
                .ignoreContentType(true)
                .followRedirects(true);
    }

    public List<ScrapedStock> fetchAll() throws Exception {
        Map<String, ScrapedStock> bag = new LinkedHashMap<>();

        crawlPage(baseUrl, bag);
        if (bag.size() < 30) {
            String letters = "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZQWX0123456789";
            for (char ch : letters.toCharArray()) {
                String url = baseUrl;
                String[] qs = {"firstLetter", "letters", "harf"};
                for (String q : qs) {
                    String u = url + (url.contains("?") ? "&" : "?") + q + "=" +
                            URLEncoder.encode(String.valueOf(ch), StandardCharsets.UTF_8);
                    crawlPage(u, bag);
                }
                try { Thread.sleep(60); } catch (InterruptedException ignored) {}
            }
        }

        log.info("Bigpara: collected {} unique stocks.", bag.size());
        return new ArrayList<>(bag.values());
    }

    private void crawlPage(String url, Map<String, ScrapedStock> sink) {
        try {
            Document doc = conn(url).get();
            for (Element table : doc.select("table")) {
                List<String> headers = new ArrayList<>();
                for (Element th : table.select("thead th")) {
                    headers.add(th.text().trim().toLowerCase(Locale.ROOT));
                }
                if (headers.isEmpty()) continue;

                int idxSymbol = guessIndex(headers, List.of("sembol", "kod", "hisse", "hisse kodu", "hisse adı", "hisse adı / kodu"));
                int idxName   = guessIndex(headers, List.of("ad", "şirket", "hisse adı", "ad/ünvan"));
                int idxLast   = guessIndex(headers, List.of("son", "fiyat", "kapanış", "son fiyat"));

                if (idxSymbol < 0 || idxLast < 0) continue;

                for (Element tr : table.select("tbody tr")) {
                    Elements tds = tr.select("td");
                    if (tds.size() <= Math.max(idxSymbol, idxLast)) continue;

                    String symbol = textOf(tds, idxSymbol).toUpperCase(Locale.ROOT);
                    String name   = idxName >= 0 ? textOf(tds, idxName) : "";
                    String priceS = textOf(tds, idxLast).replace(",", ".")
                            .replaceAll("[^0-9.]", "");

                    if (symbol.isBlank() || priceS.isBlank()) continue;

                    try {
                        BigDecimal price = new BigDecimal(priceS);
                        sink.put(symbol, new ScrapedStock(symbol, name, price));
                    } catch (NumberFormatException ignore) {  }
                }
            }
        } catch (Exception ex) {
            log.debug("Bigpara crawl failed {} -> {}", url, ex.toString());
        }
    }

    private static String textOf(Elements tds, int index) {
        if (index < 0 || index >= tds.size()) return "";
        Element td = tds.get(index);
        Element link = td.selectFirst("a");
        return (link != null ? link.text() : td.text()).trim();
    }

    private static int guessIndex(List<String> headers, List<String> candidates) {
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i);
            for (String c : candidates) {
                if (h.contains(c)) return i;
            }
        }
        return -1;
    }
}
