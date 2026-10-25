package io.github.yaforster.trails.adapter.authnone;

import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Establishes the shared identity only inside the SDK-owned security chain. */
final class SharedIdentityFilter extends OncePerRequestFilter {
    private final TrailsUserIdentity identity;

    SharedIdentityFilter(TrailsUserIdentity identity) {
        this.identity = identity;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken token = createToken();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private @NonNull UsernamePasswordAuthenticationToken createToken() {
        UUID principal = identity.userId();
        List<GrantedAuthority> authorities = List.of();
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
    }
}
