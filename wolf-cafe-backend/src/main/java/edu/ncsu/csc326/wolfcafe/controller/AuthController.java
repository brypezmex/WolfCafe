package edu.ncsu.csc326.wolfcafe.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.service.AuthService;
import lombok.AllArgsConstructor;

/**
 * Controller for authentication functionality.
 */
@CrossOrigin ( "*" )
@RestController
@RequestMapping ( "/api/auth" )
@AllArgsConstructor
public class AuthController {

    /** Link to AuthService */
    private AuthService authService;

    /**
     * Registers a new customer user with the system.
     *
     * @param registerDto
     *            object with registration info
     * @return response indicating success or failure
     */
    @PostMapping ( "/register" )
    public ResponseEntity<String> register ( @RequestBody final RegisterDto registerDto ) {
        final String response = authService.register( registerDto );
        return new ResponseEntity<>( response, HttpStatus.CREATED );
    }

    /**
     * Logs in the given user.
     *
     * @param loginDto
     *            user information for login
     * @return object representing the logged in user
     */
    @PostMapping ( "/login" )
    public ResponseEntity<JwtAuthResponse> login ( @RequestBody final LoginDto loginDto ) {
        final JwtAuthResponse jwtAuthResponse = authService.login( loginDto );
        return new ResponseEntity<>( jwtAuthResponse, HttpStatus.OK );
    }
}
