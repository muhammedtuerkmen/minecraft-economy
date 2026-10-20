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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public PagedModel<ItemResponse> search(@RequestParam(required = false) Category category,
                                           @RequestParam(name = "q", required = false) String search,
                                           @PageableDefault(size = 48, sort = "name") Pageable pageable) {
        return new PagedModel<>(itemService.search(category, search, pageable));
    }

    @GetMapping("/status")
    public CatalogStatus status() {
        return itemService.status();
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable Long id) {
        return itemService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody ItemRequest request) {
        return itemService.create(request);
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return itemService.update(id, request);
    }

    @PutMapping("/prices")
    public PriceSyncResult syncPrices(@RequestBody List<@Valid PriceUpdate> updates) {
        return itemService.syncPrices(updates);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemService.delete(id);
    }
}
