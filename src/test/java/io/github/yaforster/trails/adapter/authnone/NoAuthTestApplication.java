package io.github.yaforster.trails.adapter.authnone;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.CapabilityLabel;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.sdk.composition.*;
import java.util.List;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(NoAuthTestApplication.Operations.class)
class NoAuthTestApplication {
    @Bean
    UserProfileService profiles() {
        UserProfileService profiles = mock();
        when(profiles.getOrCreateProfile(any())).thenAnswer(
                call -> new UserProfile(call.getArgument(0), null, null, null, null, false));
        return profiles;
    }

    @Bean
    TrailsPersistenceRegistration persistence(UserProfileService profiles) {
        return new TrailsPersistenceRegistration(new AdapterId("fixture-persistence"),
                new TrailsPersistenceRegistration.Ports(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                        mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), profiles));
    }

    @Bean
    TrailsBrowserRegistration browser() {
        return new TrailsBrowserRegistration(new AdapterId("fixture-browser"), mock());
    }

    @Bean
    TrailsPrintingRegistration printing() {
        return new TrailsPrintingRegistration(new AdapterId("fixture-printer"), mock());
    }

    @Bean
    CapabilityContributor browserMetadata() {
        return () -> List.of(new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "30000"));
    }

    @RestController
    static class Operations {
        @GetMapping("/api/async")
        java.util.concurrent.Callable<String> async() {
            return () -> org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                                                        .getAuthentication()
                                                                                        .getName();
        }

        @PostMapping("/api/manage")
        @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
        void manage() {}

        @GetMapping("/api/test-data")
        @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
        void testData() {}

        @PostMapping("/api/execute")
        @PreAuthorize("@trailsAuthorization.canExecuteTests(authentication)")
        void execute() {}

        @DeleteMapping("/user/manage")
        @PreAuthorize("@trailsAuthorization.canManageUsers(authentication)")
        void users() {}

        @PostMapping("/api/report")
        @PreAuthorize("@trailsAuthorization.canReportDeployments(authentication)")
        void report() {}
    }
}
