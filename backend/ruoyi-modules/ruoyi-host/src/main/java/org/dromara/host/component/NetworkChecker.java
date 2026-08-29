package org.dromara.host.component;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * @author lyn
 */
@Component
public class NetworkChecker {
    private final List<String> testEndpoints = List.of(
        "http://47.86.30.65:8081"
       /* "http://connectivitycheck.gstatic.com/generate_204",
        "http://captive.apple.com/hotspot-detect.html",
        "http://msftconnecttest.com/connecttest.txt"*/
    );

    public boolean isOnline() {
        return testEndpoints.parallelStream()
            .anyMatch(url -> {
                try {
                    return HttpClient.newHttpClient()
                        .send(HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(Duration.ofSeconds(2))
                                .build(),
                            HttpResponse.BodyHandlers.discarding())
                        .statusCode() <= 399;
                } catch (Exception e) {
                    return false;
                }
            });
    }
}
