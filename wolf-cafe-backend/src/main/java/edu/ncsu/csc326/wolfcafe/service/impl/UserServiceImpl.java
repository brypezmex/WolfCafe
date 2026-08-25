package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.config.Roles;
import edu.ncsu.csc326.wolfcafe.dto.EditUserDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;
import edu.ncsu.csc326.wolfcafe.service.UserService;
import lombok.AllArgsConstructor;

/**
 * Administrative user-management service implementation for UC2.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    /** Logger for audit-style application logging */
    private static final Logger LOGGER = LoggerFactory.getLogger( UserServiceImpl.class );

    /** Repository for users */
    private UserRepository      userRepository;

    /** Repository for roles */
    private RoleRepository      roleRepository;

    /** Password encoder for new staff accounts */
    private PasswordEncoder     passwordEncoder;

    /** Screens user-supplied text for profanity */
    private ProfanityFilterService profanityFilterService;

    /**
     * Returns all users in the system as response DTOs.
     *
     * @return list of all users and their basic account information
     */
    @Override
    public List<UserDto> getAllUsers () {
        return userRepository.findAll().stream().map( this::mapToUserDto ).toList();
    }

    /**
     * Creates a new staff user account.
     *
     * Validates required fields, checks for duplicate username and email,
     * assigns the staff role, hashes the password, and records a success log
     * entry for UC2 auditing.
     *
     * @param registerDto
     *            request data for the new staff account
     * @return DTO for the created staff user
     * @throws WolfCafeAPIException
     *             if a required field is missing, the username/email already
     *             exists, or the staff role is not configured
     */
    @Override
    public UserDto createStaffUser ( final RegisterDto registerDto ) {
        final String name = requireNonBlank( registerDto.getName(), "Name is required." );
        final String username = requireNonBlank( registerDto.getUsername(), "Username is required." );
        final String email = requireNonBlank( registerDto.getEmail(), "Email is required." );
        final String password = requireNonBlank( registerDto.getPassword(), "Password is required." );

        validateLanguage( name, username, email );

        if ( userRepository.existsByUsername( username ) ) {
            logFailure( "CREATE_STAFF", "duplicate username" );
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Username already exists." );
        }
        if ( userRepository.existsByEmail( email ) ) {
            logFailure( "CREATE_STAFF", "duplicate email" );
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Email already exists." );
        }

        final Role staffRole = roleRepository.findByName( Roles.UserRoles.ROLE_STAFF.toString() );
        if ( staffRole == null ) {
            logFailure( "CREATE_STAFF", "staff role not found" );
            throw new WolfCafeAPIException( HttpStatus.INTERNAL_SERVER_ERROR, "Staff role is not configured." );
        }

        final User user = new User();
        user.setName( name );
        user.setUsername( username );
        user.setEmail( email );
        user.setPassword( passwordEncoder.encode( password ) );
        user.setRoles( new ArrayList<>( List.of( staffRole ) ) );

        final User savedUser = userRepository.save( user );
        LOGGER.info( "username={} role=Admin action=CREATE_STAFF newUsername={}", currentActorUsername(),
                savedUser.getUsername() );
        return mapToUserDto( savedUser );
    }

    /**
     * Updates an existing staff or customer account.
     *
     * Rejects attempts to edit admin accounts through UC2, validates required
     * fields, checks for duplicate username/email conflicts, tracks which
     * fields changed, and records an audit-style log entry.
     *
     * @param id
     *            id of the user to edit
     * @param editUserDto
     *            updated account information
     * @return DTO for the updated user
     * @throws ResourceNotFoundException
     *             if the user does not exist
     * @throws WolfCafeAPIException
     *             if the target is an admin account or the updated username or
     *             email conflicts with another user
     */
    @Override
    public UserDto editUser ( final Long id, final EditUserDto editUserDto ) {
        final User user = userRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id " + id ) );

        rejectIfAdminTarget( user, "EDIT_USER" );

        final String newName = requireNonBlank( editUserDto.getName(), "Name is required." );
        final String newUsername = requireNonBlank( editUserDto.getUsername(), "Username is required." );
        final String newEmail = requireNonBlank( editUserDto.getEmail(), "Email is required." );

        validateLanguage( newName, newUsername, newEmail );

        if ( userRepository.existsByUsernameAndIdNot( newUsername, id ) ) {
            logFailure( "EDIT_USER", "duplicate username" );
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Username already exists." );
        }
        if ( userRepository.existsByEmailAndIdNot( newEmail, id ) ) {
            logFailure( "EDIT_USER", "duplicate email" );
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Email already exists." );
        }

        final List<String> changedFields = new ArrayList<>();
        if ( !newName.equals( user.getName() ) ) {
            user.setName( newName );
            changedFields.add( "name" );
        }
        if ( !newUsername.equals( user.getUsername() ) ) {
            user.setUsername( newUsername );
            changedFields.add( "username" );
        }
        if ( !newEmail.equals( user.getEmail() ) ) {
            user.setEmail( newEmail );
            changedFields.add( "email" );
        }

        final User savedUser = userRepository.save( user );
        LOGGER.info( "username={} role=Admin action=EDIT_USER targetUsername={} fieldsChanged={}",
                currentActorUsername(), savedUser.getUsername(), String.join( ",", changedFields ) );
        return mapToUserDto( savedUser );
    }

    /**
     * Deletes an existing staff or customer account.
     *
     * Rejects attempts to delete admin accounts through UC2, removes the user's
     * role associations from the join table, deletes the user record, and logs
     * the successful delete action.
     *
     * @param id
     *            id of the user to delete
     * @throws ResourceNotFoundException
     *             if the user does not exist
     * @throws WolfCafeAPIException
     *             if the target user is an admin account
     */
    @Override
    public void deleteUser ( final Long id ) {
        final User user = userRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id " + id ) );

        rejectIfAdminTarget( user, "DELETE_USER" );

        final String targetUsername = user.getUsername();

        user.setRoles( new java.util.ArrayList<>() );
        userRepository.save( user );
        userRepository.delete( user );

        LOGGER.info( "username={} role=Admin action=DELETE_USER targetUsername={}", currentActorUsername(),
                targetUsername );
    }

    /**
     * Maps a persistent User entity into a response DTO.
     *
     * @param user
     *            entity to map
     * @return mapped response
     */
    private UserDto mapToUserDto ( final User user ) {
        final String role = user.getRoles().stream().findFirst().map( Role::getName ).orElse( "" );
        return new UserDto( user.getId(), user.getName(), user.getUsername(), user.getEmail(), role );
    }

    /**
     * Screens the account's free-text fields for profanity.
     *
     * @param name
     *            user's display name
     * @param username
     *            user's username
     * @param email
     *            user's email
     * @throws WolfCafeAPIException
     *             if any field contains a blocked term
     */
    private void validateLanguage ( final String name, final String username, final String email ) {
        profanityFilterService.validate( "Name", name );
        profanityFilterService.validate( "Username", username );
        profanityFilterService.validate( "Email", email );
    }

    /**
     * Ensures a field is present and trimmed.
     *
     * @param value
     *            raw value
     * @param message
     *            validation error message
     * @return trimmed value
     */
    private String requireNonBlank ( final String value, final String message ) {
        if ( value == null || value.trim().isEmpty() ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, message );
        }
        return value.trim();
    }

    /**
     * Rejects edit/delete actions on admin accounts because UC2 only manages
     * staff and customer accounts.
     *
     * @param user
     *            target user
     * @param action
     *            attempted action
     */
    private void rejectIfAdminTarget ( final User user, final String action ) {
        if ( hasRole( user, Roles.ROLE_ADMIN ) ) {
            logFailure( action, "cannot modify admin account" );
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST,
                    "Admin accounts cannot be edited or deleted through this endpoint." );
        }
    }

    /**
     * Checks whether the given user has the specified role.
     *
     * @param user
     *            target user
     * @param roleName
     *            role name to check
     * @return true if the user has the role
     */
    private boolean hasRole ( final User user, final String roleName ) {
        return user.getRoles().stream().map( Role::getName ).anyMatch( roleName::equals );
    }

    /**
     * Gets the username of the current authenticated admin actor.
     *
     * @return current username or "unknown"
     */
    private String currentActorUsername () {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ( authentication == null || authentication.getName() == null ) {
            return "unknown";
        }
        return authentication.getName();
    }

    /**
     * Logs a failed UC2 action with a human-readable reason.
     *
     * @param action
     *            attempted action
     * @param reason
     *            reason for failure
     */
    private void logFailure ( final String action, final String reason ) {
        LOGGER.warn( "username={} action=USER_MGMT_FAILED attemptedAction={} reason={}", currentActorUsername(), action,
                reason );
    }
}
