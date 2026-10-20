/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item.dto;

import de.muha.minecraftshop.item.Category;

public record CategoryResponse(Category key, String label) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category, category.getLabel());
    }
}
