package com.dawak.api.catalogue.application;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class MedicineSearchNormalizer {
    public String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace("ـ", "")
                .replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
                .replace('ى', 'ي').replace('ئ', 'ي').replace('ؤ', 'و')
                .replace('ک', 'ك').replace('ی', 'ي')
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return normalized;
    }
}
