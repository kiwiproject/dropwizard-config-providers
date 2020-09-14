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

@DisplayName("ServiceIdentityProvider")
class ServiceIdentityProviderTest {

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
                addSystemProperty(ServiceIdentityProvider.DEFAULT_NAME_SYSTEM_PROPERTY, "systemprop-default-service");
                addSystemProperty(ServiceIdentityProvider.DEFAULT_VERSION_SYSTEM_PROPERTY, "0.1.0");
                addSystemProperty(ServiceIdentityProvider.DEFAULT_ENVIRONMENT_SYSTEM_PROPERTY, "development");

                var provider = ServiceIdentityProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("systemprop-default-service");
                assertThat(provider.getVersion()).isEqualTo("0.1.0");
                assertThat(provider.getEnvironment()).isEqualTo("development");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.SYSTEM_PROPERTY),
                        entry("version", ResolvedBy.SYSTEM_PROPERTY),
                        entry("environment", ResolvedBy.SYSTEM_PROPERTY)
                );
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("foo", "systemprop-provided-service");
                addSystemProperty("bar", "0.1.1");
                addSystemProperty("baz", "dev-int");

                var provider = ServiceIdentityProvider.builder()
                        .nameSystemPropertyKey("foo")
                        .versionSystemPropertyKey("bar")
                        .environmentSystemPropertyKey("baz")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("systemprop-provided-service");
                assertThat(provider.getVersion()).isEqualTo("0.1.1");
                assertThat(provider.getEnvironment()).isEqualTo("dev-int");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.SYSTEM_PROPERTY),
                        entry("version", ResolvedBy.SYSTEM_PROPERTY),
                        entry("environment", ResolvedBy.SYSTEM_PROPERTY)
                );
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ServiceIdentityProvider.DEFAULT_NAME_ENV_VARIABLE)).thenReturn("env-default-service");
                when(env.getenv(ServiceIdentityProvider.DEFAULT_VERSION_ENV_VARIABLE)).thenReturn("0.2.0");
                when(env.getenv(ServiceIdentityProvider.DEFAULT_ENVIRONMENT_ENV_VARIABLE)).thenReturn("test");

                var provider = ServiceIdentityProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("env-default-service");
                assertThat(provider.getVersion()).isEqualTo("0.2.0");
                assertThat(provider.getEnvironment()).isEqualTo("test");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.SYSTEM_ENV),
                        entry("version", ResolvedBy.SYSTEM_ENV),
                        entry("environment", ResolvedBy.SYSTEM_ENV)
                );
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("foo")).thenReturn("env-provide-service");
                when(env.getenv("bar")).thenReturn("0.2.1");
                when(env.getenv("baz")).thenReturn("test-int");

                var provider = ServiceIdentityProvider.builder()
                        .kiwiEnvironment(env)
                        .nameEnvVariable("foo")
                        .versionEnvVariable("bar")
                        .environmentEnvVariable("baz")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("env-provide-service");
                assertThat(provider.getVersion()).isEqualTo("0.2.1");
                assertThat(provider.getEnvironment()).isEqualTo("test-int");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.SYSTEM_ENV),
                        entry("version", ResolvedBy.SYSTEM_ENV),
                        entry("environment", ResolvedBy.SYSTEM_ENV)
                );
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalPropertyProvider externalPropertyProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("ServiceIdentityPropertyProvider/config.properties"));
                externalPropertyProvider = ExternalPropertyProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = ServiceIdentityProvider.builder().externalPropertyProvider(externalPropertyProvider).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("external-default-service");
                assertThat(provider.getVersion()).isEqualTo("0.3.0");
                assertThat(provider.getEnvironment()).isEqualTo("stage");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("version", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("environment", ResolvedBy.EXTERNAL_PROPERTY)
                );
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = ServiceIdentityProvider.builder()
                        .externalPropertyProvider(externalPropertyProvider)
                        .nameExternalProperty("service.name.provided")
                        .versionExternalProperty("service.version.provided")
                        .environmentExternalProperty("service.env.provided")
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("external-provided-service");
                assertThat(provider.getVersion()).isEqualTo("0.3.1");
                assertThat(provider.getEnvironment()).isEqualTo("staging");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("version", ResolvedBy.EXTERNAL_PROPERTY),
                        entry("environment", ResolvedBy.EXTERNAL_PROPERTY)
                );
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = ServiceIdentityProvider.builder()
                        .serviceName("explicit-service")
                        .serviceVersion("0.4.0")
                        .serviceEnvironment("training")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("explicit-service");
                assertThat(provider.getVersion()).isEqualTo("0.4.0");
                assertThat(provider.getEnvironment()).isEqualTo("training");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.EXPLICIT_VALUE),
                        entry("version", ResolvedBy.EXPLICIT_VALUE),
                        entry("environment", ResolvedBy.EXPLICIT_VALUE)
                );
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = ServiceIdentityProvider.builder()
                        .nameSupplier(() -> "supplier-service")
                        .versionSupplier(() -> "0.5.0")
                        .environmentSupplier(() -> "production")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getName()).isEqualTo("supplier-service");
                assertThat(provider.getVersion()).isEqualTo("0.5.0");
                assertThat(provider.getEnvironment()).isEqualTo("production");
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.DEFAULT),
                        entry("version", ResolvedBy.DEFAULT),
                        entry("environment", ResolvedBy.DEFAULT)
                );
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCannotProvide() {
                var provider = ServiceIdentityProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getName()).isEmpty();
                assertThat(provider.getVersion()).isEmpty();
                assertThat(provider.getEnvironment()).isEmpty();
                assertThat(provider.getResolvedBy()).contains(
                        entry("name", ResolvedBy.NONE),
                        entry("version", ResolvedBy.NONE),
                        entry("environment", ResolvedBy.NONE)
                );
            }

        }

    }
}
