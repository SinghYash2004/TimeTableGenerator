function initShell(options) {
    const shell = document.querySelector(".app-shell");
    const sidebarBtn = document.getElementById("toggleSidebar");
    const insightBtn = document.getElementById("toggleInsight");
    const themeBtn = document.getElementById("toggleTheme");
    const search = document.getElementById("globalSearch");

    const themeCatalog = [
        { id: "light", label: "Light" },
        { id: "dark", label: "Dark" },
        { id: "peach-aqua", label: "Peach Ice / Aqua Mist" },
        { id: "pearl-charcoal", label: "Pearl White / Charcoal Stone" },
        { id: "cloudy-ocean", label: "Cloudy Sky / Ocean Blue" },
        { id: "soft-olive", label: "Soft Sage / Deep Olive" }
    ];
    const themeIds = themeCatalog.map(function (t) { return t.id; });

    function applyTheme(themeId) {
        const next = themeIds.includes(themeId) ? themeId : "light";
        document.body.setAttribute("data-theme", next);
        localStorage.setItem("theme", next);
        if (themeBtn) {
            const meta = themeCatalog.find(function (t) { return t.id === next; });
            themeBtn.setAttribute("data-theme", next);
            if (meta) {
                themeBtn.setAttribute("title", meta.label);
                themeBtn.textContent = meta.label;
            } else {
                themeBtn.textContent = "Theme";
            }
        }
    }

    const savedTheme = localStorage.getItem("theme");
    const bodyTheme = document.body.getAttribute("data-theme");
    const resolvedTheme = savedTheme || bodyTheme || "light";
    applyTheme(resolvedTheme);
    if (!savedTheme && bodyTheme) {
        localStorage.setItem("theme", bodyTheme);
    }
    const savedMotion = localStorage.getItem("motionPreference");
    const bodyMotion = document.body.getAttribute("data-motion");
    const resolvedMotion = bodyMotion || savedMotion || "system";
    applyMotionPreference(resolvedMotion);
    if (bodyMotion) {
        localStorage.setItem("motionPreference", resolvedMotion);
    }

    function pulseShellClass(target, className, duration) {
        if (!target) {
            return;
        }
        if (!target.__animTimers) {
            target.__animTimers = {};
        }
        target.classList.remove(className);
        void target.offsetWidth;
        target.classList.add(className);
        if (target.__animTimers[className]) {
            clearTimeout(target.__animTimers[className]);
        }
        target.__animTimers[className] = setTimeout(function () {
            target.classList.remove(className);
        }, duration || 520);
    }

    if (sidebarBtn && shell) {
        sidebarBtn.setAttribute("aria-pressed", shell.classList.contains("sidebar-collapsed") ? "true" : "false");
        sidebarBtn.addEventListener("click", function () {
            shell.classList.toggle("sidebar-collapsed");
            pulseShellClass(shell, "sidebar-animating", 560);
            sidebarBtn.setAttribute("aria-pressed", shell.classList.contains("sidebar-collapsed") ? "true" : "false");
            showToast(shell.classList.contains("sidebar-collapsed") ? "Sidebar collapsed" : "Sidebar expanded", "success");
        });
    }
    if (insightBtn && shell) {
        insightBtn.setAttribute("aria-pressed", shell.classList.contains("insight-collapsed") ? "true" : "false");
        insightBtn.addEventListener("click", function () {
            shell.classList.toggle("insight-collapsed");
            pulseShellClass(shell, "insight-animating", 560);
            insightBtn.setAttribute("aria-pressed", shell.classList.contains("insight-collapsed") ? "true" : "false");
            showToast(shell.classList.contains("insight-collapsed") ? "Insights hidden" : "Insights shown", "success");
        });
    }
    if (themeBtn) {
        themeBtn.addEventListener("click", function (event) {
            event.preventDefault();
            openThemeMenu(themeBtn, themeCatalog, applyTheme);
        });
    }
    if (search) {
        let timer;
        search.addEventListener("input", function () {
            clearTimeout(timer);
            timer = setTimeout(function () {
                const q = search.value.toLowerCase();
                document.querySelectorAll("[data-search]").forEach(function (el) {
                    el.style.display = el.dataset.search.includes(q) ? "" : "none";
                });
            }, 220);
        });
    }

    if (options && options.toast) {
        showToast(options.toast.text, options.toast.type || "success");
    }

    initNotifications();
    initMotionPreferenceControls();
    initExperienceInteractions();
    initValidationPopups();
    initScrollReveal();
}

