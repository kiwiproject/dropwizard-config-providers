package org.kiwiproject.config.provider;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.db.DataSourceFactory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.config.provider.util.PropertyResolutionSettings;
import org.kiwiproject.config.provider.util.SinglePropertyResolver;
import org.kiwiproject.json.JsonHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Config provider that determines the configuration for a Dropwizard data source object.
 * <p>
 * Default resolution lookup keys can be found in the constants for this class
 * @see SinglePropertyResolver for resolution order
 */
public class DropwizardDataSourceConfigProvider implements ConfigProvider {

    private static final String SYSTEM_PROPERTY = "systemProperty";
    private static final String ENV_PROPERTY = "envVariable";
    private static final String EXTERNAL_PROPERTY = "externalProperty";

    private static final String DRIVER_CLASS_FIELD = "driverClass";
    private static final String URL_FIELD = "url";
    private static final String USER_FIELD = "user";
    private static final String PASSWORD_FIELD = "password";
    private static final String MAX_SIZE_FIELD = "maxSize";
    private static final String MIN_SIZE_FIELD = "minSize";
    private static final String INITIAL_SIZE_FIELD = "initialSize";
    private static final String ORM_PROPERTIES_FIELD = "ormProperties";

    @VisibleForTesting
    static final String DEFAULT_DRIVER_CLASS_SYSTEM_PROPERTY = "kiwi.datasource.driverClass";

    @VisibleForTesting
    static final String DEFAULT_DRIVER_CLASS_ENV_VARIABLE = "KIWI_DATASOURCE_DRIVER_CLASS";

    @VisibleForTesting
    static final String DEFAULT_DRIVER_CLASS_EXTERNAL_PROPERTY_KEY = "datasource.driverClass";

    @VisibleForTesting
    static final String DEFAULT_URL_SYSTEM_PROPERTY = "kiwi.datasource.url";

    @VisibleForTesting
    static final String DEFAULT_URL_ENV_VARIABLE = "KIWI_DATASOURCE_URL";

    @VisibleForTesting
    static final String DEFAULT_URL_EXTERNAL_PROPERTY_KEY = "datasource.url";

    @VisibleForTesting
    static final String DEFAULT_USER_SYSTEM_PROPERTY = "kiwi.datasource.user";

    @VisibleForTesting
    static final String DEFAULT_USER_ENV_VARIABLE = "KIWI_DATASOURCE_USER";

    @VisibleForTesting
    static final String DEFAULT_USER_EXTERNAL_PROPERTY_KEY = "datasource.user";

    @VisibleForTesting
    static final String DEFAULT_PASSWORD_SYSTEM_PROPERTY = "kiwi.datasource.password";

    @VisibleForTesting
    static final String DEFAULT_PASSWORD_ENV_VARIABLE = "KIWI_DATASOURCE_PASSWORD";

    @VisibleForTesting
    static final String DEFAULT_PASSWORD_EXTERNAL_PROPERTY_KEY = "datasource.password";

    @VisibleForTesting
    static final String DEFAULT_MAX_SIZE_SYSTEM_PROPERTY = "kiwi.datasource.maxSize";

    @VisibleForTesting
    static final String DEFAULT_MAX_SIZE_ENV_VARIABLE = "KIWI_DATASOURCE_MAX_SIZE";

    @VisibleForTesting
    static final String DEFAULT_MAX_SIZE_EXTERNAL_PROPERTY_KEY = "datasource.maxSize";

    @VisibleForTesting
    static final String DEFAULT_MIN_SIZE_SYSTEM_PROPERTY = "kiwi.datasource.minSize";

    @VisibleForTesting
    static final String DEFAULT_MIN_SIZE_ENV_VARIABLE = "KIWI_DATASOURCE_MIN_SIZE";

    @VisibleForTesting
    static final String DEFAULT_MIN_SIZE_EXTERNAL_PROPERTY_KEY = "datasource.minSize";

    @VisibleForTesting
    static final String DEFAULT_INITIAL_SIZE_SYSTEM_PROPERTY = "kiwi.datasource.initialSize";

    @VisibleForTesting
    static final String DEFAULT_INITIAL_SIZE_ENV_VARIABLE = "KIWI_DATASOURCE_INITIAL_SIZE";

    @VisibleForTesting
    static final String DEFAULT_INITIAL_SIZE_EXTERNAL_PROPERTY_KEY = "datasource.initialSize";

