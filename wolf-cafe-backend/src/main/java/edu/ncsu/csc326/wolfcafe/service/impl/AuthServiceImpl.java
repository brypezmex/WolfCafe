package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;
import edu.ncsu.csc326.wolfcafe.service.AuthService;
import edu.ncsu.csc326.wolfcafe.service.ProfanityFilterService;
import lombok.AllArgsConstructor;

/**
 * Implemented AuthService.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** Logger for login auditing */
    private static final Logger   LOGGER = LoggerFactory.getLogger( AuthServiceImpl.class );

    /** User repository */
    private UserRepository        userRepository;
    /** Role repository */
    private RoleRepository        roleRepository;
    /** Password encoder object */
    private PasswordEncoder       passwordEncoder;
    /** Authentication manager */
    private AuthenticationManager authenticationManager;
    /** JWT Token provider for working with user tokens */
    private JwtTokenProvider      jwtTokenProvider;
    /** Screens user-supplied text for profanity */
    private ProfanityFilterService profanityFilterService;

    /**
     * Registers the given customer user.
     *
     * @param registerDto
     *            new user information
     * @return message for success or failure
     */
    @Override
    public String register ( final RegisterDto registerDto ) {
        final String name = requireNonBlank( registerDto.getName(), "Name is required." );
        final String username = requireNonBlank( registerDto.getUsername(), "Username is required." );
        final String email = requireNonBlank( registerDto.getEmail(), "Email is required." );
        final String password = requireNonBlank( registerDto.getPassword(), "Password is required." );

        profanityFilterService.validate( "Name", name );
        profanityFilterService.validate( "Username", username );
        profanityFilterService.validate( "Email", email );

        if ( userRepository.existsByUsername( username ) ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Username already exists." );
        }

        if ( userRepository.existsByEmail( email ) ) {
            throw new WolfCafeAPIException( HttpStatus.BAD_REQUEST, "Email already exists." );
        }

        final User user = new User();
        user.setName( name );
        user.setUsername( username );
        user.setEmail( email );
        user.setPassword( passwordEncoder.encode( password ) );

        final Set<Role> roles = new HashSet<>();
        final Role userRole = roleRepository.findByName( "ROLE_CUSTOMER" );
        roles.add( userRole );

        user.setRoles( roles );

        userRepository.save( user );

        return "User registered successfully.";
    }

    /**
     * Logs in the given user.
     *
     * @param loginDto
     *            username/email and password
     * @return response with authenticated user
     */
    @Override
    public JwtAuthResponse login ( final LoginDto loginDto ) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken( loginDto.getUsernameOrEmail(), loginDto.getPassword() ) );

        SecurityContextHolder.getContext().setAuthentication( authentication );

        final String token = jwtTokenProvider.generateToken( authentication );

        final Optional<User> userOptional = userRepository.findByUsernameOrEmail( loginDto.getUsernameOrEmail(),
                loginDto.getUsernameOrEmail() );

        String role = null;
        String username = loginDto.getUsernameOrEmail();
        if ( userOptional.isPresent() ) {
            final User loggedInUser = userOptional.get();
            username = loggedInUser.getUsername();
            final Optional<Role> optionalRole = loggedInUser.getRoles().stream().findFirst();

            if ( optionalRole.isPresent() ) {
                final Role userRole = optionalRole.get();
                role = userRole.getName();
            }
        }

        LOGGER.info( "username={} role={} action=LOGIN", username, role );

        final JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setRole( role );
        jwtAuthResponse.setAccessToken( token );

        return jwtAuthResponse;
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
}
