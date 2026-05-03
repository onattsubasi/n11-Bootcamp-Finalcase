package com.onatsubasi.finalcase.search.application.dto.response;

import java.util.List;

public record AutocompleteResponse(
        List<String> suggestions
) {
}
