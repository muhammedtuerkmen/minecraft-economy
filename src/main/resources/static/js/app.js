/*
 * SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
 * Copyright (c) 2026 Muha
 * Commercial use only with prior permission. See LICENSE.md.
 */

(() => {
    const PAGE_SIZE = 48;
    const PLACEHOLDER_ICON = "/img/placeholder.svg";

    const el = {
        search: document.getElementById("search"),
        tabs: document.getElementById("tabs"),
        sort: document.getElementById("sort"),
        grid: document.getElementById("grid"),
        count: document.getElementById("count"),
        sync: document.getElementById("sync"),
        empty: document.getElementById("empty"),
        sentinel: document.getElementById("sentinel"),
    };

    // Zustand kommt aus der URL, damit Filter teilbar und per Zurück-Button erhalten bleiben
    const params = new URLSearchParams(location.search);
    const state = {
        q: params.get("q") ?? "",
        category: params.get("category") ?? "",
        sort: params.get("sort") ?? "name,asc",
        page: 0,
        totalPages: 1,
        loading: false,
        requestId: 0,
    };

    const priceFormat = new Intl.NumberFormat("en-US", {maximumFractionDigits: 2, useGrouping: false});

    // ---------- Rendering ----------

    function renderTabs(categories) {
        const all = [{key: "", label: "Alle"}, ...categories];
        el.tabs.replaceChildren(...all.map(({key, label}) => {
            const tab = document.createElement("button");
            tab.className = "tab";
            tab.type = "button";
            tab.role = "tab";
            tab.textContent = label;
            tab.dataset.category = key;
            tab.setAttribute("aria-selected", String(key === state.category));
            tab.addEventListener("click", () => {
                state.category = key;
                el.tabs.querySelectorAll(".tab").forEach(t =>
                    t.setAttribute("aria-selected", String(t.dataset.category === key)));
                reload();
            });
            return tab;
        }));
    }

    function card(item) {
        const article = document.createElement("article");
        article.className = "card";

        const iconWrap = document.createElement("div");
        iconWrap.className = "card__icon";
        const img = document.createElement("img");
        img.src = `/icons/${item.minecraftId}.png`;
        img.alt = "";
        img.loading = "lazy";
        img.onerror = () => { img.onerror = null; img.src = PLACEHOLDER_ICON; };
        iconWrap.append(img);

        const name = document.createElement("div");
        name.className = "card__name";
        name.textContent = item.name;

        const category = document.createElement("div");
        category.className = "card__category";
        category.textContent = item.categoryLabel;

        const price = document.createElement("div");
        price.className = "card__price" + (item.price >= 1 ? " card__price--high" : "");
        price.textContent = `${priceFormat.format(item.price)} $`;

        article.append(iconWrap, name, category, price);
        return article;
    }

    function showSkeletons() {
        el.grid.replaceChildren(...Array.from({length: 12}, () => {
            const s = document.createElement("div");
            s.className = "card skeleton";
            return s;
        }));
    }

    function relativeTime(iso) {
        if (!iso) return "noch keine Daten";
        const minutes = Math.round((Date.now() - new Date(iso).getTime()) / 60000);
        if (minutes < 1) return "Stand gerade eben";
        if (minutes < 60) return `Stand vor ${minutes} Min.`;
        const hours = Math.round(minutes / 60);
        if (hours < 24) return `Stand vor ${hours} Std.`;
        return `Stand vor ${Math.round(hours / 24)} Tagen`;
    }

    // ---------- Daten laden ----------

    async function fetchJson(url) {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
        return res.json();
    }

    async function loadPage() {
        if (state.loading || state.page >= state.totalPages) return;
        state.loading = true;
        const requestId = state.requestId;

        const query = new URLSearchParams({page: state.page, size: PAGE_SIZE, sort: state.sort});
        if (state.q) query.set("q", state.q);
        if (state.category) query.set("category", state.category);

        try {
            const data = await fetchJson(`/api/items?${query}`);
            if (requestId !== state.requestId) return; // veraltete Antwort, Filter hat sich geändert

            if (state.page === 0) el.grid.replaceChildren();
            el.grid.append(...data.content.map(card));

            state.totalPages = data.page.totalPages;
            state.page++;
            const total = data.page.totalElements;
            el.count.textContent = `${total} ${total === 1 ? "Item" : "Items"} gefunden`;
            el.empty.hidden = total > 0;
        } catch (e) {
            el.grid.replaceChildren();
            el.count.textContent = "Items konnten nicht geladen werden.";
            console.error(e);
        } finally {
            if (requestId === state.requestId) state.loading = false;
        }
        // Große Bildschirme: Ende ist evtl. noch sichtbar, dann direkt weiterladen
        if (requestId === state.requestId && sentinelVisible()) loadPage();
    }

    function sentinelVisible() {
        return el.sentinel.getBoundingClientRect().top < innerHeight + 600;
    }

    function reload() {
        state.requestId++;
        state.page = 0;
        state.totalPages = 1;
        state.loading = false;
        syncUrl();
        showSkeletons();
        loadPage();
    }

    function syncUrl() {
        const p = new URLSearchParams();
        if (state.q) p.set("q", state.q);
        if (state.category) p.set("category", state.category);
        if (state.sort !== "name,asc") p.set("sort", state.sort);
        history.replaceState(null, "", p.size ? `?${p}` : location.pathname);
    }

    async function loadStatus() {
        try {
            const status = await fetchJson("/api/items/status");
            el.sync.textContent = `Live synchronisiert · ${relativeTime(status.lastUpdate)}`;
        } catch {
            el.sync.textContent = "Status unbekannt";
        }
    }

    // ---------- Events ----------

    let debounce;
    el.search.value = state.q;
    el.search.addEventListener("input", () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => {
            state.q = el.search.value.trim();
            reload();
        }, 250);
    });

    el.sort.value = state.sort;
    el.sort.addEventListener("change", () => {
        state.sort = el.sort.value;
        reload();
    });

    // Endlos-Scrollen: nächste Seite laden, sobald das Ende sichtbar wird
    new IntersectionObserver(entries => {
        if (entries[0].isIntersecting) loadPage();
    }, {rootMargin: "600px"}).observe(el.sentinel);

    // ---------- Start ----------

    fetchJson("/api/categories").then(renderTabs).catch(() => renderTabs([]));
    loadStatus();
    setInterval(loadStatus, 60_000);
    reload();
})();
