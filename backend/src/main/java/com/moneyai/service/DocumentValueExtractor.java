package com.moneyai.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Encontra valores monetários (ex.: R$ 1.234,56) em um texto. */
final class DocumentValueExtractor {

    private static final Pattern MONEY = Pattern.compile("R\\$\\s*(\\d{1,3}(?:\\.\\d{3})*,\\d{2}|\\d+,\\d{2})");

    private DocumentValueExtractor() { }

    static List<BigDecimal> extract(String text) {
        List<BigDecimal> values = new ArrayList<>();
        Matcher m = MONEY.matcher(text);
        while (m.find()) {
            values.add(new BigDecimal(m.group(1).replace(".", "").replace(",", ".")));
        }
        return values;
    }
}
