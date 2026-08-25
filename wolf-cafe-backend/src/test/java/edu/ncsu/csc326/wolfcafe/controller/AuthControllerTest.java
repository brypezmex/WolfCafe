package edu.ncsu.csc326.wolfcafe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;

/**
 * Tests the authorization controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    /** Shared password for the seeded accounts, from application.properties */
    @Value ( "${app.default-user-password}" )
    private String  defaultUserPassword;

    /** Mocked MVC for testing */
    @Autowired
    private MockMvc mvc;

    /**
     * Tests logging in as an admin user.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testLoginAdmin () throws Exception {
        LoginDto loginDto = new LoginDto( "admin", defaultUserPassword );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_ADMIN" ) );
    }

    /**
     * Tests creating a customer user and logging in.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testCreateCustomerAndLogin () throws Exception {
        RegisterDto registerDto = new RegisterDto( "Jordan Estes", "jestes", "vitae.erat@yahoo.edu", "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() ).andExpect( content().string( "User registered successfully." ) );

        LoginDto loginDto = new LoginDto( "jestes", "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_CUSTOMER" ) );
    }

    /**
     * Tests logging in as the seeded staff user.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testLoginStaff () throws Exception {
        LoginDto loginDto = new LoginDto( "staff", defaultUserPassword );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_STAFF" ) );
    }

    /**
     * Tests that invalid credentials return unauthorized.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testLoginInvalidPassword () throws Exception {
        LoginDto loginDto = new LoginDto( "admin", "wrong-password" );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isUnauthorized() );
    }

}
