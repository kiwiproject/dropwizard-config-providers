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
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
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
import java.util.Map;

@DisplayName("ElkLoggerConfigProvider")
class ElkLoggerConfigProviderTest {

    private static final String HOST = "localhost";
    private static final int PORT = 9000;
    private static final Map<String, String> CUSTOM_FIELDS = Map.of("serviceName", "test-service");

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
                addSystemProperty(ElkLoggerConfigProvider.DEFAULT_HOST_SYSTEM_PROPERTY, HOST);
                addSystemProperty(ElkLoggerConfigProvider.DEFAULT_PORT_SYSTEM_PROPERTY, Integer.toString(PORT));
                addSystemProperty(ElkLoggerConfigProvider.DEFAULT_CUSTOM_FIELDS_SYSTEM_PROPERTY, JSON_HELPER.toJson(CUSTOM_FIELDS));

                var provider = ElkLoggerConfigProvider.builder().build();
                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("host_var", HOST);
                addSystemProperty("port_var", Integer.toString(PORT));
                addSystemProperty("custom_field_var", JSON_HELPER.toJson(CUSTOM_FIELDS));

                var provider = ElkLoggerConfigProvider.builder()
                        .hostResolverStrategy(newSystemPropertyFieldResolverStrategy("host_var"))
                        .portResolverStrategy(newSystemPropertyFieldResolverStrategy("port_var"))
                        .customFieldsResolverStrategy(newSystemPropertyFieldResolverStrategy("custom_field_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ElkLoggerConfigProvider.DEFAULT_HOST_ENV_VARIABLE)).thenReturn(HOST);
                when(env.getenv(ElkLoggerConfigProvider.DEFAULT_PORT_ENV_VARIABLE)).thenReturn(Integer.toString(PORT));
                when(env.getenv(ElkLoggerConfigProvider.DEFAULT_CUSTOM_FIELDS_ENV_VARIABLE)).thenReturn(JSON_HELPER.toJson(CUSTOM_FIELDS));

                var provider = ElkLoggerConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("host_var")).thenReturn(HOST);
                when(env.getenv("port_var")).thenReturn(Integer.toString(PORT));
                when(env.getenv("custom_field_var")).thenReturn(JSON_HELPER.toJson(CUSTOM_FIELDS));

                var provider = ElkLoggerConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .hostResolverStrategy(newEnvVarFieldResolverStrategy("host_var"))
                        .portResolverStrategy(newEnvVarFieldResolverStrategy("port_var"))
                        .customFieldsResolverStrategy(newEnvVarFieldResolverStrategy("custom_field_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("ElkLoggerConfigProvider/config.properties"));
                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = ElkLoggerConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = ElkLoggerConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .hostResolverStrategy(newExternalPropertyFieldResolverStrategy("elk.host.provided"))
                        .portResolverStrategy(newExternalPropertyFieldResolverStrategy("elk.port.provided"))
                        .customFieldsResolverStrategy(newExternalPropertyFieldResolverStrategy("elk.customFields.provided"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = ElkLoggerConfigProvider.builder()
                        .hostResolverStrategy(newExplicitValueFieldResolverStrategy(HOST))
                        .portResolverStrategy(newExplicitValueFieldResolverStrategy(PORT))
                        .customFieldsResolverStrategy(newExplicitValueFieldResolverStrategy(CUSTOM_FIELDS))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXPLICIT_VALUE);
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = ElkLoggerConfigProvider.builder()
                        .hostResolverStrategy(newSupplierFieldResolverStrategy(() -> HOST))
                        .portResolverStrategy(newSupplierFieldResolverStrategy(() -> PORT))
                        .customFieldsResolverStrategy(newSupplierFieldResolverStrategy(() -> CUSTOM_FIELDS))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SUPPLIER);
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = ElkLoggerConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getHost()).isNull();
                assertThat(provider.getPort()).isZero();
                assertThat(provider.getCustomFields()).isNull();
                assertThat(provider.getResolvedBy()).contains(
                        entry("host", ResolvedBy.NONE),
                        entry("port", ResolvedBy.NONE),
                        entry("customFields", ResolvedBy.NONE)
                );
            }

        }

    }

    private void assertProviderCanProvide(ElkLoggerConfigProvider provider, ResolvedBy resolvedBy) {
        assertThat(provider.canProvide()).isTrue();
        assertThat(provider.getHost()).isEqualTo(HOST);
        assertThat(provider.getPort()).isEqualTo(PORT);
        assertThat(provider.getCustomFields()).isEqualTo(CUSTOM_FIELDS);
        assertThat(provider.getResolvedBy()).contains(
                entry("host", resolvedBy),
                entry("port", resolvedBy),
                entry("customFields", resolvedBy)
        );
    }
}
