package edu.ncsu.csc326.wolfcafe.service;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;

/**
 * Authorization service.
 */
public interface AuthService {
    /**
     * Registers the given customer user.
     *
     * @param registerDto
     *            new user information
     * @return message for success or failure
     */
    String register ( RegisterDto registerDto );

    /**
     * Logs in the given user.
     *
     * @param loginDto
     *            username/email and password
     * @return response with authenticated user
     */
    JwtAuthResponse login ( LoginDto loginDto );
}
