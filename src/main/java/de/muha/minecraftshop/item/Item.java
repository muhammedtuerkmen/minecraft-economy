/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // nur für JPA
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Technischer Minecraft-Name, z.B. "acacia_boat". Fachlicher Schlüssel für den Preis-Sync. */
    @Column(nullable = false, unique = true, updatable = false)
    private String minecraftId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Item(String minecraftId, String name, Category category, BigDecimal price) {
        this.minecraftId = minecraftId;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public void update(String name, Category category, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public void changePrice(BigDecimal newPrice) {
        this.price = newPrice;
    }
}
