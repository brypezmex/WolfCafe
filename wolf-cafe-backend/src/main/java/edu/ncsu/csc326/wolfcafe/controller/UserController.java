package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.EditUserDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.service.UserService;
import lombok.AllArgsConstructor;

/**
 * Controller for admin user-management functionality in UC2.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/users" )
@AllArgsConstructor
@PreAuthorize ( "hasRole('ADMIN')" )
public class UserController {

    /** Link to user-management service */
    private UserService userService;

    /**
     * Returns all users and their roles.
     *
     * @return all users
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers () {
        return ResponseEntity.ok( userService.getAllUsers() );
    }

    /**
     * Creates a new staff user.
     *
     * @param registerDto
     *            information for the new staff member
     * @return created staff user
     */
    @PostMapping ( "/staff" )
    public ResponseEntity<UserDto> createStaff ( @RequestBody final RegisterDto registerDto ) {
        return new ResponseEntity<>( userService.createStaffUser( registerDto ), HttpStatus.CREATED );
    }

    /**
     * Edits a staff or customer user.
     *
     * @param id
     *            id of the user to edit
     * @param editUserDto
     *            updated user data
     * @return updated user
     */
    @PutMapping ( "/{id}" )
    public ResponseEntity<UserDto> editUser ( @PathVariable final Long id,
            @RequestBody final EditUserDto editUserDto ) {
        return ResponseEntity.ok( userService.editUser( id, editUserDto ) );
    }

    /**
     * Deletes a staff or customer user.
     *
     * @param id
     *            id of the user to delete
     * @return success message
     */
    @DeleteMapping ( "/{id}" )
    public ResponseEntity<String> deleteUser ( @PathVariable final Long id ) {
        userService.deleteUser( id );
        return ResponseEntity.ok( "User deleted successfully." );
    }
}
