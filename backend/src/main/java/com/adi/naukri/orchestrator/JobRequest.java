package com.adi.naukri.orchestrator;

import java.util.List;

/**
 * Immutable request object passed to {@link JobOrchestrator#start}.
 *
 * @param emails          ordered list of account email addresses to process
 * @param password        shared account password — never logged or persisted
 * @param headless        {@code true} to run browser in headless mode
 * @param manualLogin     {@code true} to pause for manual login instead of scripted login
 * @param outputFolder    base path under which the run sub-folder will be created
 * @param baseUrlOverride optional override for the Naukri base URL; {@code null} means
 *                        use the default {@code https://www.naukri.com}
 * @param initialDelayMs  milliseconds to sleep at the start of each account run before
 *                        opening the browser; gives the WS handshake time to settle.
 *                        A value of 0 disables the delay (useful for tests).
 *
 * Author: Adikarthik Gupta C B
 */
public record JobRequest(
        List<String> emails,
        String       password,
        boolean      headless,
        boolean      manualLogin,
        String       outputFolder,
        String       baseUrlOverride,
        long         initialDelayMs
) {
    /** Compact constructor — defaults initialDelayMs to 3 s if not specified. */
    public JobRequest(
            List<String> emails,
            String password,
            boolean headless,
            boolean manualLogin,
            String outputFolder,
            String baseUrlOverride) {
        this(emails, password, headless, manualLogin, outputFolder, baseUrlOverride, 3_000L);
    }

    public String effectiveBaseUrl() {
        return (baseUrlOverride != null && !baseUrlOverride.isBlank())
                ? baseUrlOverride
                : "https://www.naukri.com";
    }
}
