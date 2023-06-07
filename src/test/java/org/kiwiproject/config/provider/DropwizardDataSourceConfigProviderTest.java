package org.kiwiproject.config.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.kiwiproject.collect.KiwiMaps.newHashMap;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.addSystemProperty;
import static org.kiwiproject.config.provider.util.SystemPropertyHelper.clearAllSystemProperties;
import static org.kiwiproject.test.constants.KiwiTestConstants.JSON_HELPER;
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

import java.nio.file.Path;
import java.util.Map;

@DisplayName("DropwizardDataSourceConfigProvider")
class DropwizardDataSourceConfigProviderTest {

    private static final String DRIVER_CLASS = "org.postgresql.Driver";
    private static final String URL = "jdbc://localhost:5432/test-db";
    private static final String USER = "kiwi";
    private static final String PASSWORD = "secret";
    private static final Map<String, String> ORM_PROPERTIES = Map.of("prop", "value");

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
                addSystemProperty(DropwizardDataSourceConfigProvider.DEFAULT_ORM_PROPERTIES_SYSTEM_PROPERTY, JSON_HELPER.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder().build();
                assertThat(provider.canProvide()).isTrue();

                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_PROPERTY);
            }

            @Test
            void shouldBuildUsingProvidedSystemPropertyKey() {
                addSystemProperty("driver_class_var", DRIVER_CLASS);
                addSystemProperty("url_var", URL);
                addSystemProperty("user_var", USER);
                addSystemProperty("password_var", PASSWORD);
                addSystemProperty("max_size_var", "1");
                addSystemProperty("min_size_var", "0");
                addSystemProperty("initial_size_var", "0");
                addSystemProperty("orm_properties_var", JSON_HELPER.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .driverClassResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("driver_class_var").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("url_var").build())
                        .userResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("user_var").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().systemPropertyKey("password_var").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("max_size_var").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("min_size_var").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().systemPropertyKey("initial_size_var").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().systemPropertyKey("orm_properties_var").build())
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
                when(env.getenv(DropwizardDataSourceConfigProvider.DEFAULT_ORM_PROPERTIES_ENV_VARIABLE)).thenReturn(JSON_HELPER.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .build();

                assertThat(provider.canProvide()).isTrue();
                assertFactoryIsCorrect(provider.getDataSourceFactory(), provider, ResolvedBy.SYSTEM_ENV);
            }

            @Test
            void shouldBuildUsingProvidedEnvVariable() {
                var env = mock(KiwiEnvironment.class);
                when(env.getenv("driver_class_var")).thenReturn(DRIVER_CLASS);
                when(env.getenv("url_var")).thenReturn(URL);
                when(env.getenv("user_var")).thenReturn(USER);
                when(env.getenv("password_var")).thenReturn(PASSWORD);
                when(env.getenv("max_size_var")).thenReturn("1");
                when(env.getenv("min_size_var")).thenReturn("0");
                when(env.getenv("initial_size_var")).thenReturn("0");
                when(env.getenv("orm_properties_var")).thenReturn(JSON_HELPER.toJson(ORM_PROPERTIES));

                var provider = DropwizardDataSourceConfigProvider.builder()
                        .kiwiEnvironment(env)
                        .driverClassResolver(FieldResolverStrategy.<String>builder().envVariable("driver_class_var").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().envVariable("url_var").build())
                        .userResolver(FieldResolverStrategy.<String>builder().envVariable("user_var").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().envVariable("password_var").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("max_size_var").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("min_size_var").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().envVariable("initial_size_var").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().envVariable("orm_properties_var").build())
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
                        .driverClassResolver(FieldResolverStrategy.<String>builder().externalProperty("driver_class_var").build())
                        .urlResolver(FieldResolverStrategy.<String>builder().externalProperty("url_var").build())
                        .userResolver(FieldResolverStrategy.<String>builder().externalProperty("user_var").build())
                        .passwordResolver(FieldResolverStrategy.<String>builder().externalProperty("password_var").build())
                        .maxSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("max_size_var").build())
                        .minSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("min_size_var").build())
                        .initialSizeResolver(FieldResolverStrategy.<Integer>builder().externalProperty("initial_size_var").build())
                        .ormPropertyResolver(FieldResolverStrategy.<Map<String, String>>builder().externalProperty("orm_properties_var").build())
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
            void shouldBuildUsingDefaultSupplier() {
                var provider = DropwizardDataSourceConfigProvider.builder().build();
                assertThat(provider.canProvide()).isFalse();

                var defaultFactory = new DataSourceFactory();
                var factory = provider.getDataSourceFactory();

                assertThat(factory.getDriverClass()).isNull();
                assertThat(factory.getUrl()).isBlank();
                assertThat(factory.getUser()).isBlank();
                assertThat(factory.getPassword()).isBlank();
                assertThat(factory.getMaxSize()).isEqualTo(defaultFactory.getMaxSize());
                assertThat(factory.getMinSize()).isEqualTo(defaultFactory.getMinSize());
                assertThat(factory.getInitialSize()).isEqualTo(defaultFactory.getInitialSize());
                assertThat(factory.getProperties()).isEqualTo(defaultFactory.getProperties());
                assertThat(provider.getResolvedBy()).contains(
                        entry("driverClass", ResolvedBy.NONE),
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

                assertThat(factory.getDriverClass()).isNull();
                assertThat(factory.getUrl()).isEqualTo(URL);
                assertThat(factory.getUser()).isBlank();
                assertThat(factory.getPassword()).isBlank();
                assertThat(factory.getMaxSize()).isEqualTo(defaultFactory.getMaxSize());
                assertThat(factory.getMinSize()).isEqualTo(defaultFactory.getMinSize());
                assertThat(factory.getInitialSize()).isEqualTo(defaultFactory.getInitialSize());
                assertThat(factory.getProperties()).isEqualTo(defaultFactory.getProperties());
                assertThat(provider.getResolvedBy()).contains(
                        entry("driverClass", ResolvedBy.NONE),
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