function openThemeMenu(anchor, catalog, onSelect) {
    if (!anchor) {
        return;
    }
    const existing = document.getElementById("themeMenu");
    if (existing) {
        existing.remove();
        return;
    }

    const menu = document.createElement("div");
    menu.id = "themeMenu";
    menu.className = "theme-menu";
    const current = document.body.getAttribute("data-theme") || "light";

    catalog.forEach(function (theme) {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "theme-option";
        btn.textContent = theme.label;
        btn.setAttribute("data-theme", theme.id);
        btn.setAttribute("aria-pressed", theme.id === current ? "true" : "false");
        btn.addEventListener("click", function () {
            onSelect(theme.id);
            menu.remove();
        });
        menu.appendChild(btn);
    });

    document.body.appendChild(menu);
    const rect = anchor.getBoundingClientRect();
    menu.style.top = rect.bottom + 8 + "px";
    menu.style.right = Math.max(16, window.innerWidth - rect.right) + "px";

    setTimeout(function () {
        document.addEventListener("click", function handleClose(ev) {
            if (!menu.contains(ev.target) && ev.target !== anchor) {
                menu.remove();
                document.removeEventListener("click", handleClose);
            }
        });
    }, 0);
}

function initValidationPopups() {
    const activePopups = new Map();
    const inlineHints = new Map();

    function isFormField(el) {
        if (!(el instanceof HTMLElement)) {
            return false;
        }
        if (!el.matches("input, select, textarea")) {
            return false;
        }
        const type = el.getAttribute("type");
        if (type && ["hidden", "checkbox", "radio", "file", "submit", "button"].includes(type)) {
            return false;
        }
        return true;
    }

    function removePopup(el) {
        const popup = activePopups.get(el);
        if (popup) {
            popup.remove();
            activePopups.delete(el);
        }
    }

    function positionPopup(el, popup) {
        const rect = el.getBoundingClientRect();
        const padding = 10;
        const popupRect = popup.getBoundingClientRect();
        let left = rect.left + rect.width - popupRect.width;
        left = Math.max(padding, Math.min(left, window.innerWidth - popupRect.width - padding));
        let top = rect.top - popupRect.height - 10;
        if (top < padding) {
            top = rect.bottom + 10;
            popup.classList.add("field-popup--below");
        } else {
            popup.classList.remove("field-popup--below");
        }
        popup.style.left = left + "px";
        popup.style.top = top + "px";
    }

    function getInlineHint(el) {
        if (inlineHints.has(el)) {
            return inlineHints.get(el);
        }
        const hint = document.createElement("div");
        hint.className = "field-hint";
        hint.setAttribute("aria-live", "polite");
        hint.setAttribute("role", "status");
        el.insertAdjacentElement("afterend", hint);
        inlineHints.set(el, hint);
        return hint;
    }

    function clearInlineHint(el) {
        const hint = inlineHints.get(el);
        if (hint) {
            hint.textContent = "";
            hint.classList.remove("field-hint--error");
        }
        if (el && el.classList) {
            el.classList.remove("field-invalid");
        }
    }

    function showInlineHint(el, message) {
        const hint = getInlineHint(el);
        hint.textContent = message || "";
        hint.classList.toggle("field-hint--error", Boolean(message));
        if (el && el.classList) {
            el.classList.toggle("field-invalid", Boolean(message));
        }
    }

    document.addEventListener("invalid", function (event) {
        const el = event.target;
        if (!isFormField(el) || typeof el.validationMessage !== "string") {
            return;
        }
        event.preventDefault();
        const message = el.validationMessage;
        removePopup(el);

        const popup = document.createElement("div");
        popup.className = "field-popup";
        const icon = document.createElement("span");
        icon.className = "field-popup__icon";
        icon.textContent = "!";
        const text = document.createElement("span");
        text.className = "field-popup__text";
        text.textContent = message;
        popup.appendChild(icon);
        popup.appendChild(text);
        document.body.appendChild(popup);
        activePopups.set(el, popup);

        positionPopup(el, popup);
        requestAnimationFrame(function () {
            popup.classList.add("field-popup--show");
        });
        showInlineHint(el, message);
    }, true);

    document.addEventListener("input", function (event) {
        const el = event.target;
        if (isFormField(el)) {
            removePopup(el);
            if (el.checkValidity()) {
                clearInlineHint(el);
            } else if (el.value && el.validationMessage) {
                showInlineHint(el, el.validationMessage);
            }
        }
    }, true);

    document.addEventListener("blur", function (event) {
        const el = event.target;
        if (isFormField(el)) {
            if (el.checkValidity()) {
                clearInlineHint(el);
            } else if (el.validationMessage) {
                showInlineHint(el, el.validationMessage);
            }
        }
    }, true);

    document.addEventListener("focusin", function (event) {
        const el = event.target;
        if (isFormField(el)) {
            removePopup(el);
        }
    }, true);

    window.addEventListener("resize", function () {
        activePopups.forEach(function (popup, el) {
            positionPopup(el, popup);
        });
    });
    window.addEventListener("scroll", function () {
        activePopups.forEach(function (popup, el) {
            positionPopup(el, popup);
        });
    }, true);
}

