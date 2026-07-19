package com.adi.naukri.automation;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Immutable configuration for a single {@link NaukriAutomator} run.
 *
 * @param baseUrl              root URL of the target site (e.g. {@code https://www.naukri.com}
 *                             or {@code http://127.0.0.1:PORT} for tests).
 * @param downloadsDir         directory where downloaded resumes are saved.
 *                             Screenshots on failure are written to
 *                             {@code <downloadsDir>/screenshots/<email>.png}.
 * @param pageLoadMs           navigation / page-load timeout in milliseconds.
 * @param actionMs             element interaction timeout for the LOGIN step in milliseconds.
 * @param postLoginActionMs    element interaction timeout for all post-login steps (HEADLINE_*,
 *                             DOWNLOAD_RESUME, UPLOAD_RESUME, LOGOUT) in milliseconds.
 *                             Real Naukri's Next.js hydration is slow; 25 s is recommended.
 * @param manualLogin          when {@code true} the automator skips the programmatic
 *                             login and instead waits for the user to log in manually.
 * @param manualLoginTimeout   how long to wait for the user to complete manual login.
 * @param initialDelayMs       milliseconds to pause at the very start of each account run
 *                             (before launching the browser) to let the WS handshake and
 *                             run-screen settle. Default is 3000 ms.
 *
 * Author: Adikarthik Gupta C B
 */
public record AutomatorConfig(
        String   baseUrl,
        Path     downloadsDir,
        long     pageLoadMs,
        long     actionMs,
        long     postLoginActionMs,
        boolean  manualLogin,
        Duration manualLoginTimeout,
        long     initialDelayMs
) {
    /**
     * Convenience factory that sets {@code postLoginActionMs} to 25 s (real-Naukri default)
     * and {@code initialDelayMs} to 3 s.
     */
    public AutomatorConfig(
            String baseUrl,
            Path downloadsDir,
            long pageLoadMs,
            long actionMs,
            boolean manualLogin,
            Duration manualLoginTimeout) {
        this(baseUrl, downloadsDir, pageLoadMs, actionMs, 25_000L, manualLogin, manualLoginTimeout, 3_000L);
    }

    /**
     * Convenience factory that sets {@code postLoginActionMs} to 25 s (real-Naukri default).
     */
    public AutomatorConfig(
            String baseUrl,
            Path downloadsDir,
            long pageLoadMs,
            long actionMs,
            boolean manualLogin,
            Duration manualLoginTimeout,
            long initialDelayMs) {
        this(baseUrl, downloadsDir, pageLoadMs, actionMs, 25_000L, manualLogin, manualLoginTimeout, initialDelayMs);
    }
}
