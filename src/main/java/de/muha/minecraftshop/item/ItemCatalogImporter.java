/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Legt beim Start alle Items aus dem Katalog (catalog/items.csv) an, die noch nicht in der DB sind.
 * Bestehende Items und ihre Preise werden nie überschrieben. Neue Minecraft-Version:
 * tools/generate_catalog.py ausführen, App neu starten, fertig.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(name = "shop.catalog.import-on-startup", matchIfMissing = true)
class ItemCatalogImporter implements ApplicationRunner {

    private final ItemRepository itemRepository;

    @Value("${shop.catalog.location:classpath:catalog/items.csv}")
    private Resource catalog;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Set<String> existing = itemRepository.findAllMinecraftIds();
        List<Item> newItems = readCatalog().stream()
                .filter(item -> !existing.contains(item.getMinecraftId()))
                .toList();

        itemRepository.saveAll(newItems);
        log.info("Item catalog: {} new items imported, {} already present", newItems.size(), existing.size());
    }

    private List<Item> readCatalog() {
        try (var reader = new BufferedReader(new InputStreamReader(catalog.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1) // Header
                    .filter(line -> !line.isBlank())
                    .map(ItemCatalogImporter::parseLine)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read item catalog " + catalog, e);
        }
    }

    /** Format: minecraft_id,name,category,price (Namen enthalten keine Kommas). */
    private static Item parseLine(String line) {
        String[] cols = line.split(",", -1);
        return new Item(cols[0], cols[1], Category.valueOf(cols[2]), new BigDecimal(cols[3]));
    }
}
