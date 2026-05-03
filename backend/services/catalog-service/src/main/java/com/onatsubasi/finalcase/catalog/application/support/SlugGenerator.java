package com.onatsubasi.finalcase.catalog.application.support;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class SlugGenerator {

    public String generate(
            String requestedSlug,
            String fallbackName,
            CatalogErrorCode errorCode
    ) {
        String source = requestedSlug == null || requestedSlug.isBlank()
                ? fallbackName
                : requestedSlug;

        if (source == null || source.isBlank()) {
            throw new BaseException(errorCode, "Slug source is required");
        }

        String slug = normalize(source)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            throw new BaseException(errorCode, "Generated slug cannot be blank");
        }

        return slug;
    }

    private String normalize(String value) {
        String turkishNormalized = value
                .replace("Ğ", "G")
                .replace("ğ", "g")
                .replace("Ü", "U")
                .replace("ü", "u")
                .replace("Ş", "S")
                .replace("ş", "s")
                .replace("İ", "I")
                .replace("ı", "i")
                .replace("Ö", "O")
                .replace("ö", "o")
                .replace("Ç", "C")
                .replace("ç", "c");

        return Normalizer.normalize(turkishNormalized.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}