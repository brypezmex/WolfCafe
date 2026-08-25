package edu.ncsu.csc326.wolfcafe.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.EditUserDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;

/**
 * Tests UserServiceImpl.
 */
@SpringBootTest
public class UserServiceTest {

    /** Reference to UserService */
    @Autowired
    private UserService    userService;

    /** Reference to UserRepository */
    @Autowired
    private UserRepository userRepository;

    /**
     * Tests getting all users.
     */
    @Test
    @Transactional
    void testGetAllUsers () {
        List<UserDto> users = userService.getAllUsers();

        assertTrue( users.size() >= 3 );
        assertTrue( users.stream().anyMatch( u -> "admin".equals( u.getUsername() ) ) );
        assertTrue( users.stream().anyMatch( u -> "staff".equals( u.getUsername() ) ) );
        assertTrue( users.stream().anyMatch( u -> "customer".equals( u.getUsername() ) ) );
    }

    /**
     * Tests creating a staff user.
     */
    @Test
    @Transactional
    void testCreateStaffUser () {
        RegisterDto registerDto = new RegisterDto( "Staff Two", "staff2", "staff2@ncsu.edu", "temp123" );

        UserDto createdUser = userService.createStaffUser( registerDto );

        assertAll( "UserDto contents", () -> assertEquals( "Staff Two", createdUser.getName() ),
                () -> assertEquals( "staff2", createdUser.getUsername() ),
                () -> assertEquals( "staff2@ncsu.edu", createdUser.getEmail() ),
                () -> assertEquals( "ROLE_STAFF", createdUser.getRole() ) );
    }

    /**
     * Tests creating a staff user with a duplicate username.
     */
    @Test
    @Transactional
    void testCreateStaffUserDuplicateUsername () {
        RegisterDto registerDto = new RegisterDto( "Other Staff", "staff", "otherstaff@ncsu.edu", "temp123" );

        assertThrows( WolfCafeAPIException.class, () -> userService.createStaffUser( registerDto ) );
    }

    /**
     * Tests creating a staff user with a duplicate email.
     */
    @Test
    @Transactional
    void testCreateStaffUserDuplicateEmail () {
        RegisterDto registerDto = new RegisterDto( "Other Staff", "staff2", "staff@wolfcafe.edu", "temp123" );

        assertThrows( WolfCafeAPIException.class, () -> userService.createStaffUser( registerDto ) );
    }

    /**
     * Tests editing a user.
     */
    @Test
    @Transactional
    void testEditUser () {
        User customer = userRepository.findByUsername( "customer" ).orElseThrow();

        EditUserDto editUserDto = new EditUserDto( customer.getName(), "customer", "customer_updated@wolfcafe.edu" );

        UserDto updatedUser = userService.editUser( customer.getId(), editUserDto );

        assertAll( "UserDto contents", () -> assertEquals( "customer", updatedUser.getUsername() ),
                () -> assertEquals( "customer_updated@wolfcafe.edu", updatedUser.getEmail() ),
                () -> assertEquals( "ROLE_CUSTOMER", updatedUser.getRole() ) );
    }

    /**
     * Tests editing a user that does not exist.
     */
    @Test
    @Transactional
    void testEditUserNotFound () {
        EditUserDto editUserDto = new EditUserDto( "Nobody", "nobody", "nobody@test.com" );

        assertThrows( ResourceNotFoundException.class, () -> userService.editUser( 999999L, editUserDto ) );
    }

    /**
     * Tests deleting a created staff user.
     */
    @Test
    @Transactional
    void testDeleteUser () {
        RegisterDto registerDto = new RegisterDto( "Staff Two", "staff2", "staff2@ncsu.edu", "temp123" );
        UserDto createdUser = userService.createStaffUser( registerDto );

        userService.deleteUser( createdUser.getId() );

        Optional<User> deletedUser = userRepository.findById( createdUser.getId() );
        assertTrue( deletedUser.isEmpty() );
    }

    /**
     * Tests deleting a user that does not exist.
     */
    @Test
    @Transactional
    void testDeleteUserNotFound () {
        assertThrows( ResourceNotFoundException.class, () -> userService.deleteUser( 999999L ) );
    }

    /**
     * Tests deleting the admin user through the UC2 endpoint.
     */
    @Test
    @Transactional
    void testDeleteAdminRejected () {
        User admin = userRepository.findByUsername( "admin" ).orElseThrow();

        assertThrows( WolfCafeAPIException.class, () -> userService.deleteUser( admin.getId() ) );
    }
}
