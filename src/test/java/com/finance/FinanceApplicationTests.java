package com.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.AuthDto;
import com.finance.dto.FinancialRecordDto;
import com.finance.dto.UserDto;
import com.finance.entity.RecordType;
import com.finance.entity.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FinanceApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired TestDataSeeder seeder;

    static String adminToken;
    static String viewerToken;
    static Long   recordId;

    @BeforeEach
    void setup() {
        seeder.seedAdmin();
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    @Test @Order(1)
    void register_publicUser_getsViewerRole() throws Exception {
        var req = new AuthDto.RegisterRequest();
        req.setName("Test Viewer"); req.setEmail("viewer@test.com"); req.setPassword("Test@1234");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.role").value("VIEWER"));
    }

    @Test @Order(2)
    void register_duplicateEmail_returns409() throws Exception {
        var req = new AuthDto.RegisterRequest();
        req.setName("Dup"); req.setEmail("viewer@test.com"); req.setPassword("Test@1234");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test @Order(3)
    void register_invalidEmail_returns422() throws Exception {
        var req = new AuthDto.RegisterRequest();
        req.setName("Bad"); req.setEmail("not-an-email"); req.setPassword("Test@1234");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test @Order(4)
    void login_viewer_success() throws Exception {
        var req = new AuthDto.LoginRequest();
        req.setEmail("viewer@test.com"); req.setPassword("Test@1234");

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").exists())
            .andReturn();

        var body = mapper.readTree(result.getResponse().getContentAsString());
        viewerToken = body.at("/data/token").asText();
    }

    @Test @Order(5)
    void login_admin_success() throws Exception {
        var req = new AuthDto.LoginRequest();
        req.setEmail("testadmin@finance.com"); req.setPassword("Admin@1234");

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        var body = mapper.readTree(result.getResponse().getContentAsString());
        adminToken = body.at("/data/token").asText();
    }

    @Test @Order(6)
    void login_wrongPassword_returns401() throws Exception {
        var req = new AuthDto.LoginRequest();
        req.setEmail("viewer@test.com"); req.setPassword("wrongpassword");

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(7)
    void getMe_authenticated() throws Exception {
        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("viewer@test.com"));
    }

    @Test @Order(8)
    void getMe_noToken_returns401() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    // ── Records ───────────────────────────────────────────────────────────────

    @Test @Order(9)
    void createRecord_asAdmin_success() throws Exception {
        var req = new FinancialRecordDto.CreateRequest();
        req.setAmount(new BigDecimal("2500.00"));
        req.setType(RecordType.INCOME);
        req.setCategory("Salary");
        req.setDate(LocalDate.of(2024, 6, 1));
        req.setNotes("June salary");

        MvcResult result = mvc.perform(post("/api/records")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.amount").value(2500.00))
            .andReturn();

        var body = mapper.readTree(result.getResponse().getContentAsString());
        recordId = body.at("/data/id").asLong();
    }

    @Test @Order(10)
    void createRecord_asViewer_returns403() throws Exception {
        var req = new FinancialRecordDto.CreateRequest();
        req.setAmount(new BigDecimal("100.00"));
        req.setType(RecordType.EXPENSE);
        req.setCategory("Food");
        req.setDate(LocalDate.now());

        mvc.perform(post("/api/records")
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }

    @Test @Order(11)
    void createRecord_negativeAmount_returns422() throws Exception {
        var req = new FinancialRecordDto.CreateRequest();
        req.setAmount(new BigDecimal("-50.00"));
        req.setType(RecordType.INCOME);
        req.setCategory("Test");
        req.setDate(LocalDate.now());

        mvc.perform(post("/api/records")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test @Order(12)
    void listRecords_asViewer_success() throws Exception {
        mvc.perform(get("/api/records")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test @Order(13)
    void listRecords_withTypeFilter() throws Exception {
        mvc.perform(get("/api/records?type=INCOME")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content[*].type", everyItem(is("INCOME"))));
    }

    @Test @Order(14)
    void getRecord_byId() throws Exception {
        mvc.perform(get("/api/records/" + recordId)
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(recordId));
    }

    @Test @Order(15)
    void updateRecord_asAdmin_success() throws Exception {
        var req = new FinancialRecordDto.UpdateRequest();
        req.setNotes("Updated note");
        req.setAmount(new BigDecimal("3000.00"));

        mvc.perform(patch("/api/records/" + recordId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.notes").value("Updated note"))
            .andExpect(jsonPath("$.data.amount").value(3000.00));
    }

    @Test @Order(16)
    void updateRecord_asViewer_returns403() throws Exception {
        mvc.perform(patch("/api/records/" + recordId)
                .header("Authorization", "Bearer " + viewerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"hack\"}"))
            .andExpect(status().isForbidden());
    }

    @Test @Order(17)
    void deleteRecord_asAdmin_success() throws Exception {
        mvc.perform(delete("/api/records/" + recordId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test @Order(18)
    void deletedRecord_returns404() throws Exception {
        mvc.perform(get("/api/records/" + recordId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isNotFound());
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Test @Order(19)
    void dashboard_summary() throws Exception {
        mvc.perform(get("/api/dashboard/summary")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.totalIncome").exists())
            .andExpect(jsonPath("$.data.totalExpenses").exists())
            .andExpect(jsonPath("$.data.netBalance").exists())
            .andExpect(jsonPath("$.data.recordCount").exists());
    }

    @Test @Order(20)
    void dashboard_categories() throws Exception {
        mvc.perform(get("/api/dashboard/categories")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(21)
    void dashboard_monthlyTrends() throws Exception {
        mvc.perform(get("/api/dashboard/trends/monthly?year=2024")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(22)
    void dashboard_weeklyTrends() throws Exception {
        mvc.perform(get("/api/dashboard/trends/weekly?weeks=8")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test @Order(23)
    void dashboard_recentActivity() throws Exception {
        mvc.perform(get("/api/dashboard/recent?limit=5")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @Test @Order(24)
    void listUsers_asAdmin() throws Exception {
        mvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test @Order(25)
    void listUsers_asViewer_returns403() throws Exception {
        mvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + viewerToken))
            .andExpect(status().isForbidden());
    }

    @Test @Order(26)
    void createUser_withAnalystRole_asAdmin() throws Exception {
        var req = new UserDto.CreateRequest();
        req.setName("New Analyst");
        req.setEmail("analyst2@test.com");
        req.setPassword("Analyst@1234");
        req.setRole(Role.ANALYST);

        mvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.role").value("ANALYST"));
    }
}