function showToast(text, type) {
    let wrap = document.querySelector(".toast-wrap");
    if (!wrap) {
        wrap = document.createElement("div");
        wrap.className = "toast-wrap";
        document.body.appendChild(wrap);
    }
    const toast = document.createElement("div");
    toast.className = "toast " + (type || "success");
    toast.textContent = text;
    wrap.appendChild(toast);
    pushNotification(text, type || "success");
    setTimeout(function () {
        toast.remove();
    }, 2800);
}

function initNotifications() {
    if (document.getElementById("notificationCenter")) {
        return;
    }
    const toolbar = document.querySelector(".toolbar");
    if (!toolbar) {
        return;
    }

    const btn = document.createElement("button");
    btn.type = "button";
    btn.id = "toggleNotifications";
    btn.className = "btn magnetic-btn notify-btn";
    btn.innerHTML = "<span class=\"notify-icon\">🔔</span><span class=\"notify-count\" id=\"notifyCount\">0</span>";
    btn.setAttribute("aria-label", "Notifications");
    toolbar.insertBefore(btn, toolbar.firstChild);

    const panel = document.createElement("div");
    panel.id = "notificationCenter";
    panel.className = "notification-center";
    panel.setAttribute("role", "dialog");
    panel.setAttribute("aria-label", "Notification center");
    panel.innerHTML = "<div class=\"notification-header\"><div><strong>Notifications</strong><div class=\"muted\" style=\"font-size:12px;\">Recent activity</div></div><button type=\"button\" class=\"btn magnetic-btn\" id=\"clearNotifications\">Clear</button></div><div class=\"notification-list\" id=\"notificationList\"></div>";
    document.body.appendChild(panel);

    function togglePanel() {
        panel.classList.toggle("notification-center--open");
        btn.setAttribute("aria-pressed", panel.classList.contains("notification-center--open") ? "true" : "false");
    }

    btn.addEventListener("click", function (event) {
        event.stopPropagation();
        togglePanel();
    });

    document.addEventListener("click", function (event) {
        if (!panel.contains(event.target) && event.target !== btn) {
            panel.classList.remove("notification-center--open");
        }
    });

    const clearBtn = document.getElementById("clearNotifications");
    if (clearBtn) {
        clearBtn.addEventListener("click", function () {
            saveNotifications([]);
            renderNotifications();
        });
    }

    renderNotifications();
}

function loadNotifications() {
    try {
        const raw = localStorage.getItem("erp_notifications");
        const parsed = raw ? JSON.parse(raw) : [];
        return Array.isArray(parsed) ? parsed : [];
    } catch (err) {
        return [];
    }
}

function saveNotifications(items) {
    localStorage.setItem("erp_notifications", JSON.stringify(items));
}

function pushNotification(text, type) {
    const items = loadNotifications();
    items.unshift({
        id: Date.now(),
        text: text,
        type: type || "success",
        time: new Date().toISOString()
    });
    const trimmed = items.slice(0, 25);
    saveNotifications(trimmed);
    renderNotifications();
}

