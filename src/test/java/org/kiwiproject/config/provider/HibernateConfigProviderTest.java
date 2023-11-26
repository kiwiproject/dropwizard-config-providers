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
import java.util.HashMap;
import java.util.Map;

@DisplayName("HibernateConfigProvider")
class HibernateConfigProviderTest {

    private static final Map<String, Object> PROPERTIES = Map.of("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");

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
                addSystemProperty(HibernateConfigProvider.DEFAULT_HIBERNATE_SYSTEM_PROPERTY, JSON_HELPER.toJson(PROPERTIES));

                var provider = HibernateConfigProvider.builder().build();
                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("properties_var", JSON_HELPER.toJson(PROPERTIES));

                var provider = HibernateConfigProvider.builder()
                        .resolverStrategy(newSystemPropertyFieldResolverStrategy("properties_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_PROPERTY);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(HibernateConfigProvider.DEFAULT_HIBERNATE_ENV_VARIABLE)).thenReturn(JSON_HELPER.toJson(PROPERTIES));

                var provider = HibernateConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("properties_var")).thenReturn(JSON_HELPER.toJson(PROPERTIES));

                var provider = HibernateConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(newEnvVarFieldResolverStrategy("properties_var"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SYSTEM_ENV);
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("HibernateConfigProvider/config.properties"));
                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = HibernateConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = HibernateConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(newExternalPropertyFieldResolverStrategy("hibernate.properties.provided"))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXTERNAL_PROPERTY);
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = HibernateConfigProvider.builder()
                        .resolverStrategy(newExplicitValueFieldResolverStrategy(PROPERTIES))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.EXPLICIT_VALUE);
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = HibernateConfigProvider.builder()
                        .resolverStrategy(newSupplierFieldResolverStrategy(() -> PROPERTIES))
                        .build();

                assertProviderCanProvide(provider, ResolvedBy.SUPPLIER);
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCanProvide() {
                var provider = HibernateConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getHibernateProperties()).containsAllEntriesOf(HibernateConfigProvider.DEFAULT_HIBERNATE_PROPERTIES);
                assertThat(provider.getResolvedBy()).contains(
                        entry("hibernateProperties", ResolvedBy.PROVIDER_DEFAULT)
                );
            }

        }

    }

    private void assertProviderCanProvide(HibernateConfigProvider provider, ResolvedBy resolvedBy) {
        assertThat(provider.canProvide()).isTrue();

        var mergedProperties = new HashMap<>(HibernateConfigProvider.DEFAULT_HIBERNATE_PROPERTIES);
        mergedProperties.putAll(PROPERTIES);

        assertThat(provider.getHibernateProperties()).containsAllEntriesOf(mergedProperties);
        assertThat(provider.getResolvedBy()).contains(
                entry("hibernateProperties", resolvedBy)
        );
    }
}
