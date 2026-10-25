package io.github.yaforster.trails.adapter.authnone;

import io.github.yaforster.trails.sdk.autoconfigure.TrailsSdkAutoConfiguration;
import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.composition.TrailsAccessPolicy;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import java.util.UUID;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Registers credential-free access with one durable identity shared by all visitors. */
@AutoConfiguration(before = TrailsSdkAutoConfiguration.class)
public class NoAuthAutoConfiguration {

    // This identity is a persistence key, not a credential; changing it would abandon the shared profile.
    private static final TrailsUserIdentity SHARED_IDENTITY = new TrailsUserIdentity(
            UUID.fromString("803e31d2-04dd-4c20-9ab7-9861a753f497"));

    @Bean
    UserDetailsService noAuthUserDetailsService() {
        // Prevent Boot from creating an unused default user and generated password.
        return _ -> {
            throw new UsernameNotFoundException("Credential login is disabled");
        };
    }

    @Bean
    TrailsAuthenticationRegistration noAuthRegistration() {
        return new TrailsAuthenticationRegistration(
                new AdapterId("none"), http -> http.csrf(AbstractHttpConfigurer::disable)
                                                   .sessionManagement(session -> session.sessionCreationPolicy(
                                                           SessionCreationPolicy.STATELESS))
                                                   .addFilterBefore(new SharedIdentityFilter(SHARED_IDENTITY),
                                                           AnonymousAuthenticationFilter.class),
                _ -> SHARED_IDENTITY, TrailsAccessPolicy.UNRESTRICTED);
    }
}
