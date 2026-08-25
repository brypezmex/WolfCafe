package edu.ncsu.csc326.wolfcafe.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationEntryPoint;
import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;

/**
 * Details about roles and permissions.
 */
@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SpringSecurityConfig {

    /** JWT authentication entry point for an authenticated user. */
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    /** JWT authentication filter. */
    private JwtAuthenticationFilter     authenticationFilter;

    /**
     * Encodes passwords.
     *
     * @return password encoder
     */
    @Bean
    public static PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates global permission structures for roles.
     *
     * @param http
     *            the security object
     * @return the SecurityFilterChain with permission information
     * @throws Exception
     *             if error
     */
    @Bean
    public SecurityFilterChain securityFilterChain ( HttpSecurity http ) throws Exception {
        http.cors( Customizer.withDefaults() ).csrf( csrf -> csrf.disable() )
                .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
                .authorizeHttpRequests( authorize -> {
                    authorize.requestMatchers( HttpMethod.OPTIONS, "/**" ).permitAll();
                    authorize.requestMatchers( "/api/auth/**" ).permitAll();
                    // Container health checks must not need a token.
                    authorize.requestMatchers( "/actuator/health/**" ).permitAll();
                    authorize.anyRequest().authenticated();
                } ).httpBasic( httpBasic -> httpBasic.disable() ).formLogin( form -> form.disable() );

        http.exceptionHandling( exception -> exception.authenticationEntryPoint( authenticationEntryPoint ) );

        http.addFilterBefore( authenticationFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }

    /**
     * Returns the AuthenticationManager for the project.
     *
     * @param configuration
     *            configuration information for authentication
     * @return AuthenticationManager
     * @throws Exception
     *             if error
     */
    @Bean
    public AuthenticationManager authenticationManager ( AuthenticationConfiguration configuration ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Global CORS configuration.
     *
     * This is required because Spring Security can reject requests before
     * controller-level @CrossOrigin annotations are applied.
     *
     * @param allowedOrigins
     *            comma-separated list of browser origins allowed to call the
     *            API, or "*" for any origin
     * @return CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource (
            @Value ( "${app.cors.allowed-origins:*}" ) final String allowedOrigins ) {
        CorsConfiguration config = new CorsConfiguration();

        /*
         * Origin patterns (rather than plain origins) are used so a wildcard
         * and host patterns such as https://*.example.com both work.
         */
        config.setAllowedOriginPatterns( parseAllowedOrigins( allowedOrigins ) );
        config.setAllowedMethods( List.of( "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" ) );
        config.setAllowedHeaders( List.of( "*" ) );
        config.setExposedHeaders( List.of( "Authorization" ) );

        /*
         * We use JWT in the Authorization header, not browser cookies. Keeping
         * credentials false avoids stricter CORS restrictions.
         */
        config.setAllowCredentials( false );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**", config );

        return source;
    }

    /**
     * Splits the configured allowed-origins property into a list, trimming
     * blank entries. Falls back to "*" when nothing usable is configured.
     *
     * @param allowedOrigins
     *            raw comma-separated property value
     * @return list of allowed origin patterns
     */
    private List<String> parseAllowedOrigins ( final String allowedOrigins ) {
        if ( allowedOrigins == null || allowedOrigins.isBlank() ) {
            return List.of( "*" );
        }

        final List<String> origins = Arrays.stream( allowedOrigins.split( "," ) ).map( String::trim )
                .filter( origin -> !origin.isEmpty() ).toList();

        return origins.isEmpty() ? List.of( "*" ) : origins;
    }
}
