package com.erp.web.web;

import com.erp.web.auth.UserSession;
import com.erp.web.dashboard.DashboardRepository;
import com.erp.web.dashboard.DashboardSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerApiTest {

    private MockMvc mockMvc;
    private AtomicInteger loadCalls;
    private AtomicReference<DashboardSummary> summaryRef;

    @BeforeEach
    void setUp() {
        loadCalls = new AtomicInteger(0);
        summaryRef = new AtomicReference<>(new DashboardSummary(
                0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0
        ));

        DashboardRepository repository = new DashboardRepository(null) {
            @Override
            public DashboardSummary loadSummary() {
                loadCalls.incrementAndGet();
                return summaryRef.get();
            }
        };

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DashboardController(repository))
                .build();
    }

    @Test
    void dashboardSummaryReturns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isUnauthorized());

        assertEquals(0, loadCalls.get());
    }

    @Test
    void dashboardSummaryReturnsLivePayloadWhenAuthenticated() throws Exception {
        summaryRef.set(new DashboardSummary(
                32,
                14.5,
                66.1,
                48.2,
                74,
                3,
                20,
                28,
                190
        ));

        mockMvc.perform(
                        get("/api/dashboard/summary")
                                .sessionAttr("user", new UserSession("admin", "ADMIN", null, null))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conflicts").value(32))
                .andExpect(jsonPath("$.overload").value(14.5))
                .andExpect(jsonPath("$.roomUtil").value(66.1))
                .andExpect(jsonPath("$.budget").value(48.2))
                .andExpect(jsonPath("$.score").value(74))
                .andExpect(jsonPath("$.insight").isString());

        assertEquals(1, loadCalls.get());
    }
}
