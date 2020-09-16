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

@DisplayName("ZooKeeperConfigProvider")
class ZooKeeperConfigProviderTest {

    private static final String ZOOKEEPER_CONNECT_STRING = "zoo.test:2181,zoo2.test:2181";

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
                addSystemProperty(ZooKeeperConfigProvider.DEFAULT_CONNECT_STRING_SYSTEM_PROPERTY, ZOOKEEPER_CONNECT_STRING);

                var provider = ZooKeeperConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("bar", ZOOKEEPER_CONNECT_STRING);

                var resolver = FieldResolverStrategy.<String>builder().systemPropertyKey("bar").build();
                var provider = ZooKeeperConfigProvider.builder().resolverStrategy(resolver).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.SYSTEM_PROPERTY));
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ZooKeeperConfigProvider.DEFAULT_CONNECT_STRING_ENV_VARIABLE)).thenReturn(ZOOKEEPER_CONNECT_STRING);

                var provider = ZooKeeperConfigProvider.builder().kiwiEnvironment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn(ZOOKEEPER_CONNECT_STRING);

                var resolver = FieldResolverStrategy.<String>builder().envVariable("baz").build();
                var provider = ZooKeeperConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(resolver)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers
                        .resourceFilePath("ZooKeeperConfigProvider/config.properties"));

                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = ZooKeeperConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var resolver = FieldResolverStrategy.<String>builder().externalProperty("zookeeper.connection.provided").build();
                var provider = ZooKeeperConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(resolver)
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.EXTERNAL_PROPERTY));
            }
        }

        @Nested
        class WithExplicitNetwork {

            @Test
            void shouldBuildUsingProvidedNetwork() {
                var resolver = FieldResolverStrategy.<String>builder().explicitValue(ZOOKEEPER_CONNECT_STRING).build();
                var provider = ZooKeeperConfigProvider.builder().resolverStrategy(resolver).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var resolver = FieldResolverStrategy.<String>builder()
                        .valueSupplier(() -> ZOOKEEPER_CONNECT_STRING)
                        .build();

                var provider = ZooKeeperConfigProvider.builder()
                        .resolverStrategy(resolver)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getConnectString()).isEqualTo(ZOOKEEPER_CONNECT_STRING);
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.DEFAULT));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = ZooKeeperConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getConnectString()).isEmpty();
                assertThat(provider.getResolvedBy()).containsExactly(entry("connectString", ResolvedBy.NONE));
            }

        }

    }
}
