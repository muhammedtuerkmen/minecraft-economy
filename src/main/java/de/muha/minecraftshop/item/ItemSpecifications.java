/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import org.springframework.data.jpa.domain.Specification;

/** Bausteine für dynamische Filter. Werden im Service je nach gesetzten Parametern kombiniert. */
final class ItemSpecifications {

    private ItemSpecifications() {
    }

    static Specification<Item> hasCategory(Category category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    static Specification<Item> nameContains(String search) {
        String pattern = "%" + search.toLowerCase().replace("%", "\\%").replace("_", "\\_") + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern, '\\');
    }
}
