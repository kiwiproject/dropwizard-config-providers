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

@DisplayName("ActiveMQConfigProvider")
class ActiveMQConfigProviderTest {

    private static final String AMQ_CONNECTION = "msg1.test:61616,msg2.test:61616";

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
                addSystemProperty(ActiveMQConfigProvider.DEFAULT_AMQ_SERVERS_SYSTEM_PROPERTY, AMQ_CONNECTION);

                var provider = ActiveMQConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("bar", AMQ_CONNECTION);

                var provider = ActiveMQConfigProvider.builder()
                        .resolverStrategy(newSystemPropertyFieldResolverStrategy("bar"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.SYSTEM_PROPERTY));
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ActiveMQConfigProvider.DEFAULT_AMQ_SERVERS_ENV_VARIABLE)).thenReturn(AMQ_CONNECTION);

                var provider = ActiveMQConfigProvider.builder().kiwiEnvironment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn(AMQ_CONNECTION);

                var provider = ActiveMQConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(newEnvVarFieldResolverStrategy("baz"))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers
                        .resourceFilePath("ActiveMQConfigProvider/config.properties"));

                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = ActiveMQConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = ActiveMQConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(newExternalPropertyFieldResolverStrategy("amq.connection.provided"))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.EXTERNAL_PROPERTY));
            }
        }

        @Nested
        class WithExplicitNetwork {

            @Test
            void shouldBuildUsingProvidedNetwork() {
                var provider = ActiveMQConfigProvider.builder()
                        .resolverStrategy(newExplicitValueFieldResolverStrategy(AMQ_CONNECTION))
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = ActiveMQConfigProvider.builder()
                        .resolverStrategy(newSupplierFieldResolverStrategy(() -> AMQ_CONNECTION))
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getActiveMQServers()).isEqualTo(AMQ_CONNECTION);
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.SUPPLIER));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = ActiveMQConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getActiveMQServers()).isNull();
                assertThat(provider.getResolvedBy()).containsExactly(entry("activeMQServers", ResolvedBy.NONE));
            }

        }

    }
}