function renderNotifications() {
    const list = document.getElementById("notificationList");
    const count = document.getElementById("notifyCount");
    if (!list || !count) {
        return;
    }
    const items = loadNotifications();
    count.textContent = String(items.length);
    list.innerHTML = "";
    if (!items.length) {
        const empty = document.createElement("div");
        empty.className = "muted";
        empty.textContent = "No notifications yet.";
        list.appendChild(empty);
        return;
    }
    items.forEach(function (item) {
        const row = document.createElement("div");
        row.className = "notification-item notification-item--" + item.type;
        const time = new Date(item.time);
        row.innerHTML = "<div class=\"notification-text\">" + item.text + "</div><div class=\"notification-time\">" + time.toLocaleString() + "</div>";
        list.appendChild(row);
    });
}

function initReportSavedViews() {
    const form = document.getElementById("reportsFilterForm");
    const semesterInput = document.getElementById("reportSemester");
    const departmentSelect = document.getElementById("reportDepartment");
    const viewNameInput = document.getElementById("reportViewName");
    const viewsSelect = document.getElementById("reportSavedViews");
    const saveBtn = document.getElementById("saveReportView");
    const applyBtn = document.getElementById("applyReportView");
    const deleteBtn = document.getElementById("deleteReportView");

    if (!form || !semesterInput || !departmentSelect || !viewsSelect || !saveBtn || !applyBtn || !deleteBtn) {
        return;
    }

    const storageKey = "erp_reports_views";

    function loadViews() {
        try {
            const raw = localStorage.getItem(storageKey);
            const parsed = raw ? JSON.parse(raw) : [];
            return Array.isArray(parsed) ? parsed : [];
        } catch (err) {
            return [];
        }
    }

    function saveViews(views) {
        localStorage.setItem(storageKey, JSON.stringify(views));
    }

    function refreshSelect(views, selectedName) {
        viewsSelect.innerHTML = "";
        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Saved views";
        viewsSelect.appendChild(placeholder);
        views.forEach(function (view) {
            const opt = document.createElement("option");
            opt.value = view.name;
            opt.textContent = view.name;
            if (selectedName && selectedName === view.name) {
                opt.selected = true;
            }
            viewsSelect.appendChild(opt);
        });
    }

    function normalizeName(name) {
        return (name || "").trim();
    }

    function upsertView(views, view) {
        const idx = views.findIndex(function (v) {
            return v.name.toLowerCase() === view.name.toLowerCase();
        });
        if (idx >= 0) {
            views[idx] = view;
        } else {
            views.push(view);
        }
        return views;
    }

    function getCurrentView() {
        return {
            name: normalizeName(viewNameInput ? viewNameInput.value : ""),
            semester: (semesterInput.value || "").trim(),
            departmentId: departmentSelect.value || ""
        };
    }

    function applyView(view) {
        semesterInput.value = view.semester || "";
        departmentSelect.value = view.departmentId || "";
        form.submit();
    }

    let views = loadViews();
    refreshSelect(views);

    saveBtn.addEventListener("click", function () {
        const view = getCurrentView();
        if (!view.name) {
            showToast("Give this view a name before saving.", "error");
            return;
        }
        views = upsertView(views, view);
        saveViews(views);
        refreshSelect(views, view.name);
        showToast("Report view saved.", "success");
    });

    applyBtn.addEventListener("click", function () {
        const selected = viewsSelect.value;
        const view = views.find(function (v) { return v.name === selected; });
        if (!view) {
            showToast("Select a saved view to apply.", "error");
            return;
        }
        applyView(view);
    });

    deleteBtn.addEventListener("click", function () {
        const selected = viewsSelect.value;
        if (!selected) {
            showToast("Select a saved view to delete.", "error");
            return;
        }
        views = views.filter(function (v) { return v.name !== selected; });
        saveViews(views);
        refreshSelect(views);
        showToast("Saved view removed.", "success");
    });
}

function attachTableSort(tableSelector) {
    const table = document.querySelector(tableSelector);
    if (!table) {
        return;
    }
    const tbody = table.querySelector("tbody");
    table.querySelectorAll("th[data-sort]").forEach(function (th, index) {
        th.addEventListener("click", function () {
            const rows = Array.from(tbody.querySelectorAll("tr"));
            const asc = th.dataset.asc !== "true";
            th.dataset.asc = asc ? "true" : "false";
            rows.sort(function (a, b) {
                const av = a.children[index].innerText.trim();
                const bv = b.children[index].innerText.trim();
                return asc ? av.localeCompare(bv, undefined, { numeric: true }) : bv.localeCompare(av, undefined, { numeric: true });
            });
            rows.forEach(function (r) {
                tbody.appendChild(r);
            });
        });
    });
}

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.style.display = "grid";
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.style.display = "none";
    }
}

