package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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

@DisplayName("SharedStorageConfigProvider")
class SharedStorageConfigProviderTest {

    private static final String SHARED_STORAGE_PATH = "/tmp/shared";

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
                addSystemProperty(SharedStorageConfigProvider.DEFAULT_SHARED_STORAGE_PATH_SYSTEM_PROPERTY, SHARED_STORAGE_PATH);

                var provider = SharedStorageConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.SYSTEM_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("path_var", SHARED_STORAGE_PATH);

                var resolver = FieldResolverStrategy.<String>builder().systemPropertyKey("path_var").build();
                var provider = SharedStorageConfigProvider.builder().resolverStrategy(resolver).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.SYSTEM_PROPERTY));
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(SharedStorageConfigProvider.DEFAULT_SHARED_STORAGE_PATH_ENV_VARIABLE)).thenReturn(SHARED_STORAGE_PATH);

                var provider = SharedStorageConfigProvider.builder().kiwiEnvironment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.SYSTEM_ENV));
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("path_var")).thenReturn(SHARED_STORAGE_PATH);

                var resolver = FieldResolverStrategy.<String>builder().envVariable("path_var").build();
                var provider = SharedStorageConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .resolverStrategy(resolver)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.SYSTEM_ENV));
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers
                        .resourceFilePath("SharedStorageConfigProvider/config.properties"));

                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = SharedStorageConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.EXTERNAL_PROPERTY));
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var resolver = FieldResolverStrategy.<String>builder().externalProperty("shared.storage.path.provided").build();
                var provider = SharedStorageConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .resolverStrategy(resolver)
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.EXTERNAL_PROPERTY));
            }
        }

        @Nested
        class WithExplicitNetwork {

            @Test
            void shouldBuildUsingProvidedNetwork() {
                var resolver = FieldResolverStrategy.<String>builder().explicitValue(SHARED_STORAGE_PATH).build();
                var provider = SharedStorageConfigProvider.builder().resolverStrategy(resolver).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.EXPLICIT_VALUE));
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var resolver = FieldResolverStrategy.<String>builder()
                        .valueSupplier(() -> SHARED_STORAGE_PATH)
                        .build();

                var provider = SharedStorageConfigProvider.builder()
                        .resolverStrategy(resolver)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getSharedStoragePath()).isEqualTo(SHARED_STORAGE_PATH);
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.SUPPLIER));
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = SharedStorageConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getSharedStoragePath()).isNull();
                assertThat(provider.getResolvedBy()).containsExactly(entry("sharedStoragePath", ResolvedBy.NONE));
            }

        }

    }
}
