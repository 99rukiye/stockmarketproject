package com.stockmarketproject.scrape;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BigparaScraperService {

    private static final String URL = "https://bigpara.hurriyet.com.tr/borsa/canli-borsa/";
    private static BigDecimal round2(BigDecimal bd) { return bd.setScale(2, RoundingMode.HALF_UP); }

    public List<ScrapedStock> fetchAll() {
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/123.0 Safari/537.36")
                    .timeout(15_000)
                    .get();

            List<ScrapedStock> out = parseByTable(doc, "#liveStocksTable table");
            if (!out.isEmpty()) return out;

            out = parseByTable(doc, "table");
            if (!out.isEmpty()) return out;

            log.warn("BigparaScraper: No rows captured from {}", URL);
            return List.of();

        } catch (Exception e) {
            log.warn("BigparaScraper: fetch error: {}", e.toString());
            return List.of();
        }
    }

    private List<ScrapedStock> parseByTable(Document doc, String tableCss) {
        List<ScrapedStock> list = new ArrayList<>();
        Element table = doc.selectFirst(tableCss);
        if (table == null) return list;

        int priceIdx = findPriceColumnIndex(table.select("thead th"));
        int symbolIdx = findSymbolColumnIndex(table.select("thead th"));

        Elements rows = table.select("tbody tr");
        for (Element tr : rows) {
            Elements tds = tr.select("td");
            if (tds.isEmpty()) continue;

            String symbol = readSymbol(tds, symbolIdx);
            String name   = readName(tds, symbolIdx);
            BigDecimal price = readPrice(tds, priceIdx);

            if (symbol != null && price != null) {
                list.add(new ScrapedStock(symbol, name, round2(price)));
            }
        }
        return list;
    }

    private static int findPriceColumnIndex(Elements ths) {
        for (int i = 0; i < ths.size(); i++) {
            String t = ths.get(i).text().toLowerCase();
            if (t.contains("son") || t.contains("fiyat")) return i;
        }
        return Math.max(1, Math.min(2, ths.size()-1));
    }
    private static int findSymbolColumnIndex(Elements ths) {
        for (int i = 0; i < ths.size(); i++) {
            String t = ths.get(i).text().toLowerCase();
            if (t.contains("sembol") || t.contains("kod")) return i;
        }
        return 0;
    }

    @Nullable
    private static String readSymbol(Elements tds, int symbolIdx) {
        String txt = safeTd(tds, symbolIdx);
        if (txt == null) return null;
        txt = txt.trim().replaceAll("[^A-ZÇĞİÖŞÜ0-9]", "");
        if (txt.isBlank()) return null;
        return txt;
    }

    private static String readName(Elements tds, int symbolIdx) {
        String name = safeTd(tds, Math.min(symbolIdx + 1, tds.size()-1));
        return name == null ? "" : name.trim();
    }

    @Nullable
    private static BigDecimal readPrice(Elements tds, int priceIdx) {
        String raw = safeTd(tds, priceIdx);
        if (raw == null) return null;
        raw = raw.replace(".", "").replace(",", ".").replaceAll("[^0-9.\\-]", "");
        if (raw.isBlank()) return null;
        try { return new BigDecimal(raw); } catch (NumberFormatException e) { return null; }
    }

    @Nullable
    private static String safeTd(Elements tds, int idx) {
        if (idx < 0 || idx >= tds.size()) return null;
        return tds.get(idx).text();
    }
}