function initCharts(config) {
    if (!window.Chart || !config) {
        return;
    }
    if (config.weeklyLoad && document.getElementById("weeklyLoadChart")) {
        new Chart(document.getElementById("weeklyLoadChart"), {
            type: "line",
            data: {
                labels: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
                datasets: [{ data: config.weeklyLoad, borderColor: "#79b7ff", backgroundColor: "rgba(121,183,255,.16)", tension: .35, fill: true }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: "rgba(166,190,229,.16)" }, ticks: { color: "#d7e5ff" } },
                    x: { grid: { color: "rgba(166,190,229,.1)" }, ticks: { color: "#d7e5ff" } }
                }
            }
        });
    }
    if (config.budgetSpread && document.getElementById("budgetSpreadChart")) {
        new Chart(document.getElementById("budgetSpreadChart"), {
            type: "doughnut",
            data: {
                labels: ["Used", "Remaining"],
                datasets: [{ data: config.budgetSpread, backgroundColor: ["#d8bd84", "#5f7ea9"] }]
            },
            options: { plugins: { legend: { position: "bottom", labels: { color: "#d7e5ff" } } } }
        });
    }
    if (config.heatmap && document.getElementById("periodHeatmapChart")) {
        new Chart(document.getElementById("periodHeatmapChart"), {
            type: "bar",
            data: {
                labels: ["P1", "P2", "P3", "P4", "P5", "P6", "P7"],
                datasets: [{ data: config.heatmap, backgroundColor: "#2dd4bf" }]
            },
            options: {
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, grid: { color: "rgba(166,190,229,.16)" }, ticks: { color: "#d7e5ff" } },
                    x: { grid: { color: "rgba(166,190,229,.1)" }, ticks: { color: "#d7e5ff" } }
                }
            }
        });
    }
}

function initDashboardExperience(config) {
    if (!document.body.classList.contains("dashboard-page")) {
        return;
    }
    if (window.__dashboardInitDone) {
        return;
    }
    window.__dashboardInitDone = true;

    const state = {
        conflicts: Number(config?.conflicts || 0),
        overload: Number(config?.overload || 0),
        score: Number(config?.score || 0),
        budget: Number(config?.budget || 0),
        roomUtil: Number(config?.roomUtil || 0)
    };

    startInsightTyping();
    updateDashboardMetrics(state, true);
    startDashboardPolling(state);
}

function initExperienceInteractions() {
    if (!document.body.classList.contains("experience-page")) {
        return;
    }
    if (window.__experienceInitDone) {
        return;
    }
    window.__experienceInitDone = true;
    bindSpotlight();
    bindTiltCards();
    bindMagneticButtons();
}

function bindSpotlight() {
    const root = document.body;
    root.addEventListener("pointermove", function (event) {
        const x = (event.clientX / window.innerWidth) * 100;
        const y = (event.clientY / window.innerHeight) * 100;
        root.style.setProperty("--spot-x", x + "%");
        root.style.setProperty("--spot-y", y + "%");
    });
}

function bindTiltCards() {
    document.querySelectorAll(".experience-page .tilt-card").forEach(function (card) {
        card.addEventListener("pointermove", function (event) {
            const rect = card.getBoundingClientRect();
            const px = (event.clientX - rect.left) / rect.width;
            const py = (event.clientY - rect.top) / rect.height;
            const tiltX = (0.5 - py) * 5;
            const tiltY = (px - 0.5) * 6;
            card.style.transform = "perspective(900px) rotateX(" + tiltX.toFixed(2) + "deg) rotateY(" + tiltY.toFixed(2) + "deg) translateY(-2px)";
        });
        card.addEventListener("pointerleave", function () {
            card.style.transform = "";
        });
    });
}

function bindMagneticButtons() {
    document.querySelectorAll(".experience-page .magnetic-btn").forEach(function (button) {
        button.addEventListener("pointermove", function (event) {
            const rect = button.getBoundingClientRect();
            const x = event.clientX - rect.left - rect.width / 2;
            const y = event.clientY - rect.top - rect.height / 2;
            button.style.transform = "translate(" + (x * 0.06).toFixed(2) + "px," + (y * 0.06).toFixed(2) + "px)";
        });
        button.addEventListener("pointerleave", function () {
            button.style.transform = "";
        });

        /* Lightweight ripple for tactile response without external libraries. */
        button.addEventListener("click", function (event) {
            const rect = button.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const ripple = document.createElement("span");
            ripple.className = "btn-ripple";
            ripple.style.width = size + "px";
            ripple.style.height = size + "px";
            ripple.style.left = (event.clientX - rect.left - size / 2) + "px";
            ripple.style.top = (event.clientY - rect.top - size / 2) + "px";
            button.appendChild(ripple);
            ripple.addEventListener("animationend", function () {
                ripple.remove();
            });
        });
    });
}

