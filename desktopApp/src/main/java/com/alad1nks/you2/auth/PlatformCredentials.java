package com.alad1nks.you2.auth;

/** Desktop-only implementation; shared UI and screenshot tests do not depend on Java 25. */
public final class PlatformCredentials implements DesktopSessionStore {
    private final String service;
    private final String account;
    private DesktopSessionStore backend;

    public static PlatformCredentials create() {
        String apiKey = System.getenv("YOU2_FIREBASE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) apiKey = "AIzaSyDxS2_csqcYwEnBERyySF8OZ-tNtUctBEw";
        return new PlatformCredentials("com.alad1nks.you2.firebase", apiKey);
    }

    public PlatformCredentials(String service, String account) {
        if (service.isBlank() || account.isBlank() || service.indexOf('\0') >= 0 || account.indexOf('\0') >= 0)
            throw new IllegalArgumentException("Invalid credential identifier");
        this.service = service;
        this.account = account;
    }

    private synchronized DesktopSessionStore backend() {
        if (backend == null) {
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")) backend = new WindowsCredentials(service, account);
            else if (os.startsWith("Mac")) backend = new MacCredentials(service, account);
            else if (os.startsWith("Linux")) backend = new LinuxCredentials(service, account);
            else throw new IllegalStateException("Unsupported OS credential store");
        }
        return backend;
    }

    @Override public String read() {
        String token = backend().read();
        return token == null || token.isBlank() ? null : token;
    }

    @Override public void write(String refreshToken) {
        if (refreshToken.isBlank() || refreshToken.indexOf('\0') >= 0)
            throw new IllegalArgumentException("Invalid refresh token");
        backend().write(refreshToken);
    }

    @Override public void clear() { backend().clear(); }
}
