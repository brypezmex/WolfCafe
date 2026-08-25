package edu.ncsu.csc326.wolfcafe.service;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.dto.EditUserDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;

/**
 * Administrative user-management service for UC2.
 */
public interface UserService {

    /**
     * Returns all users in the system.
     *
     * @return all users with their roles
     */
    List<UserDto> getAllUsers ();

    /**
     * Creates a new staff user.
     *
     * @param registerDto
     *            information for the new staff user
     * @return created staff user
     */
    UserDto createStaffUser ( RegisterDto registerDto );

    /**
     * Edits an existing staff or customer user.
     *
     * @param id
     *            id of the user to edit
     * @param editUserDto
     *            updated values
     * @return updated user
     */
    UserDto editUser ( Long id, EditUserDto editUserDto );

    /**
     * Deletes an existing staff or customer user.
     *
     * @param id
     *            id of the user to delete
     */
    void deleteUser ( Long id );
}