function initScrollReveal() {
    if (window.__scrollRevealInit) {
        return;
    }
    window.__scrollRevealInit = true;

    const prefersReduce = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const appReduce = document.body.classList.contains("app-reduce-motion");

    const selectors = [
        ".main h1",
        ".main h2",
        ".main h3",
        ".main p",
        ".main .card",
        ".main .table-wrap",
        ".main .dept-card",
        ".main .filter-bar",
        ".main .grid",
        ".main .split",
        ".main .kpi",
        ".main .badge",
        ".main .pill",
        ".main .progress",
        ".main .heatmap-cell",
        ".main .notification-item"
    ];

    const elements = Array.from(document.querySelectorAll(selectors.join(", ")));
    if (!elements.length) {
        return;
    }

    elements.forEach(function (el, idx) {
        if (!el.classList.contains("reveal")) {
            el.classList.add("reveal");
        }
        const delay = Math.min(idx % 6, 6) * 60;
        el.style.setProperty("--reveal-delay", delay + "ms");
    });

    if (prefersReduce || appReduce || typeof IntersectionObserver === "undefined") {
        elements.forEach(function (el) { el.classList.add("reveal-visible"); });
        return;
    }

    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add("reveal-visible");
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.15, rootMargin: "0px 0px -10% 0px" });

    elements.forEach(function (el) {
        observer.observe(el);
    });
}

function startInsightTyping() {
    const target = document.getElementById("aiTypingText");
    if (!target) {
        return;
    }
    const insights = [
        "Cross-department conflict risk trending down after slot balancing.",
        "Model suggests reallocating two Friday periods to reduce overload by 11%.",
        "Confidence gain detected from recent room utilization normalization."
    ];
    let insightIndex = 0;
    let charIndex = 0;
    let deleting = false;

    function tick() {
        if (window.__dashboardLiveInsight && typeof window.__dashboardLiveInsight === "string") {
            insights[0] = window.__dashboardLiveInsight;
        }
        const message = insights[insightIndex];
        if (!deleting) {
            charIndex += 1;
            target.textContent = message.slice(0, charIndex);
            if (charIndex >= message.length) {
                deleting = true;
                setTimeout(tick, 1800);
                return;
            }
        } else {
            charIndex -= 1;
            target.textContent = message.slice(0, Math.max(0, charIndex));
            if (charIndex <= 0) {
                deleting = false;
                insightIndex = (insightIndex + 1) % insights.length;
            }
        }
        setTimeout(tick, deleting ? 24 : 34);
    }

    tick();
}

function startDashboardPolling(state) {
    async function pull() {
        if (document.hidden) {
            return;
        }
        try {
            const response = await fetch("/api/dashboard/summary", {
                headers: { Accept: "application/json" },
                cache: "no-store"
            });
            if (!response.ok) {
                return;
            }
            const live = await response.json();
            state.conflicts = Number(live.conflicts || 0);
            state.overload = Number(live.overload || 0);
            state.score = Number(live.score || 0);
            state.budget = Number(live.budget || 0);
            state.roomUtil = Number(live.roomUtil || state.roomUtil || 0);
            updateDashboardMetrics(state, false);
            if (typeof live.insight === "string" && live.insight.trim()) {
                window.__dashboardLiveInsight = live.insight.trim();
            }
        } catch (err) {
            // Silent fail keeps dashboard stable if polling is unavailable.
        }
    }

    pull();
    setInterval(pull, 6000);
    document.addEventListener("visibilitychange", function () {
        if (!document.hidden) {
            pull();
        }
    });
}

