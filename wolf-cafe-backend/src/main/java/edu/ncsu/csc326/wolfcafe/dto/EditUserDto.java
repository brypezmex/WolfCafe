package edu.ncsu.csc326.wolfcafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information used by an admin to edit an existing staff or customer user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EditUserDto {

    /** User's name */
    private String name;

    /** User's username */
    private String username;

    /** User's email */
    private String email;
}
