package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.kiwiproject.config.provider.util.TestHelpers.newEnvVarFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newExplicitValueFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newExternalPropertyFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newSupplierFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.TestHelpers.newSystemPropertyFieldResolverStrategy;
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

@DisplayName("NetworkIdentityConfigProvider")
class NetworkIdentityConfigProviderTest {

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
                addSystemProperty(NetworkIdentityConfigProvider.DEFAULT_NETWORK_SYSTEM_PROPERTY, "VPC-SystemProp-Default");

                var provider = NetworkIdentityConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-SystemProp-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("bar", "VPC-SystemProp-Provided");

                var provider = NetworkIdentityConfigProvider.builder()
                        .resolverStrategy(newSystemPropertyFieldResolverStrategy("bar"))
                        .build();
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
                when(env.getenv(NetworkIdentityConfigProvider.DEFAULT_NETWORK_ENV_VARIABLE)).thenReturn("VPC-Env-Default");

                var provider = NetworkIdentityConfigProvider.builder().kiwiEnvironment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Env-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn("VPC-Env-Provided");

                var provider = NetworkIdentityConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(newEnvVarFieldResolverStrategy("baz"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Env-Provided");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers
                        .resourceFilePath("NetworkIdentityConfigProvider/config.properties"));

                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = NetworkIdentityConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-External-Default");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = NetworkIdentityConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(newExternalPropertyFieldResolverStrategy("network.provided"))
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
                var provider = NetworkIdentityConfigProvider.builder()
                        .resolverStrategy(newExplicitValueFieldResolverStrategy("VPC-Explicit"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Explicit");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = NetworkIdentityConfigProvider.builder()
                        .resolverStrategy(newSupplierFieldResolverStrategy(() -> "VPC-Supplier"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getNetwork()).isEqualTo("VPC-Supplier");
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.SUPPLIER));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = NetworkIdentityConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getNetwork()).isNull();
                assertThat(provider.getResolvedBy()).containsExactly(entry("network", ResolvedBy.NONE));
            }

        }

    }
}
