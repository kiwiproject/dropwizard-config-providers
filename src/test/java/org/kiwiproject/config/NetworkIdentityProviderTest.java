package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;

import java.util.Optional;

@DisplayName("NetworkIdentityProvider")
class NetworkIdentityProviderTest {

    private KiwiEnvironment environment;
    private ExternalPropertyProvider externalPropertyProvider;

    @BeforeEach
    void setUp() {
        environment = mock(KiwiEnvironment.class);
        externalPropertyProvider = spy(new ExternalPropertyProvider());
    }

    @Nested
    class Construct {

        @Test
        void shouldCreateProviderThatCanNotProvide_ByDefaultWithoutAConfigFile() {
            var provider = new NetworkIdentityProvider();
            assertThat(provider.canProvide()).isFalse();
            assertThat(provider.network).isBlank();
            assertThat(provider.networkResolvedBy).isEqualTo(ResolvedBy.NONE);
        }

        @Test
        void shouldCreateProviderThatCanProvide_WhenConfigFileContainsNetwork() {
            when(externalPropertyProvider.getProperty(NetworkIdentityProvider.PROPERTY_KEY)).thenReturn(Optional.of("MY-VPC"));

            var provider = new NetworkIdentityProvider(externalPropertyProvider, environment);
            assertThat(provider.canProvide()).isTrue();
            assertThat(provider.network).isEqualTo("MY-VPC");
            assertThat(provider.networkResolvedBy).isEqualTo(ResolvedBy.EXTERNAL_PROPERTY);
        }

        @Test
        void shouldCreateProviderThatCanProvide_WhenEnvironmentVariableExists() {
            when(environment.getenv("KIWI_ENV_NETWORK")).thenReturn("MY-VPC-2");

            var provider = new NetworkIdentityProvider(externalPropertyProvider, environment);
            assertThat(provider.canProvide()).isTrue();
            assertThat(provider.network).isEqualTo("MY-VPC-2");
            assertThat(provider.networkResolvedBy).isEqualTo(ResolvedBy.SYSTEM_ENV);
        }

        @Test
        void shouldCreateProviderThatCanProvide_WhenExplicitNetworkIsGiven() {
            var provider = new NetworkIdentityProvider("MY-SUBNET");
            assertThat(provider.canProvide()).isTrue();
            assertThat(provider.network).isEqualTo("MY-SUBNET");
            assertThat(provider.networkResolvedBy).isEqualTo(ResolvedBy.EXPLICIT_VALUE);
        }
    }
}
