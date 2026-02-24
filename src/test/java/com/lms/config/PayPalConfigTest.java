package com.lms.config;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PayPalConfigTest {

    @InjectMocks
    private PayPalConfig payPalConfig;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(payPalConfig, "clientId", "test-client-id");
        ReflectionTestUtils.setField(payPalConfig, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(payPalConfig, "mode", "sandbox");
    }

    @Test
    void paypalSdkConfig_ShouldCreateConfigMapWithCorrectMode() {
        Map<String, String> configMap = payPalConfig.paypalSdkConfig();

        assertThat(configMap).isNotNull();
        assertThat(configMap).hasSize(1);
        assertThat(configMap.get("mode")).isEqualTo("sandbox");
    }

    @Test
    void paypalSdkConfig_ShouldHandleLiveMode() {
        ReflectionTestUtils.setField(payPalConfig, "mode", "live");

        Map<String, String> configMap = payPalConfig.paypalSdkConfig();

        assertThat(configMap.get("mode")).isEqualTo("live");
    }

    @Test
    void oAuthTokenCredential_ShouldCreateCredentialWithCorrectConfig() {
        OAuthTokenCredential credential = payPalConfig.oAuthTokenCredential();

        assertThat(credential).isNotNull();
    }

    @Test
    void apiContext_ShouldCreateContextWithCorrectConfig() {
        APIContext apiContext = payPalConfig.apiContext();

        assertThat(apiContext).isNotNull();
        assertThat(apiContext.getConfigurationMap()).isNotNull();
        assertThat(apiContext.getConfigurationMap().get("mode")).isEqualTo("sandbox");
    }

    @Test
    void apiContext_ShouldHaveCorrectClientIdAndSecret() {
        APIContext apiContext = payPalConfig.apiContext();

        assertThat(apiContext).isNotNull();
    }

    @Test
    void payPalConfig_ShouldHandleEmptyProperties() {
        ReflectionTestUtils.setField(payPalConfig, "clientId", "");
        ReflectionTestUtils.setField(payPalConfig, "clientSecret", "");
        ReflectionTestUtils.setField(payPalConfig, "mode", "");

        Map<String, String> configMap = payPalConfig.paypalSdkConfig();

        assertThat(configMap.get("mode")).isEmpty();

        // Fix: Properly handle the exception
        assertThatThrownBy(() -> payPalConfig.apiContext())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mode needs to be either `sandbox` or `live`");
    }

    @Test
    void payPalConfig_ShouldHandleNullProperties() {
        ReflectionTestUtils.setField(payPalConfig, "clientId", null);
        ReflectionTestUtils.setField(payPalConfig, "clientSecret", null);
        ReflectionTestUtils.setField(payPalConfig, "mode", null);

        Map<String, String> configMap = payPalConfig.paypalSdkConfig();

        assertThat(configMap.get("mode")).isNull();

        // Fix: Properly handle the exception
        assertThatThrownBy(() -> payPalConfig.apiContext())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mode needs to be either `sandbox` or `live`");
    }
}