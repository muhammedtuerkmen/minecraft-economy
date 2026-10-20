/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(Long id) {
        super("Item with id " + id + " not found");
    }
}
