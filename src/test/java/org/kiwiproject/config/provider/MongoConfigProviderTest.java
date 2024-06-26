package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.config.provider.FieldResolverStrategies.newEnvVarFieldResolverStrategy;
import static org.kiwiproject.config.provider.FieldResolverStrategies.newExplicitValueFieldResolverStrategy;
import static org.kiwiproject.config.provider.FieldResolverStrategies.newExternalPropertyFieldResolverStrategy;
import static org.kiwiproject.config.provider.FieldResolverStrategies.newSupplierFieldResolverStrategy;
import static org.kiwiproject.config.provider.FieldResolverStrategies.newSystemPropertyFieldResolverStrategy;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;
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

@DisplayName("MongoConfigProvider")
class MongoConfigProviderTest {

    private static final String MONGO_CONNECTION = "db1.test:27017,db2.test:27017";

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
                addSystemProperty(MongoConfigProvider.DEFAULT_MONGO_SYSTEM_PROPERTY, MONGO_CONNECTION);

                var provider = MongoConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("url_var", MONGO_CONNECTION);

                var provider = MongoConfigProvider.builder()
                        .resolverStrategy(newSystemPropertyFieldResolverStrategy("url_var"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.SYSTEM_PROPERTY));
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(MongoConfigProvider.DEFAULT_MONGO_ENV_VARIABLE)).thenReturn(MONGO_CONNECTION);

                var provider = MongoConfigProvider.builder().kiwiEnvironment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("url_var")).thenReturn(MONGO_CONNECTION);

                var provider = MongoConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(newEnvVarFieldResolverStrategy("url_var"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers
                        .resourceFilePath("MongoConfigProvider/config.properties"));

                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = MongoConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = MongoConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(newExternalPropertyFieldResolverStrategy("mongo.connection.provided"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.EXTERNAL_PROPERTY));
            }
        }

        @Nested
        class WithExplicitNetwork {

            @Test
            void shouldBuildUsingProvidedNetwork() {
                var provider = MongoConfigProvider.builder()
                        .resolverStrategy(newExplicitValueFieldResolverStrategy(MONGO_CONNECTION))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = MongoConfigProvider.builder()
                        .resolverStrategy(newSupplierFieldResolverStrategy(() -> MONGO_CONNECTION))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getUrl()).isEqualTo(MONGO_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.SUPPLIER));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = MongoConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getUrl()).isNull();
                assertThat(provider.getResolvedBy()).containsExactly(entry("url", ResolvedBy.NONE));
            }

        }

    }
}
