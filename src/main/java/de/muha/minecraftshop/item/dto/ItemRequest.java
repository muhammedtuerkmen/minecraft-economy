/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item.dto;

import de.muha.minecraftshop.item.Category;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Wird für Create (POST) und Update (PUT) verwendet. Die minecraftId ist nach dem Anlegen fix. */
public record ItemRequest(
        @NotBlank @Pattern(regexp = "[a-z0-9_]+") @Size(max = 100) String minecraftId,
        @NotBlank @Size(max = 255) String name,
        @NotNull Category category,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price
) {
}
