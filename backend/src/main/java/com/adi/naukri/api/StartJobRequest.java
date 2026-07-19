package com.adi.naukri.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for {@code POST /api/jobs}.
 *
 * <p>{@link #baseUrlOverride} is a test-only field. Production callers never send it;
 * when absent (null) the orchestrator uses {@code https://www.naukri.com}.</p>
 *
 * <p>{@link #initialDelayMs} is optional. When absent (null) the orchestrator uses the
 * default of 3 000 ms. Pass 0 to disable the warm-up pause (useful in tests).</p>
 *
 * Author: Adikarthik Gupta C B
 */
public record StartJobRequest(
        @NotEmpty List<@Email String> emails,
        @NotBlank String password,
        boolean headless,
        boolean manualLogin,
        @NotBlank String outputFolder,
        String baseUrlOverride,
        Long initialDelayMs
) {}
