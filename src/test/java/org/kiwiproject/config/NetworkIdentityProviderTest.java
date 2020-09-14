package org.kiwiproject.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.config.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;

import java.nio.file.Path;

@DisplayName("NetworkIdentityProvider")
class NetworkIdentityProviderTest {

    @Nested
    class Construct {

        @Nested
        class WithSystemProperty {

            @AfterEach
            void tearDown() {
                clearAllSystemProperties();
            }

            @Test
            void shouldBuildUsingDefaultSystemPropertyKey() {
                addSystemProperty(NetworkIdentityProvider.DEFAULT_NETWORK_SYSTEM_PROPERTY, "VPC-SystemProp-Default");

                var provider = NetworkIdentityProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-SystemProp-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("bar", "VPC-SystemProp-Provided");

                var provider = NetworkIdentityProvider.builder().systemPropertyKey("bar").build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-SystemProp-Provided");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_PROPERTY));
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(NetworkIdentityProvider.DEFAULT_NETWORK_ENV_VARIABLE)).thenReturn("VPC-Env-Default");

                var provider = NetworkIdentityProvider.builder().environment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Env-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn("VPC-Env-Provided");

                var provider = NetworkIdentityProvider.builder()
                        .environment(env)
                        .envVariable("baz")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Env-Provided");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalPropertyProvider externalPropertyProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("NetworkPropertyProvider/config.properties"));
                externalPropertyProvider = ExternalPropertyProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = NetworkIdentityProvider.builder().externalPropertyProvider(externalPropertyProvider).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-External-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = NetworkIdentityProvider.builder()
                        .externalPropertyProvider(externalPropertyProvider)
                        .externalProperty("network.provided")
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-External-Provided");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.EXTERNAL_PROPERTY));
            }
        }

        @Nested
        class WithExplicitNetwork {

            @Test
            void shouldBuildUsingProvidedNetwork() {
                var provider = NetworkIdentityProvider.builder().namedNetwork("VPC-Explicit").build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Explicit");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = NetworkIdentityProvider.builder()
                        .networkSupplier(() -> "VPC-Supplier")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Supplier");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.DEFAULT));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = NetworkIdentityProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getNetwork()).isEmpty();
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.NONE));
            }

        }

    }
}
