/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import de.muha.minecraftshop.item.dto.CatalogStatus;
import de.muha.minecraftshop.item.dto.ItemRequest;
import de.muha.minecraftshop.item.dto.ItemResponse;
import de.muha.minecraftshop.item.dto.PriceSyncResult;
import de.muha.minecraftshop.item.dto.PriceUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public Page<ItemResponse> search(Category category, String search, Pageable pageable) {
        List<Specification<Item>> filters = new ArrayList<>();
        if (category != null) {
            filters.add(ItemSpecifications.hasCategory(category));
        }
        if (search != null && !search.isBlank()) {
            filters.add(ItemSpecifications.nameContains(search.strip()));
        }
        return itemRepository.findAll(Specification.allOf(filters), pageable).map(ItemResponse::from);
    }

    public ItemResponse getById(Long id) {
        return ItemResponse.from(findItem(id));
    }

    public CatalogStatus status() {
        return new CatalogStatus(itemRepository.count(), itemRepository.findLastUpdate().orElse(null));
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        if (!itemRepository.findByMinecraftIdIn(List.of(request.minecraftId())).isEmpty()) {
            throw new DuplicateItemException(request.minecraftId());
        }
        Item item = new Item(request.minecraftId(), request.name(), request.category(), request.price());
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item item = findItem(id);
        item.update(request.name(), request.category(), request.price());
        return ItemResponse.from(item); // Dirty Checking speichert automatisch beim Commit
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.delete(findItem(id));
    }

    /**
     * Übernimmt Preise vom Minecraft-Server (z.B. aus einem Shop-Plugin). Unbekannte Items werden
     * nicht angelegt, sondern zurückgemeldet, damit der Katalog die einzige Quelle für Items bleibt.
     */
    @Transactional
    public PriceSyncResult syncPrices(List<PriceUpdate> updates) {
        Map<String, Item> items = itemRepository
                .findByMinecraftIdIn(updates.stream().map(PriceUpdate::minecraftId).toList())
                .stream()
                .collect(Collectors.toMap(Item::getMinecraftId, Function.identity()));

        List<String> unknown = new ArrayList<>();
        int updated = 0;
        for (PriceUpdate update : updates) {
            Item item = items.get(update.minecraftId());
            if (item == null) {
                unknown.add(update.minecraftId());
            } else if (item.getPrice().compareTo(update.price()) != 0) {
                item.changePrice(update.price());
                updated++;
            }
        }
        return new PriceSyncResult(updated, unknown);
    }

    private Item findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }
}
