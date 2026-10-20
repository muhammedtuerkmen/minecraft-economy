/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

public class DuplicateItemException extends RuntimeException {

    public DuplicateItemException(String minecraftId) {
        super("Item with minecraftId '" + minecraftId + "' already exists");
    }
}
