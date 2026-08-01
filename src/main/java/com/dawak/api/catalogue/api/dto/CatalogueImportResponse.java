package com.dawak.api.catalogue.api.dto;

import java.util.List;
import java.util.UUID;

public record CatalogueImportResponse(int importedCount, List<UUID> packageIds, boolean replayed) {}
