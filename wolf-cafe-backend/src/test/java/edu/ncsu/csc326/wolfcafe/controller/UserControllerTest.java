package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.dto.EditUserDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.service.UserService;

/**
 * Tests the UserController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    /** Mocked MVC */
    @Autowired
    private MockMvc                   mvc;

    /** Mocked UserService */
    @MockitoBean
    private UserService               userService;

    /** Object mapper */
    private static final ObjectMapper MAPPER   = new ObjectMapper();

    /** API path */
    private static final String       API_PATH = "/api/users";

    /** Encoding */
    private static final String       ENCODING = "utf-8";

    /**
     * Tests getting all users as an admin.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testGetAllUsers () throws Exception {
        UserDto admin = new UserDto( 1L, "Admin User", "admin", "admin@wolfcafe.edu", "ROLE_ADMIN" );
        UserDto staff = new UserDto( 2L, "Staff User", "staff", "staff@wolfcafe.edu", "ROLE_STAFF" );
        UserDto customer = new UserDto( 3L, "Customer User", "customer", "customer@wolfcafe.edu", "ROLE_CUSTOMER" );

        List<UserDto> users = Arrays.asList( admin, staff, customer );

        Mockito.when( userService.getAllUsers() ).thenReturn( users );

        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$", Matchers.hasSize( 3 ) ) )
                .andExpect( jsonPath( "$[0].username", Matchers.equalTo( "admin" ) ) )
                .andExpect( jsonPath( "$[1].username", Matchers.equalTo( "staff" ) ) )
                .andExpect( jsonPath( "$[2].username", Matchers.equalTo( "customer" ) ) );
    }

    /**
     * Tests trying to get users without logging in.
     *
     * @throws Exception
     *             if error
     */
    @Test
    public void testGetAllUsersUnauthorized () throws Exception {
        mvc.perform( get( API_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isUnauthorized() );
    }

    /**
     * Tests creating a staff user as an admin.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testCreateStaff () throws Exception {
        RegisterDto registerDto = new RegisterDto( "Staff Two", "staff2", "staff2@ncsu.edu", "temp123" );
        UserDto createdUser = new UserDto( 4L, "Staff Two", "staff2", "staff2@ncsu.edu", "ROLE_STAFF" );

        Mockito.when( userService.createStaffUser( ArgumentMatchers.any() ) ).thenReturn( createdUser );

        String json = MAPPER.writeValueAsString( registerDto );

        mvc.perform( post( API_PATH + "/staff" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.username", Matchers.equalTo( "staff2" ) ) )
                .andExpect( jsonPath( "$.email", Matchers.equalTo( "staff2@ncsu.edu" ) ) )
                .andExpect( jsonPath( "$.role", Matchers.equalTo( "ROLE_STAFF" ) ) );
    }

    /**
     * Tests creating a staff user without authorization.
     *
     * @throws Exception
     *             if error
     */
    @Test
    public void testCreateStaffUnauthorized () throws Exception {
        RegisterDto registerDto = new RegisterDto( "Staff Two", "staff2", "staff2@ncsu.edu", "temp123" );
        String json = MAPPER.writeValueAsString( registerDto );

        mvc.perform( post( API_PATH + "/staff" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isUnauthorized() );
    }

    /**
     * Tests editing a user as an admin.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testEditUser () throws Exception {
        EditUserDto editUserDto = new EditUserDto( "Customer User", "customer", "customer_updated@wolfcafe.edu" );
        UserDto updatedUser = new UserDto( 3L, "Customer User", "customer", "customer_updated@wolfcafe.edu",
                "ROLE_CUSTOMER" );

        Mockito.when( userService.editUser( ArgumentMatchers.eq( 3L ), ArgumentMatchers.any() ) )
                .thenReturn( updatedUser );

        String json = MAPPER.writeValueAsString( editUserDto );

        mvc.perform( put( API_PATH + "/3" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.username", Matchers.equalTo( "customer" ) ) )
                .andExpect( jsonPath( "$.email", Matchers.equalTo( "customer_updated@wolfcafe.edu" ) ) );
    }

    /**
     * Tests deleting a user as an admin.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testDeleteUser () throws Exception {
        mvc.perform( delete( API_PATH + "/2" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( content().string( "User deleted successfully." ) );
    }
}