function updateDashboardMetrics(state, initialLoad) {
    const conflictWidth = state.conflicts > 50 ? 95 : clamp(state.conflicts * 2, 0, 95);
    setBarWidth("conflictRiskBar", conflictWidth);
    setBarWidth("overloadRiskBar", clamp(state.overload, 0, 100));
    setBarWidth("confidenceBar", clamp(state.score, 0, 100));

    setInsightSeverity("conflictInsight", state.conflicts, 30, 10);
    setInsightSeverity("overloadInsight", state.overload, 60, 25);
    setInsightSeverity("confidenceInsight", 100 - state.score, 60, 25);

    animateMetricElement("kpiConflicts", state.conflicts, "int", initialLoad ? 900 : 700);
    animateMetricElement("kpiOverload", state.overload, "percent1", initialLoad ? 900 : 700);
    animateMetricElement("kpiRoomUtil", state.roomUtil, "percent1", initialLoad ? 900 : 700);
    animateMetricElement("kpiBudget", state.budget, "percent1", initialLoad ? 900 : 700);
    animateMetricElement("kpiScore", state.score, "int", initialLoad ? 900 : 700);
    animateMetricElement("conflictRiskValue", state.conflicts, "int", initialLoad ? 900 : 700);
    animateMetricElement("overloadRiskValue", state.overload, "percent1", initialLoad ? 900 : 700);
    animateMetricElement("confidenceValue", state.score, "score100", initialLoad ? 900 : 700);

    const alertBadge = document.getElementById("alertBadge");
    if (alertBadge) {
        alertBadge.textContent = "Alerts: " + Math.round(state.conflicts);
        alertBadge.classList.toggle("pulse-critical", state.conflicts >= 20);
    }
}

function animateMetricElement(id, targetValue, format, duration) {
    const el = document.getElementById(id);
    if (!el) {
        return;
    }
    const startValue = Number(el.dataset.currentValue || 0);
    el.dataset.currentValue = String(targetValue);

    const start = performance.now();
    function frame(now) {
        const progress = Math.min(1, (now - start) / duration);
        const eased = 1 - Math.pow(1 - progress, 3);
        const value = startValue + (targetValue - startValue) * eased;
        el.textContent = formatMetric(value, format);
        if (progress < 1) {
            requestAnimationFrame(frame);
        } else {
            el.textContent = formatMetric(targetValue, format);
        }
    }
    requestAnimationFrame(frame);
}

function formatMetric(value, format) {
    if (format === "percent1") {
        return value.toFixed(1) + "%";
    }
    if (format === "score100") {
        return Math.round(value) + "/100";
    }
    return String(Math.round(value));
}

function setBarWidth(id, width) {
    const bar = document.getElementById(id);
    if (bar) {
        bar.style.width = clamp(width, 0, 100) + "%";
    }
}

function setInsightSeverity(id, value, highThreshold, mediumThreshold) {
    const card = document.getElementById(id);
    if (!card) {
        return;
    }
    const severity = value >= highThreshold ? "high" : (value >= mediumThreshold ? "medium" : "low");
    card.setAttribute("data-severity", severity);
}

function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
}

function applyMotionPreference(preference) {
    const value = preference === "reduce" || preference === "full" ? preference : "system";
    const prefersReduced = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const reduce = value === "reduce" || (value === "system" && prefersReduced);
    document.body.classList.toggle("app-reduce-motion", reduce);
    document.body.setAttribute("data-motion", value);
}

function initMotionPreferenceControls() {
    const motionSelect = document.getElementById("motionPreference");
    const motionState = document.getElementById("motionPreferenceState");
    if (!motionSelect) {
        return;
    }

    const saved = localStorage.getItem("motionPreference") || "system";
    motionSelect.value = saved;
    syncMotionStateText(saved, motionState);

    motionSelect.addEventListener("change", function () {
        const next = motionSelect.value || "system";
        localStorage.setItem("motionPreference", next);
        applyMotionPreference(next);
        syncMotionStateText(next, motionState);
        showToast("Motion preference updated", "success");
    });
}

function syncMotionStateText(preference, node) {
    if (!node) {
        return;
    }
    if (preference === "reduce") {
        node.textContent = "Reduced motion is enforced across the app.";
        return;
    }
    if (preference === "full") {
        node.textContent = "Full motion is enabled regardless of system preference.";
        return;
    }
    node.textContent = "Motion follows your system accessibility preference.";
}

function initReportsExperience(config) {
    if (!document.body.classList.contains("reports-page")) {
        return;
    }
    animateMetricElement("reportConflicts", Number(config?.conflicts || 0), "int", 850);
    animateMetricElement("reportBudget", Number(config?.budget || 0), "percent1", 850);
    animateMetricElement("reportUtil", Number(config?.roomUtil || 0), "percent1", 850);
}

