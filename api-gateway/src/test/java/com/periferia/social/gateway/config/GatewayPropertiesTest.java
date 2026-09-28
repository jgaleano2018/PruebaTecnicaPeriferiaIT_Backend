package com.periferia.social.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class GatewayPropertiesTest {

    @Test
    void appliesSafeResilienceDefaults() {
        GatewayProperties props = new GatewayProperties(URI.create("http://auth"), URI.create("http://post"),
                URI.create("http://feed"), URI.create("ws://feed"), null, null);

        assertThat(props.allowedOrigins()).isEmpty();
        assertThat(props.resilience().timeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(props.resilience().failureRateThreshold()).isEqualTo(50);
        assertThat(props.resilience().retries()).isZero();
    }

    @Test
    void keepsExplicitValues() {
        GatewayProperties.Resilience resilience =
                new GatewayProperties.Resilience(Duration.ofSeconds(2), Duration.ofSeconds(30), 25, Duration.ofMillis(50), 3);

        assertThat(resilience.timeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(resilience.failureRateThreshold()).isEqualTo(25);
        assertThat(resilience.retries()).isEqualTo(3);
    }
}
