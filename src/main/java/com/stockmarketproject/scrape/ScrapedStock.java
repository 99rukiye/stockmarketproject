package com.stockmarketproject.scrape;

import java.math.BigDecimal;

public record ScrapedStock(String symbol, String name, BigDecimal price) {}