function initReportsHeatmap() {
    const container = document.getElementById("conflictHeatmap");
    const table = document.getElementById("conflictsTable");
    if (!container || !table) {
        return;
    }

    const rows = Array.from(table.querySelectorAll("tbody tr"));
    if (!rows.length) {
        container.textContent = "No conflict data available.";
        return;
    }

    const dayOrder = {
        mon: 1, monday: 1,
        tue: 2, tuesday: 2,
        wed: 3, wednesday: 3,
        thu: 4, thursday: 4,
        fri: 5, friday: 5,
        sat: 6, saturday: 6,
        sun: 7, sunday: 7
    };

    const dataMap = {};
    const daysSet = new Set();
    const periodsSet = new Set();

    rows.forEach(function (row) {
        const day = row.children[3]?.innerText.trim();
        const periodRaw = row.children[4]?.innerText.trim();
        const countRaw = row.children[6]?.innerText.trim();
        const period = Number(periodRaw);
        const count = Number(countRaw);
        if (!day || !Number.isFinite(period)) {
            return;
        }
        daysSet.add(day);
        periodsSet.add(period);
        const key = day + "__" + period;
        if (!dataMap[key]) {
            dataMap[key] = { count: 0, rows: [] };
        }
        dataMap[key].count += Number.isFinite(count) ? count : 0;
        dataMap[key].rows.push({
            type: row.children[0]?.innerText.trim(),
            department: row.children[1]?.innerText.trim(),
            slot: row.children[2]?.innerText.trim(),
            day: day,
            period: period,
            entity: row.children[5]?.innerText.trim(),
            count: count
        });
    });

    const days = Array.from(daysSet).sort(function (a, b) {
        const ak = dayOrder[a.toLowerCase()] || 99;
        const bk = dayOrder[b.toLowerCase()] || 99;
        if (ak !== bk) {
            return ak - bk;
        }
        return a.localeCompare(b);
    });
    const periods = Array.from(periodsSet).sort(function (a, b) { return a - b; });

    const maxCount = Object.values(dataMap).reduce(function (max, item) {
        return Math.max(max, item.count);
    }, 0);

    container.innerHTML = "";
    container.style.gridTemplateColumns = "140px repeat(" + periods.length + ", minmax(64px, 1fr))";

    const corner = document.createElement("div");
    corner.className = "heatmap-label";
    corner.textContent = "Day / Period";
    container.appendChild(corner);

    periods.forEach(function (p) {
        const label = document.createElement("div");
        label.className = "heatmap-label";
        label.textContent = "P" + p;
        container.appendChild(label);
    });

    days.forEach(function (day) {
        const dayLabel = document.createElement("div");
        dayLabel.className = "heatmap-label heatmap-label--day";
        dayLabel.textContent = day;
        container.appendChild(dayLabel);

        periods.forEach(function (period) {
            const key = day + "__" + period;
            const entry = dataMap[key];
            const cell = document.createElement("button");
            cell.type = "button";
            cell.className = "heatmap-cell";
            const count = entry ? entry.count : 0;
            cell.textContent = count ? String(count) : "0";
            if (!count) {
                cell.classList.add("heatmap-cell--empty");
            } else {
                const alpha = maxCount ? (0.12 + (count / maxCount) * 0.58) : 0.12;
                cell.style.setProperty("--heat-alpha", alpha.toFixed(2));
                cell.title = "Conflicts: " + count;
                cell.addEventListener("click", function () {
                    showHeatmapDetails(day, period, entry.rows);
                });
            }
            container.appendChild(cell);
        });
    });

    function showHeatmapDetails(day, period, details) {
        const modal = document.getElementById("conflictHeatmapModal");
        const title = document.getElementById("conflictHeatmapTitle");
        const list = document.getElementById("conflictHeatmapList");
        if (!modal || !title || !list) {
            return;
        }
        title.textContent = day + " · Period " + period;
        list.innerHTML = "";
        details.forEach(function (item) {
            const li = document.createElement("li");
            li.textContent = item.type + " · " + item.department + " · " + item.entity + " (x" + item.count + ")";
            list.appendChild(li);
        });
        openModal("conflictHeatmapModal");
    }
}