    @VisibleForTesting
    static final String DEFAULT_ORM_PROPERTIES_SYSTEM_PROPERTY = "kiwi.datasource.ormProperties";

    @VisibleForTesting
    static final String DEFAULT_ORM_PROPERTIES_ENV_VARIABLE = "KIWI_DATASOURCE_ORM_PROPERTIES";

    @VisibleForTesting
    static final String DEFAULT_ORM_PROPERTIES_EXTERNAL_PROPERTY_KEY = "datasource.ormProperties";

    private static final Map<String, String> DRIVER_CLASS_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_DRIVER_CLASS_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_DRIVER_CLASS_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_DRIVER_CLASS_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> URL_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_URL_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_URL_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_URL_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> USER_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_USER_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_USER_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_USER_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> PASSWORD_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_PASSWORD_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_PASSWORD_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_PASSWORD_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> MAX_SIZE_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_MAX_SIZE_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_MAX_SIZE_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_MAX_SIZE_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> MIN_SIZE_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_MIN_SIZE_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_MIN_SIZE_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_MIN_SIZE_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> INITIAL_SIZE_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_INITIAL_SIZE_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_INITIAL_SIZE_ENV_VARIABLE,
            EXTERNAL_PROPERTY, DEFAULT_INITIAL_SIZE_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, String> ORM_PROPERTIES_DEFAULTS = Map.of(
            SYSTEM_PROPERTY, DEFAULT_ORM_PROPERTIES_SYSTEM_PROPERTY,
            ENV_PROPERTY, DEFAULT_ORM_PROPERTIES_ENV_VARIABLE, EXTERNAL_PROPERTY,
            DEFAULT_ORM_PROPERTIES_EXTERNAL_PROPERTY_KEY);

    private static final Map<String, Map<String, String>> DEFAULTS_FOR_PROPERTIES = Map.of(
            DRIVER_CLASS_FIELD, DRIVER_CLASS_DEFAULTS,
            URL_FIELD, URL_DEFAULTS,
            USER_FIELD, USER_DEFAULTS,
            PASSWORD_FIELD, PASSWORD_DEFAULTS,
            MAX_SIZE_FIELD, MAX_SIZE_DEFAULTS,
            MIN_SIZE_FIELD, MIN_SIZE_DEFAULTS,
            INITIAL_SIZE_FIELD, INITIAL_SIZE_DEFAULTS,
            ORM_PROPERTIES_FIELD, ORM_PROPERTIES_DEFAULTS
    );

    @Getter
    private final DataSourceFactory dataSourceFactory;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy driverClassResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy urlResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy userResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy passwordResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy maxSizeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy minSizeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy initialSizeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy ormPropertiesResolvedBy;

    @SuppressWarnings("java:S107")
    @Builder
    private DropwizardDataSourceConfigProvider(ExternalConfigProvider externalConfigProvider,
                                               KiwiEnvironment kiwiEnvironment,
                                               FieldResolverStrategy<String> driverClassResolver,
                                               FieldResolverStrategy<String> urlResolver,
                                               FieldResolverStrategy<String> userResolver,
                                               FieldResolverStrategy<String> passwordResolver,
                                               FieldResolverStrategy<Integer> maxSizeResolver,
                                               FieldResolverStrategy<Integer> minSizeResolver,
                                               FieldResolverStrategy<Integer> initialSizeResolver,
                                               FieldResolverStrategy<Map<String, String>> ormPropertyResolver,
                                               Supplier<DataSourceFactory> dataSourceFactorySupplier) {
        var originalFactory = getSuppliedFactoryOrDefault(dataSourceFactorySupplier);

        dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass(resolveProperty(DRIVER_CLASS_FIELD, driverClassResolver, externalConfigProvider,
                kiwiEnvironment, originalFactory.getDriverClass(), this::setDriverClassResolvedBy));
        dataSourceFactory.setUrl(resolveProperty(URL_FIELD, urlResolver, externalConfigProvider, kiwiEnvironment,
                originalFactory.getUrl(), this::setUrlResolvedBy));
        dataSourceFactory.setUser(resolveProperty(USER_FIELD, userResolver, externalConfigProvider, kiwiEnvironment,
                originalFactory.getUser(), this::setUserResolvedBy));
        dataSourceFactory.setPassword(resolveProperty(PASSWORD_FIELD, passwordResolver, externalConfigProvider,
                kiwiEnvironment, originalFactory.getPassword(), this::setPasswordResolvedBy));
        dataSourceFactory.setMaxSize(resolveProperty(MAX_SIZE_FIELD, maxSizeResolver, externalConfigProvider,
                kiwiEnvironment, originalFactory.getMaxSize(), this::setMaxSizeResolvedBy, Integer::parseInt));
        dataSourceFactory.setMinSize(resolveProperty(MIN_SIZE_FIELD, minSizeResolver, externalConfigProvider,
                kiwiEnvironment, originalFactory.getMinSize(), this::setMinSizeResolvedBy, Integer::parseInt));
        dataSourceFactory.setInitialSize(resolveProperty(INITIAL_SIZE_FIELD, initialSizeResolver, externalConfigProvider,
                kiwiEnvironment, originalFactory.getInitialSize(), this::setInitialSizeResolvedBy, Integer::parseInt));

        var json = new JsonHelper();

        var resolvedProperties = resolveProperty(ORM_PROPERTIES_FIELD, ormPropertyResolver, externalConfigProvider,
                kiwiEnvironment, new HashMap<>(), this::setOrmPropertiesResolvedBy,
                value -> json.toMap(value, new TypeReference<>() {
                }));

        var mergedProperties = new HashMap<>(originalFactory.getProperties());
        mergedProperties.putAll(resolvedProperties);
        dataSourceFactory.setProperties(mergedProperties);
    }

