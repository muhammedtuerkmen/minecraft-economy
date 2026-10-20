/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    BLOCKS("Blöcke"),
    BOOKS("Bücher"),
    FOOD("Essen"),
    COMBAT("Kampf"),
    MISC("Sonstiges"),
    POTIONS("Tränke"),
    UTILITIES("Utilities"),
    TOOLS("Werkzeug"),
    INGREDIENTS("Zutaten");

    private final String label;
}
