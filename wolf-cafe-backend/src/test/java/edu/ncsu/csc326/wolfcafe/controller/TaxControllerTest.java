package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.TaxRateDto;
import edu.ncsu.csc326.wolfcafe.repository.TaxRateRepository;
 
/**
 * Integration tests for TaxController covering UC6.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TaxControllerTest {
 
    @Value("${app.default-user-password}")
    private String adminUserPassword;
 
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private TaxRateRepository taxRateRepository;
 
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_PATH = "/api/tax";
 
    private String adminToken;
    private String customerToken;
 
    /**
     * Clear saved tax rates and get tokens before each test.
     *
     * @throws Exception if setup fails
     */
    @BeforeEach
    public void setUp() throws Exception {
        taxRateRepository.deleteAll();
 
        adminToken = loginAndGetToken("admin", adminUserPassword);
 
        // Register a customer for role-enforcement tests
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(
                        new RegisterDto("Tax Customer", "taxcust", "taxcust@test.com", "taxpass"))));
        customerToken = loginAndGetToken("taxcust", "taxpass");
    }
 
    // -----------------------------------------------------------------------
    // GET /api/tax
    // -----------------------------------------------------------------------
 
    /**
     * ST-UC6-01 step 1: Admin reads current tax rate.
     * When nothing has been saved, returns the default from properties.
     */
    @Test
    public void testGetCurrentTaxRateAsAdmin() throws Exception {
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").isNumber());
    }
 
    /**
     * Unauthenticated request to GET /api/tax returns 401.
     */
    @Test
    public void testGetCurrentTaxRateUnauthorized() throws Exception {
        mvc.perform(get(API_PATH))
                .andExpect(status().isUnauthorized());
    }
 
    // -----------------------------------------------------------------------
    // PUT /api/tax - ST-UC6-01 (success)
    // -----------------------------------------------------------------------
 
    /**
     * ST-UC6-01: Admin successfully updates the tax rate to a valid value.
     */
    @Test
    @Transactional
    public void testUpdateTaxRateSuccess() throws Exception {
        TaxRateDto dto = new TaxRateDto(0.045);
 
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(0.045));
 
        // GET should now return the updated rate
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(0.045));
    }
 
    /**
     * A rate of 0.0 is valid (no tax).
     */
    @Test
    @Transactional
    public void testUpdateTaxRateZeroIsValid() throws Exception {
        TaxRateDto dto = new TaxRateDto(0.0);
 
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(0.0));
    }
 
    // -----------------------------------------------------------------------
    // PUT /api/tax - ST-UC6-02 (invalid input)
    // -----------------------------------------------------------------------
 
    /**
     * ST-UC6-02: Admin enters a negative rate — expects 400.
     */
    @Test
    @Transactional
    public void testUpdateTaxRateNegativeReturnsBadRequest() throws Exception {
        TaxRateDto dto = new TaxRateDto(-1.5);
 
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isBadRequest());
    }
 
    /**
     * ST-UC6-02: After a rejected update the rate remains unchanged.
     */
    @Test
    @Transactional
    public void testUpdateTaxRateNegativeDoesNotChangeRate() throws Exception {
        // Set a known good rate first
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(new TaxRateDto(0.03))))
                .andExpect(status().isOk());
 
        // Attempt negative update
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(new TaxRateDto(-0.01))))
                .andExpect(status().isBadRequest());
 
        // Rate must still be 0.03
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(0.03));
    }
 
    // -----------------------------------------------------------------------
    // PUT /api/tax - ST-UC6-03 (unauthorized)
    // -----------------------------------------------------------------------
 
    /**
     * ST-UC6-03: A customer cannot update the tax rate — expects 403.
     */
    @Test
    public void testUpdateTaxRateAsCustomerForbidden() throws Exception {
        TaxRateDto dto = new TaxRateDto(0.05);
 
        mvc.perform(put(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isForbidden());
    }
 
    /**
     * ST-UC6-03: Unauthenticated request to PUT /api/tax returns 401.
     */
    @Test
    public void testUpdateTaxRateUnauthorized() throws Exception {
        TaxRateDto dto = new TaxRateDto(0.05);
 
        mvc.perform(put(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isUnauthorized());
    }
 
    // -----------------------------------------------------------------------
    // Private helper
    // -----------------------------------------------------------------------
 
    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(new LoginDto(username, password))))
                .andExpect(status().isOk())
                .andReturn();
 
        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}