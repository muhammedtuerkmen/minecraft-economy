/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item.dto;

import java.util.List;

public record PriceSyncResult(int updated, List<String> unknownMinecraftIds) {
}
