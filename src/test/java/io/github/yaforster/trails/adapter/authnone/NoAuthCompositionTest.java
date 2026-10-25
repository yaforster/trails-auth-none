package io.github.yaforster.trails.adapter.authnone;

import io.github.yaforster.trails.sdk.composition.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NoAuthCompositionTest {
    @Test
    void shouldRejectAnotherAuthenticationProvider_evenWhenPrimary() {
        new ApplicationContextRunner().withUserConfiguration(NoAuthTestApplication.class, OtherProvider.class)
                                      .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                                              "Invalid Trails composition: at most one authentication registration is allowed, found 2"));
    }

    @Configuration(proxyBeanMethods = false)
    static class OtherProvider {
        @Bean
        @Primary
        TrailsAuthenticationRegistration otherAuthentication() {
            return new TrailsAuthenticationRegistration(new AdapterId("customer"), http -> {
            }, authentication -> mock());
        }
    }
}
