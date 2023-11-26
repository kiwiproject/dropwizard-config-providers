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

@DisplayName("ElucidationConfigProvider")
class ElucidationConfigProviderTest {

    private static final String HOST = "localhost";
    private static final int PORT = 9000;

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
                addSystemProperty(ElucidationConfigProvider.DEFAULT_HOST_SYSTEM_PROPERTY, HOST);
                addSystemProperty(ElucidationConfigProvider.DEFAULT_PORT_SYSTEM_PROPERTY, Integer.toString(PORT));
                addSystemProperty(ElucidationConfigProvider.DEFAULT_ENABLED_SYSTEM_PROPERTY, "true");

                var provider = ElucidationConfigProvider.builder().build();
                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("host_var", HOST);
                addSystemProperty("port_var", Integer.toString(PORT));
                addSystemProperty("enabled_var", "true");

                var provider = ElucidationConfigProvider.builder()
                        .hostResolverStrategy(newSystemPropertyFieldResolverStrategy("host_var"))
                        .portResolverStrategy(newSystemPropertyFieldResolverStrategy("port_var"))
                        .enabledResolverStrategy(newSystemPropertyFieldResolverStrategy("enabled_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ElucidationConfigProvider.DEFAULT_HOST_ENV_VARIABLE)).thenReturn(HOST);
                when(env.getenv(ElucidationConfigProvider.DEFAULT_PORT_ENV_VARIABLE)).thenReturn(Integer.toString(PORT));
                when(env.getenv(ElucidationConfigProvider.DEFAULT_ENABLED_ENV_VARIABLE)).thenReturn("true");

                var provider = ElucidationConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("host_var")).thenReturn(HOST);
                when(env.getenv("port_var")).thenReturn(Integer.toString(PORT));
                when(env.getenv("enabled_var")).thenReturn("true");

                var provider = ElucidationConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .hostResolverStrategy(newEnvVarFieldResolverStrategy("host_var"))
                        .portResolverStrategy(newEnvVarFieldResolverStrategy("port_var"))
                        .enabledResolverStrategy(newEnvVarFieldResolverStrategy("enabled_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("ElucidationConfigProvider/config.properties"));
                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = ElucidationConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = ElucidationConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .hostResolverStrategy(newExternalPropertyFieldResolverStrategy("elucidation.host.provided"))
                        .portResolverStrategy(newExternalPropertyFieldResolverStrategy("elucidation.port.provided"))
                        .enabledResolverStrategy(newExternalPropertyFieldResolverStrategy("elucidation.enabled.provided"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = ElucidationConfigProvider.builder()
                        .hostResolverStrategy(newExplicitValueFieldResolverStrategy(HOST))
                        .portResolverStrategy(newExplicitValueFieldResolverStrategy(PORT))
                        .enabledResolverStrategy(newExplicitValueFieldResolverStrategy(true))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXPLICIT_VALUE);
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = ElucidationConfigProvider.builder()
                        .hostResolverStrategy(newSupplierFieldResolverStrategy(() -> HOST))
                        .portResolverStrategy(newSupplierFieldResolverStrategy(() -> PORT))
                        .enabledResolverStrategy(newSupplierFieldResolverStrategy(() -> true))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SUPPLIER);
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = ElucidationConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getHost()).isNull();
                assertThat(provider.getPort()).isZero();
                assertThat(provider.isEnabled()).isFalse();
                assertThat(provider.getResolvedBy()).contains(
                        entry("host", ResolvedBy.NONE),
                        entry("port", ResolvedBy.NONE),
                        entry("enabled", ResolvedBy.NONE)
                );
            }

        }

    }

    private void assertProviderCanProvide(ElucidationConfigProvider provider, ResolvedBy resolvedBy) {
        assertThat(provider.canProvide()).isTrue();
        assertThat(provider.getHost()).isEqualTo(HOST);
        assertThat(provider.getPort()).isEqualTo(PORT);
        assertThat(provider.isEnabled()).isTrue();
        assertThat(provider.getResolvedBy()).contains(
                entry("host", resolvedBy),
                entry("port", resolvedBy),
                entry("enabled", resolvedBy)
        );
    }
}
