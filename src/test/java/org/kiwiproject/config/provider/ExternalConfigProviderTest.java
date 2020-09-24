package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
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

@DisplayName("ExternalConfigProvider")
class ExternalConfigProviderTest {

    private ExternalConfigProvider provider;
    private Path propertyPath;

    @BeforeEach
    void setUp() {
        propertyPath = Path.of(ResourceHelpers.resourceFilePath("ExternalConfigProvider/config.properties"));
        provider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
    }

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
                addSystemProperty(ExternalConfigProvider.DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY, propertyPath.toString());

                var provider = ExternalConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("bar", propertyPath.toString());

                var provider = ExternalConfigProvider.builder().systemPropertyKey("bar").build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(ExternalConfigProvider.DEFAULT_CONFIG_PATH_ENV_VARIABLE)).thenReturn(propertyPath.toString());

                var provider = ExternalConfigProvider.builder().environment(env).build();
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("baz")).thenReturn(propertyPath.toString());

                var provider = ExternalConfigProvider.builder()
                        .environment(env)
                        .envVariable("baz")
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

        }

        @Nested
        class WithExplicitPath {

            @Test
            void shouldBuildUsingProvidedPath() {
                assertThat(provider.canProvide()).isTrue();
                assertThat(provider.getPropertiesPath()).isEqualTo(propertyPath);
            }

        }

        @Nested
        class WithDefaultPath {

            @Test
            void shouldBuildUsingDefaultPath() {
                var provider = ExternalConfigProvider.builder().build();

                assertThat(provider.canProvide()).isFalse();
                assertThat(provider.getPropertiesPath()).isEqualTo(ExternalConfigProvider.DEFAULT_CONFIG_PATH);
            }

        }

    }

    @Nested
    class GetProperty {

        @Test
        void shouldReturnPropertyValue_WhenFound() {
            var propertyValue = provider.getProperty("unit.test.foo");
            assertThat(propertyValue).hasValue("bar");
        }

        @Test
        void shouldReturnOptionalEmpty_WhenNotFound() {
            var propertyValue = provider.getProperty("unit.test.baz");
            assertThat(propertyValue).isNotPresent();
        }
    }

    @Nested
    class UsePropertyIfPresent {

        @Test
        void shouldCallConsumer_WhenFound() {
            provider.usePropertyIfPresent("unit.test.foo",
                    value -> assertThat(value).isEqualTo("bar"),
                    () -> fail("Test should have called the consumer not the else"));
        }

        @Test
        @SuppressWarnings("java:S3415")
        void shouldCallOrElse_WhenNotFound() {
            provider.usePropertyIfPresent("unit.test.baz",
                    value -> fail("Test should have called the else not the consumer"),
                    () -> assertThat(true).isTrue());
        }
    }

    @Nested
    class ResolveExternalProperty {

        @Test
        void shouldCallFunction_WhenFound() {
            var result = provider.resolveExternalProperty("unit.test.foo",
                    value -> new ResolverResult<>(value, ResolvedBy.EXTERNAL_PROPERTY),
                    () -> new ResolverResult<>("Nope not gonna happen", ResolvedBy.PROVIDER_DEFAULT));

            assertThat(result.getValue()).isEqualTo("bar");
            assertThat(result.getResolvedBy()).isEqualTo(ResolvedBy.EXTERNAL_PROPERTY);
        }

        @Test
        @SuppressWarnings("java:S3415")
        void shouldCallOrElse_WhenNotFound() {
            var result = provider.resolveExternalProperty("unit.test.baz",
                    value -> new ResolverResult<>("Should not work", ResolvedBy.EXTERNAL_PROPERTY),
                    () -> new ResolverResult<>("This is my default", ResolvedBy.PROVIDER_DEFAULT));

            assertThat(result.getValue()).isEqualTo("This is my default");
            assertThat(result.getResolvedBy()).isEqualTo(ResolvedBy.PROVIDER_DEFAULT);
        }
    }
}
