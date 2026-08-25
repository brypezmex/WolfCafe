package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Safe user data returned to the frontend for admin user management.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** User's id */
    private Long   id;

    /** User's name */
    private String name;

    /** User's username */
    private String username;

    /** User's email */
    private String email;

    /** User's primary role */
    private String role;
}
