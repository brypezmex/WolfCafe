package edu.ncsu.csc326.wolfcafe.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;

/**
 * Sets up the database with roles and default users. Based on code from
 * https://github.com/Baeldung/spring-security-registration/blob/master/src/main/java/com/baeldung/spring/SetupDataLoader.java
 */
@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    /** True if already setup */
    private boolean         alreadySetup = false;

    /** Link to RoleRepository */
    @Autowired
    private RoleRepository  roleRepository;

    /** Link to UserRepository */
    @Autowired
    private UserRepository  userRepository;

    /** Encodes passwords */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Password shared by all three seeded accounts, from
     * application.properties. One value keeps the demo accounts predictable;
     * override it per deployment with the DEFAULT_PASSWORD environment
     * variable.
     */
    @Value ( "${app.default-user-password}" )
    private String          defaultUserPassword;

    /**
     * When the application loads and the context is refreshed this method will
     * run and create the admin user role and any other user roles defined in
     * the Roles.UserRoles enum.
     *
     * @param event
     *            the context refreshed event
     */
    @Override
    @Transactional
    public void onApplicationEvent ( final ContextRefreshedEvent event ) {
        if ( alreadySetup ) {
            return;
        }

        seedDefaults();

        alreadySetup = true;
    }

    /**
     * Creates the default roles and the default admin, staff, and customer
     * users if they are not already present. Safe to call repeatedly, and used
     * both at startup and after the nightly database reset.
     */
    @Transactional
    public void seedDefaults () {
        final Role adminRole = createRoleIfNotFound( Roles.ROLE_ADMIN );
        Role staffRole = null;
        Role customerRole = null;
        for ( final Roles.UserRoles role : Roles.UserRoles.values() ) {
            final Role createdRole = createRoleIfNotFound( role.toString() );
            if ( Roles.UserRoles.ROLE_STAFF == role ) {
                staffRole = createdRole;
            }
            else if ( Roles.UserRoles.ROLE_CUSTOMER == role ) {
                customerRole = createdRole;
            }
        }

        createUserIfNotFound( "Admin User", "admin", "admin@wolfcafe.edu", defaultUserPassword,
                new ArrayList<>( Arrays.asList( adminRole ) ) );
        createUserIfNotFound( "Staff User", "staff", "staff@wolfcafe.edu", defaultUserPassword,
                new ArrayList<>( Arrays.asList( staffRole ) ) );
        createUserIfNotFound( "Customer User", "customer", "customer@wolfcafe.edu", defaultUserPassword,
                new ArrayList<>( Arrays.asList( customerRole ) ) );
    }

    /**
     * Creates the role with the given name.
     *
     * @param name
     *            role name
     * @return created role
     */
    @Transactional
    public Role createRoleIfNotFound ( final String name ) {
        Role role = roleRepository.findByName( name );
        if ( role == null ) {
            role = new Role();
            role.setName( name );
        }
        role = roleRepository.save( role );
        return role;

    }

    /**
     * Creates a user with the given information.
     *
     * @param name
     *            user's name
     * @param username
     *            user's username
     * @param email
     *            user's email
     * @param rawPassword
     *            user's plain text password before encoding
     * @param roles
     *            user's roles
     * @return created user
     */
    @Transactional
    public User createUserIfNotFound ( final String name, final String username, final String email,
            final String rawPassword, final Collection<Role> roles ) {
        final Optional<User> returnedUser = userRepository.findByUsernameOrEmail( username, email );

        if ( returnedUser.isEmpty() ) {
            final User user = new User();
            user.setName( name );
            user.setUsername( username );
            user.setEmail( email );
            user.setPassword( passwordEncoder.encode( rawPassword ) );
            user.setRoles( roles );
            userRepository.save( user );
            return user;
        }
        return returnedUser.get();
    }

}
