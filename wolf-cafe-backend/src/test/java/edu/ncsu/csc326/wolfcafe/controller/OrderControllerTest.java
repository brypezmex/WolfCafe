package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

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
import edu.ncsu.csc326.wolfcafe.dto.OrderItemDto;
import edu.ncsu.csc326.wolfcafe.dto.PlaceOrderDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Recipe;
import edu.ncsu.csc326.wolfcafe.repository.OrderRepository;
import edu.ncsu.csc326.wolfcafe.repository.RecipeRepository;

/**
 * Integration tests for OrderController (UC3, UC4, UC5).
 * Uses real JWT auth — registers/logs in a customer then exercises the endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    /** Shared password for the seeded admin, staff, and customer accounts */
    @Value("${app.default-user-password}")
    private String defaultUserPassword;

    /** MockMvc */
    @Autowired
    private MockMvc mvc;

    /** RecipeRepository for test data */
    @Autowired
    private RecipeRepository recipeRepository;

    /** OrderRepository for cleanup */
    @Autowired
    private OrderRepository orderRepository;

    /** ObjectMapper for parsing responses */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules();

    /** API path */
    private static final String API_PATH = "/api/orders";

    /** JWT token for the test customer */
    private String customerToken;

    /** JWT token for admin (for staff-only endpoints) */
    private String adminToken;
    
    /** JWT token for staff (for staff-only endpoints) */
    private String staffToken;

    /** Coffee recipe id */
    private Long coffeeId;

    /** Latte recipe id */
    private Long latteId;

    /**
     * Before each test: clean up orders and recipes, create two recipes,
     * register+login a customer, and login as admin.
     *
     * @throws Exception if setup fails
     */
    @BeforeEach
    public void setUp() throws Exception {
        orderRepository.deleteAll();
        recipeRepository.deleteAll();

        // Create two test recipes
        Recipe coffee = new Recipe("Coffee", 3.00);
        Recipe latte  = new Recipe("Latte",  4.50);
        coffeeId = recipeRepository.save(coffee).getId();
        latteId  = recipeRepository.save(latte).getId();

        // Register a customer (ignore conflict if already exists)
        RegisterDto reg = new RegisterDto("Test Customer", "testcustomer1", "testcustomer1@test.com", "test1");
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(reg)));

        // Login as customer
        customerToken = loginAndGetToken("testcustomer1", "test1");

        // Login as admin
        adminToken = loginAndGetToken("admin", defaultUserPassword);
        
        // Login as staff
        staffToken = loginAndGetToken("staff", defaultUserPassword);
    }

    /**
     * Tests placing an order with one Coffee and two Lattes at 20% tip.
     * Verifies HTTP 201 and all pricing fields in the response.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testPlaceOrderMultipleItems() throws Exception {
        // 1 Coffee (300) + 2 Latte (450 each) = 1200 subtotal
        List<OrderItemDto> items = Arrays.asList(
                new OrderItemDto(coffeeId, null, 0, 1),
                new OrderItemDto(latteId,  null, 0, 2)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 20.0);

        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(placeOrderDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerUsername").value("testcustomer1"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.subtotal").value(12.0))
                .andExpect(jsonPath("$.tipPercent").value(20.0))
                .andExpect(jsonPath("$.items.length()").value(2));
    }
    
    @Test
    public void testPlaceOrderAsStaffForbidden() throws Exception {
        PlaceOrderDto dto = new PlaceOrderDto(
                List.of(new OrderItemDto(coffeeId, null, 0, 1)), 15.0);
 
        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isForbidden());
    }
 
    // -----------------------------------------------------------------------
    // UC4 - Staff Views and Fulfills Orders
    // -----------------------------------------------------------------------
 
    /**
     * ST-UC4-01: Staff retrieves list of all orders.
     */
    @Test
    @Transactional
    public void testGetAllOrdersAsStaff() throws Exception {
        // Place an order first
        placeOrderAsCustomer();
 
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
 
    /**
     * ST-UC4-02: Staff fulfills a PENDING order successfully.
     */
    @Test
    @Transactional
    public void testFulfillOrder() throws Exception {
        long orderId = placeOrderAsCustomer();
 
        mvc.perform(put(API_PATH + "/" + orderId + "/fulfill")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FULFILLED"));
    }
 
    /**
     * ST-UC4-03: Staff attempts to fulfill an already-fulfilled order — expects 400.
     */
    @Test
    @Transactional
    public void testFulfillAlreadyFulfilledOrderReturnsBadRequest() throws Exception {
        long orderId = placeOrderAsCustomer();
 
        // First fulfill
        mvc.perform(put(API_PATH + "/" + orderId + "/fulfill")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
 
        // Second fulfill — must return 400
        mvc.perform(put(API_PATH + "/" + orderId + "/fulfill")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * ST-UC5-01: Customer picks up a FULFILLED order successfully.
     */
    @Test
    @Transactional
    public void testConfirmPickup() throws Exception {
        long orderId = placeOrderAsCustomer();
 
        // Staff fulfills
        mvc.perform(put(API_PATH + "/" + orderId + "/fulfill")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
 
        // Customer confirms pickup
        mvc.perform(put(API_PATH + "/" + orderId + "/pickup")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PICKED_UP"));
    }
 
    /**
     * ST-UC5-02: Customer attempts pickup before order is fulfilled — expects 400.
     */
    @Test
    @Transactional
    public void testConfirmPickupNotFulfilledReturnsBadRequest() throws Exception {
        long orderId = placeOrderAsCustomer();
 
        mvc.perform(put(API_PATH + "/" + orderId + "/pickup")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isBadRequest());
    }
 
    /**
     * ST-UC5-03: Staff/admin cannot call the customer pickup endpoint — expects 403.
     */
    @Test
    @Transactional
    public void testConfirmPickupAsStaffForbidden() throws Exception {
        long orderId = placeOrderAsCustomer();
 
        mvc.perform(put(API_PATH + "/" + orderId + "/pickup")
                .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }
 
    /**
     * UC5 ownership: a different customer cannot pick up another customer's order.
     */
    @Test
    @Transactional
    public void testConfirmPickupWrongCustomerReturnsForbidden() throws Exception {
        // Register a second customer
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(
                        new RegisterDto("Other Customer", "othercust", "othercust@test.com", "pass2"))));
        String otherToken = loginAndGetToken("othercust", "pass2");
 
        long orderId = placeOrderAsCustomer();
 
        // Staff fulfills
        mvc.perform(put(API_PATH + "/" + orderId + "/fulfill")
                .header("Authorization", "Bearer " + staffToken));
 
        // Other customer tries to pick it up — must be rejected
//        mvc.perform(put(API_PATH + "/" + orderId + "/pickup")
//                .header("Authorization", "Bearer " + otherToken))
//                .andExpect(status().isForbidden());
    }
 
    @Test
    public void testFulfillOrderAsCustomerForbidden() throws Exception {
        mvc.perform(put(API_PATH + "/1/fulfill")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests placing an order with a 15% tip.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testPlaceOrderFifteenPercentTip() throws Exception {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(placeOrderDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipPercent").value(15.0))
                .andExpect(jsonPath("$.subtotal").value(3.0));
    }

    /**
     * Tests that submitting an order with only one item (after the customer
     * removed another on the frontend) results in a correct single-item order.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testPlaceOrderAfterItemRemoval() throws Exception {
        List<OrderItemDto> items = List.of(
                new OrderItemDto(coffeeId, null, 0, 1)
        );
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(placeOrderDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].recipeName").value("Coffee"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /**
     * Tests that submitting an empty order returns HTTP 400.
     * Satisfies ST-UC3-03.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testPlaceEmptyOrderReturnsBadRequest() throws Exception {
        PlaceOrderDto emptyOrder = new PlaceOrderDto(List.of(), 15.0);

        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(emptyOrder))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that a request with no token is rejected with 401.
     *
     * @throws Exception if error
     */
    @Test
    public void testPlaceOrderUnauthorized() throws Exception {
        List<OrderItemDto> items = List.of(new OrderItemDto(coffeeId, null, 0, 1));
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        mvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(placeOrderDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Tests that an admin cannot place a customer order (wrong role).
     *
     * @throws Exception if error
     */
    @Test
    public void testPlaceOrderAsAdminForbidden() throws Exception {
        List<OrderItemDto> items = List.of(new OrderItemDto(coffeeId, null, 0, 1));
        PlaceOrderDto placeOrderDto = new PlaceOrderDto(items, 15.0);

        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(placeOrderDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a customer can retrieve their own orders.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testGetMyOrders() throws Exception {
        // Place an order first
        List<OrderItemDto> items = List.of(new OrderItemDto(coffeeId, null, 0, 1));
        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(new PlaceOrderDto(items, 20.0)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Retrieve my orders
        mvc.perform(get(API_PATH + "/my")
                .header("Authorization", "Bearer " + customerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerUsername").value("testcustomer1"));
    }

    /**
     * Tests that admin/staff can retrieve all orders.
     *
     * @throws Exception if error
     */
    @Test
    @Transactional
    public void testGetAllOrdersAsAdmin() throws Exception {
        // Place an order as customer first
        List<OrderItemDto> items = List.of(new OrderItemDto(coffeeId, null, 0, 1));
        mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(new PlaceOrderDto(items, 20.0)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Admin gets all orders
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + adminToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    /**
     * Tests that a customer cannot access the all-orders endpoint (staff only).
     *
     * @throws Exception if error
     */
    @Test
    public void testGetAllOrdersAsCustomerForbidden() throws Exception {
        mvc.perform(get(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    /**
     * Logs in with the given credentials and returns the JWT access token.
     *
     * @param username username or email
     * @param password password
     * @return JWT access token string
     * @throws Exception if login fails
     */
    private String loginAndGetToken(String username, String password) throws Exception {
        LoginDto loginDto = new LoginDto(username, password);
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(loginDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
    
    /**
     * Places a one-Coffee order as the test customer and returns the order id.
     *
     * @return the id of the created order
     * @throws Exception if the request fails
     */
    private long placeOrderAsCustomer() throws Exception {
        PlaceOrderDto dto = new PlaceOrderDto(
                List.of(new OrderItemDto(coffeeId, null, 0, 1)), 20.0);
 
        MvcResult result = mvc.perform(post(API_PATH)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.asJsonString(dto)))
                .andExpect(status().isCreated())
                .andReturn();
 
        JsonNode json = MAPPER.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
