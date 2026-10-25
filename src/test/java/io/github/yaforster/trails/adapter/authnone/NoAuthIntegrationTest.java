package io.github.yaforster.trails.adapter.authnone;

import java.util.UUID;
import io.github.yaforster.trails.app.services.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = NoAuthTestApplication.class)
@AutoConfigureMockMvc
class NoAuthIntegrationTest {
    @Autowired
    private MockMvc http;
    @Autowired
    private UserProfileService profiles;

    @Test
    void shouldRetainSharedIdentity_duringAsyncExecutionAndRedispatch() throws Exception {
        org.springframework.test.web.servlet.MvcResult result = http.perform(get("/api/async"))
                                                                    .andExpect(request().asyncStarted())
                                                                    .andReturn();
        http.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(content().string("803e31d2-04dd-4c20-9ab7-9861a753f497"));
    }

    @ParameterizedTest
    @CsvSource({"POST,/api/manage", "GET,/api/test-data", "POST,/api/execute", "DELETE,/user/manage",
            "POST,/api/report"})
    void shouldAllowEveryRoleProtectedOperation_withoutCredentialsOrCsrf(String method, String path) throws Exception {
        http.perform(request(HttpMethod.valueOf(method), path))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource({"Bearer invalid", "Basic invalid"})
    void shouldUseSharedIdentity_evenWithUnusableCredentials(String authorization) throws Exception {
        http.perform(get("/user/profile").header("Authorization", authorization))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("803e31d2-04dd-4c20-9ab7-9861a753f497"))
            .andExpect(jsonPath("$.links.update.method").value("PUT"));
    }

    @Test
    void shouldExposeSharedProfile_withoutLogin() throws Exception {
        http.perform(get("/user/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("803e31d2-04dd-4c20-9ab7-9861a753f497"));
    }

    @Test
    void shouldDeleteSharedAvatar_withoutLogin() throws Exception {
        http.perform(delete("/user/profile/avatar"))
            .andExpect(status().isNoContent());
        verify(profiles).deleteProfilePicture(UUID.fromString("803e31d2-04dd-4c20-9ab7-9861a753f497"));
    }

    @Test
    void shouldAdvertiseUnrestrictedAccessAndProfiles() throws Exception {
        http.perform(get("/api/capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'AUTHENTICATION_REQUIRED')].value").value("false"))
            .andExpect(jsonPath("$[?(@.name == 'USER_FEATURES_ACTIVE')].value").value("true"));
    }

    @Test
    void shouldPreserveRequestValidation() throws Exception {
        http.perform(put("/user/profile").contentType("application/json")
                                         .content("{\"email\":\"invalid\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldClearRequestIdentity_afterProcessing() throws Exception {
        http.perform(get("/user/profile"))
            .andExpect(status().isOk());
        assertThat(SecurityContextHolder.getContext()
                                        .getAuthentication()).isNull();
    }

    @Test
    void shouldAllowBrowserPreflight() throws Exception {
        http.perform(options("/api/manage").header("Origin", "http://localhost:4200")
                                           .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }
}
