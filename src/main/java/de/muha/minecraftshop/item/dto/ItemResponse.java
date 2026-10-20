/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item.dto;

import de.muha.minecraftshop.item.Category;
import de.muha.minecraftshop.item.Item;

import java.math.BigDecimal;

public record ItemResponse(Long id, String minecraftId, String name, Category category,
                           String categoryLabel, BigDecimal price) {

    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getMinecraftId(), item.getName(), item.getCategory(),
                item.getCategory().getLabel(), item.getPrice());
    }
}