    private DataSourceFactory getSuppliedFactoryOrDefault(Supplier<DataSourceFactory> dataSourceFactorySupplier) {
        setAllResolvedByToProviderDefault();

        if (nonNull(dataSourceFactorySupplier)) {
            return dataSourceFactorySupplier.get();
        }

        return new DataSourceFactory();
    }

    private void setAllResolvedByToProviderDefault() {
        driverClassResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        urlResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        userResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        passwordResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        maxSizeResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        minSizeResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        initialSizeResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
        ormPropertiesResolvedBy = ResolvedBy.PROVIDER_DEFAULT;
    }

    private String resolveProperty(String fieldName,
                                   FieldResolverStrategy<String> resolver,
                                   ExternalConfigProvider externalConfigProvider,
                                   KiwiEnvironment kiwiEnvironment,
                                   String originalValue,
                                   Consumer<ResolvedBy> resolvedBySetter) {

        return resolveProperty(fieldName, resolver, externalConfigProvider, kiwiEnvironment, originalValue,
                resolvedBySetter, value -> value);
    }

    private <T> T resolveProperty(String fieldName,
                                  FieldResolverStrategy<T> resolver,
                                  ExternalConfigProvider externalConfigProvider,
                                  KiwiEnvironment kiwiEnvironment,
                                  T originalValue,
                                  Consumer<ResolvedBy> resolvedBySetter,
                                  Function<String, T> convertFromString) {

        var defaultFields = DEFAULTS_FOR_PROPERTIES.get(fieldName);

        var resolution = SinglePropertyResolver.resolveProperty(PropertyResolutionSettings.<T>builder()
                .externalConfigProvider(externalConfigProvider)
                .kiwiEnvironment(kiwiEnvironment)
                .resolverStrategy(resolver)
                .systemProperty(defaultFields.get(SYSTEM_PROPERTY))
                .environmentVariable(defaultFields.get(ENV_PROPERTY))
                .externalKey(defaultFields.get(EXTERNAL_PROPERTY))
                .defaultValue(originalValue)
                .convertFromString(convertFromString)
                .build());

        resolvedBySetter.accept(resolution.getResolvedBy());
        return resolution.getValue();
    }

    @Override
    public boolean canProvide() {
        return isNotEmpty(dataSourceFactory.getUrl());
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(
                DRIVER_CLASS_FIELD, driverClassResolvedBy,
                URL_FIELD, urlResolvedBy,
                USER_FIELD, userResolvedBy,
                PASSWORD_FIELD, passwordResolvedBy,
                MAX_SIZE_FIELD, maxSizeResolvedBy,
                MIN_SIZE_FIELD, minSizeResolvedBy,
                INITIAL_SIZE_FIELD, initialSizeResolvedBy,
                ORM_PROPERTIES_FIELD, ormPropertiesResolvedBy
        );
    }
}
