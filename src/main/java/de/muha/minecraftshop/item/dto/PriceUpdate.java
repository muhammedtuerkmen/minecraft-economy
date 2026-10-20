/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Ein Eintrag beim Preis-Sync vom Minecraft-Server, z.B. {"minecraftId": "diamond", "price": 75}. */
public record PriceUpdate(
        @NotBlank String minecraftId,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal price
) {
}
