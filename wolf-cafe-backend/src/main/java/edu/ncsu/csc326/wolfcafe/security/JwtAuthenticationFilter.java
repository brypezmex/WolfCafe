package edu.ncsu.csc326.wolfcafe.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Checks user's JWT tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Token provider. */
    private JwtTokenProvider   jwtTokenProvider;

    /** Service for UserDetails. */
    private UserDetailsService userDetailsService;

    /**
     * Constructs the authentication filter.
     *
     * @param jwtTokenProvider
     *            token provider
     * @param userDetailsService
     *            service for UserDetails
     */
    public JwtAuthenticationFilter ( JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Loads the user given the token.
     *
     * @param request
     *            request from client
     * @param response
     *            response for the request
     * @param filterChain
     *            permissions
     * @throws ServletException
     *             if servlet error
     * @throws IOException
     *             if IO error
     */
    @Override
    protected void doFilterInternal ( HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain ) throws ServletException, IOException {

        String path = request.getServletPath();

        /*
         * Do not try to authenticate preflight requests or auth endpoints.
         * Login/register are public and should not be blocked by JWT parsing.
         */
        if ( "OPTIONS".equalsIgnoreCase( request.getMethod() ) || path.startsWith( "/api/auth" ) ) {
            filterChain.doFilter( request, response );
            return;
        }

        try {
            String token = getTokenFromRequest( request );

            if ( StringUtils.hasText( token ) && jwtTokenProvider.validateToken( token )
                    && SecurityContextHolder.getContext().getAuthentication() == null ) {

                String username = jwtTokenProvider.getUsername( token );

                UserDetails userDetails = userDetailsService.loadUserByUsername( username );

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities() );

                authenticationToken.setDetails( new WebAuthenticationDetailsSource().buildDetails( request ) );

                SecurityContextHolder.getContext().setAuthentication( authenticationToken );
            }
        }
        catch ( Exception ex ) {
            /*
             * Do not crash the whole request if the token is malformed or
             * expired. The request will continue without authentication and
             * Spring Security will return 401/403 when the endpoint requires
             * authentication.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter( request, response );
    }

    /**
     * Extracts bearer token from Authorization header.
     *
     * @param request
     *            HTTP request
     * @return JWT token, or null if not present
     */
    private String getTokenFromRequest ( HttpServletRequest request ) {
        String bearerToken = request.getHeader( "Authorization" );

        if ( StringUtils.hasText( bearerToken ) && bearerToken.startsWith( "Bearer " ) ) {
            return bearerToken.substring( 7 );
        }

        return null;
    }
}