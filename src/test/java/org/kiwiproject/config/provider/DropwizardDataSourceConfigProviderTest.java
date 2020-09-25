package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.json.JsonHelper;

import java.nio.file.Path;
import java.util.Map;

@DisplayName("DropwizardDataSourceConfigProvider")
class DropwizardDataSourceConfigProviderTest {

    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String URL = "jdbc://localhost:5432/test-db";
    private static final String USER = "kiwi";
    private static final String PASSWORD = "secret";
    private static final Map<String, String> ORM_PROPERTIES = newHashMap("prop", "value");

    private JsonHelper json;

    @BeforeEach
    void setUp() {
        json = new JsonHelper();
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
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_DRIVER_CLASS_SYSTEM_PROPERTY, DRIVER_CLASS);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_URL_SYSTEM_PROPERTY, URL);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_USER_SYSTEM_PROPERTY, USER);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_PASSWORD_SYSTEM_PROPERTY, PASSWORD);
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_MAX_SIZE_SYSTEM_PROPERTY, "1");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_MIN_SIZE_SYSTEM_PROPERTY, "0");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_INITIAL_SIZE_SYSTEM_PROPERTY, "0");
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_ORM_PROPERTIES_SYSTEM_PROPERTY, json.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();

                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("a", DRIVER_CLASS);
                addSystemProperty("b", URL);
                addSystemProperty("c", USER);
                addSystemProperty("d", PASSWORD);
                addSystemProperty("e", "1");
                addSystemProperty("f", "0");
                addSystemProperty("g", "0");
                addSystemProperty("h", json.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .driverClassResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("a").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("b").build())
                        .userResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("c").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("d").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("e").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("f").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("g").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().systemPropertyKey("h").build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_PROPERTY);
            }

        }

        @Nested
        class WithEnvironmentVariable {

            @Test
            void shouldBuildUsingDefaultEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_DRIVER_CLASS_ENV_VARIABLE)).thenReturn(DRIVER_CLASS);
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_URL_ENV_VARIABLE)).thenReturn(URL);
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_USER_ENV_VARIABLE)).thenReturn(USER);
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_PASSWORD_ENV_VARIABLE)).thenReturn(PASSWORD);
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_MAX_SIZE_ENV_VARIABLE)).thenReturn("1");
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_MIN_SIZE_ENV_VARIABLE)).thenReturn("0");
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_INITIAL_SIZE_ENV_VARIABLE)).thenReturn("0");
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_ORM_PROPERTIES_ENV_VARIABLE)).thenReturn(json.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("a")).thenReturn(DRIVER_CLASS);
                when(env.getenv("b")).thenReturn(URL);
                when(env.getenv("c")).thenReturn(USER);
                when(env.getenv("d")).thenReturn(PASSWORD);
                when(env.getenv("e")).thenReturn("1");
                when(env.getenv("f")).thenReturn("0");
                when(env.getenv("g")).thenReturn("0");
                when(env.getenv("h")).thenReturn(json.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .driverClassResolver(FieldResolverStrategy.<String>builder().envVariable("a").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().envVariable("b").build())
                        .userResolver(FieldResolverStrategy.<String>builder().envVariable("c").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().envVariable("d").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("e").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("f").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("g").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().envVariable("h").build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_ENV);
            }

        }

        @Nested
        class WithExternalProperty {

            private ExternalConfigProvider externalConfigProvider;

            @BeforeEach
            void setUp() {
                var propertyPath = Path.of(ResourceHelpers.resourceFilePath("DropwizardDataSourceConfigProvider/config.properties"));
                externalConfigProvider = ExternalConfigProvider.builder().explicitPath(propertyPath).build();
            }

            @Test
            void shouldBuildUsingDefaultExternalProperty() {
                var provider = DropwizardDataSourceConfigProvider.builder().externalConfigProvider(externalConfigProvider).build();
                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.EXTERNAL_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedExternalProperty() {
                var provider = DropwizardDataSourceConfigProvider.builder()
                        .externalConfigProvider(externalConfigProvider)
                        .driverClassResolver(FieldResolverStrategy.<String>builder().externalProperty("a").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().externalProperty("b").build())
                        .userResolver(FieldResolverStrategy.<String>builder().externalProperty("c").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().externalProperty("d").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("e").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("f").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("g").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().externalProperty("h").build())
                        .build();
                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.EXTERNAL_PROPERTY);
            }
        }

        @Nested
        class WithExplicitValues {

            @Test
            void shouldBuildUsingProvidedValues() {
                var provider = DropwizardDataSourceConfigProvider.builder()
                        .driverClassResolver(FieldResolverStrategy.<String>builder().explicitValue(DRIVER_CLASS).build())
                        .urlResolver(FieldResolverStrategy.<String>builder().explicitValue(URL).build())
                        .userResolver(FieldResolverStrategy.<String>builder().explicitValue(USER).build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().explicitValue(PASSWORD).build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().explicitValue(1).build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().explicitValue(0).build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().explicitValue(0).build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().explicitValue(newHashMap("prop", "value")).build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.EXPLICIT_VALUE);
            }

        }

        @Nested
        class WithSupplier {

            @Test
            void shouldBuildUsingProvidedSupplier() {
                var provider = DropwizardDataSourceConfigProvider.builder()
                        .driverClassResolver(FieldResolverStrategy.<String>builder().valueSupplier(() -> DRIVER_CLASS).build())
                        .urlResolver(FieldResolverStrategy.<String>builder().valueSupplier(() -> URL).build())
                        .userResolver(FieldResolverStrategy.<String>builder().valueSupplier(() -> USER).build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().valueSupplier(() -> PASSWORD).build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().valueSupplier(() -> 1).build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().valueSupplier(() -> 0).build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().valueSupplier(() -> 0).build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().valueSupplier(() -> newHashMap("prop", "value")).build())
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SUPPLIER);
            }

            @Test
            void shouldBuildUsingDefaultSupplierAndCanProvide() {
                var provider = DropwizardDataSourceConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();

                var defaultFactory = new DataSourceFactory();
                var factory = provider.getDataSourceFactory();

                assertThat(factory.getDriverClass()).isBlank();
                assertThat(factory.getUrl()).isBlank();
                assertThat(factory.getUser()).isBlank();
                assertThat(factory.getPassword()).isBlank();
                assertThat(factory.getMaxSize()).isEqualTo(defaultFactory.getMaxSize());
                assertThat(factory.getMinSize()).isEqualTo(defaultFactory.getMinSize());
                assertThat(factory.getInitialSize()).isEqualTo(defaultFactory.getInitialSize());
                assertThat(factory.getProperties()).isEqualTo(defaultFactory.getProperties());
                assertThat(provider.getResolvedBy()).contains(
                        entry("driverClass", ResolvedBy.PROVIDER_DEFAULT),
                        entry("url", ResolvedBy.PROVIDER_DEFAULT),
                        entry("user", ResolvedBy.NONE),
                        entry("password", ResolvedBy.NONE),
                        entry("maxSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("minSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("initialSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("ormProperties", ResolvedBy.PROVIDER_DEFAULT)
                );
            }

        }

        @Nested
        class WithProvidedDataSourceFactory {

            @Test
            void shouldBuildUsingTheProvidedFactoryAsDefault() {
                var defaultFactory = new DataSourceFactory();
                defaultFactory.setUrl(URL);

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .dataSourceFactorySupplier(() -> defaultFactory)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                var factory = provider.getDataSourceFactory();

                assertThat(factory.getDriverClass()).isBlank();
                assertThat(factory.getUrl()).isEqualTo(URL);
                assertThat(factory.getUser()).isBlank();
                assertThat(factory.getPassword()).isBlank();
                assertThat(factory.getMaxSize()).isEqualTo(defaultFactory.getMaxSize());
                assertThat(factory.getMinSize()).isEqualTo(defaultFactory.getMinSize());
                assertThat(factory.getInitialSize()).isEqualTo(defaultFactory.getInitialSize());
                assertThat(factory.getProperties()).isEqualTo(defaultFactory.getProperties());
                assertThat(provider.getResolvedBy()).contains(
                        entry("driverClass", ResolvedBy.PROVIDER_DEFAULT),
                        entry("url", ResolvedBy.PROVIDER_DEFAULT),
                        entry("user", ResolvedBy.NONE),
                        entry("password", ResolvedBy.NONE),
                        entry("maxSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("minSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("initialSize", ResolvedBy.PROVIDER_DEFAULT),
                        entry("ormProperties", ResolvedBy.PROVIDER_DEFAULT)
                );
            }
        }

    }

    private void assertFactoryIsCorrect(DataSourceFactory factory, ConfigProvider provider, ResolvedBy resolution) {
        assertThat(factory.getDriverClass()).isEqualTo(DRIVER_CLASS);
        assertThat(factory.getUrl()).isEqualTo(URL);
        assertThat(factory.getUser()).isEqualTo(USER);
        assertThat(factory.getPassword()).isEqualTo(PASSWORD);
        assertThat(factory.getMaxSize()).isEqualTo(1);
        assertThat(factory.getMinSize()).isZero();
        assertThat(factory.getInitialSize()).isZero();
        assertThat(factory.getProperties()).containsAllEntriesOf(ORM_PROPERTIES);
        assertThat(provider.getResolvedBy()).contains(
                entry("driverClass", resolution),
                entry("url", resolution),
                entry("user", resolution),
                entry("password", resolution),
                entry("maxSize", resolution),
                entry("minSize", resolution),
                entry("initialSize", resolution),
                entry("ormProperties", resolution)
        );
    }
}
