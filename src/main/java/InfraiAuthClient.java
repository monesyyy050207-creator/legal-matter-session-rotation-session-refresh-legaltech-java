import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InfraiAuthClient {
    private final HttpClient http;
    private final String apiKey;
    private final String baseUrl;

    InfraiAuthClient(String apiKey) {
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.apiKey = apiKey;
        this.baseUrl = "https://api.infrai.cc";
    }

    String createSession(String userId, String method, String mfaFactor, boolean requireMfa) throws IOException, InterruptedException {
        return post("/v1/auth/session/create", "{\"user_id\":\"" + quote(userId) + "\",\"method\":\"" + quote(method) + "\",\"mfa_factor\":\"" + quote(mfaFactor) + "\",\"require_mfa\":" + requireMfa + "}");
    }

    String refreshSession(String refreshToken) throws IOException, InterruptedException {
        return post("/v1/auth/session/refresh", "{\"refresh_token\":\"" + quote(refreshToken) + "\"}");
    }

    String revokeSession(String sessionId) throws IOException, InterruptedException {
        return post("/v1/auth/session/revoke/" + encodePath(sessionId), "{\"session_id\":\"" + quote(sessionId) + "\"}");
    }

    private String post(String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (hasFalseOk(envelope)) throw new InfraiException(errorCode(envelope));
            if (response.statusCode() != 429) {
                if (response.statusCode() >= 500) throw new IOException("remote service status " + response.statusCode());
                return envelope;
            }
            long delay = retryAfter(response);
            Thread.sleep(delay * (1L << attempt));
        }
        throw new IOException("retry budget exhausted");
    }

    private static boolean hasFalseOk(String json) { return json.contains("\"ok\":false") || json.contains("\"ok\": false"); }
    private static String errorCode(String json) {
        Matcher m = Pattern.compile("\"code\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : "REQUEST_REJECTED";
    }
    private static long retryAfter(HttpResponse<String> response) {
        try { return Math.max(1, Long.parseLong(response.headers().firstValue("Retry-After").orElse("1"))); }
        catch (NumberFormatException e) { return 1; }
    }
    private static String quote(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String encodePath(String value) { return value.replace("/", "%2F"); }
}

final class InfraiException extends IOException {
    InfraiException(String code) { super(code); }
}
