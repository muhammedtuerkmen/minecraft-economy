/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

package de.muha.minecraftshop.item;

import de.muha.minecraftshop.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional // jeder Test wird zurückgerollt
class ItemApiTests {

    @Autowired
    MockMvc mvc;

    @Test
    void catalogIsImportedOnStartup() throws Exception {
        mvc.perform(get("/api/items/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(greaterThan(1000)));
    }

    @Test
    void searchesByNameAndCategory() throws Exception {
        mvc.perform(get("/api/items").param("q", "diamond sw").param("category", "COMBAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].minecraftId").value("diamond_sword"))
                .andExpect(jsonPath("$.content[0].categoryLabel").value("Kampf"));
    }

    @Test
    void syncsPricesAndReportsUnknownItems() throws Exception {
        mvc.perform(put("/api/items/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"minecraftId": "diamond", "price": 99.5},
                                 {"minecraftId": "not_an_item", "price": 1}]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.unknownMinecraftIds[0]").value("not_an_item"));

        mvc.perform(get("/api/items").param("q", "diamond").param("sort", "price,asc"))
                .andExpect(jsonPath("$.content[?(@.minecraftId == 'diamond')].price").value(99.5));
    }

    @Test
    void rejectsInvalidPrice() throws Exception {
        mvc.perform(put("/api/items/prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"minecraftId\": \"diamond\", \"price\": -5}]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSortPropertyIsBadRequest() throws Exception {
        mvc.perform(get("/api/items").param("sort", "foo"))
                .andExpect(status().isBadRequest());
    }
}
