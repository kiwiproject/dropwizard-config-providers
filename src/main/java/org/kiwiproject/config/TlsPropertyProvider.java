package org.kiwiproject.config;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Property provider that provides a {@link TlsContextConfiguration}.
 * <p>
 * The provider will look for the fields in the following order:
 * <ol>
 *     <li>System property with the given system property key</li>
 *     <li>System property with the default system property key (see constants ending in SYSTEM_PROPERTY)</li>
 *     <li>Environment variable with the given variable name</li>
 *     <li>Environment variable with the default variable name (see constants ending in ENV_VARIABLE)</li>
 *     <li>The given network</li>
 *     <li>The named network from an external configuration file with the given key</li>
 *     <li>The named network from an external configuration file with the default key (see constants ending in EXTERNAL_PROPERTY_KEY)</li>
 *     <li>The named network from a given supplier</li>
 * </ol>
 */
@Slf4j
public class TlsPropertyProvider implements ConfigProvider {

    private static final String RESOLUTION_VALUE_KEY = "value";
    private static final String RESOLUTION_METHOD_KEY = "method";
    private static final String SYSTEM_PROPERTY = "systemProperty";
    private static final String ENV_PROPERTY = "envVariable";
    private static final String EXTERNAL_PROPERTY = "externalProperty";

    private static final String KEYSTORE_PATH_FIELD = "keyStorePath";
    private static final String KEYSTORE_PASSWORD_FIELD = "keyStorePassword";
    private static final String KEYSTORE_TYPE_FIELD = "keyStoreType";
    private static final String TRUSTSTORE_PATH_FIELD = "trustStorePath";
    private static final String TRUSTSTORE_PASSWORD_FIELD = "trustStorePassword";
    private static final String TRUSTSTORE_TYPE_FIELD = "trustStoreType";
    private static final String VERIFY_HOSTNAME_FIELD = "verifyHostname";
    private static final String PROTOCOL_FIELD = "protocol";
    private static final String SUPPORTED_PROTOCOLS_FIELD = "supportedProtocols";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY = "kiwi.tls.keyStorePath";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_PATH";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PATH_EXTERNAL_PROPERTY_KEY = "tls.keyStorePath";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY = "kiwi.tls.keyStorePassword";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_PASSWORD";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY = "tls.keyStorePassword";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY = "kiwi.tls.keyStoreType";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE = "KIWI_TLS_KEYSTORE_TYPE";

    @VisibleForTesting
    static final String DEFAULT_KEYSTORE_TYPE_EXTERNAL_PROPERTY_KEY = "tls.keyStoreType";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY = "kiwi.tls.trustStorePath";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_PATH";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PATH_EXTERNAL_PROPERTY_KEY = "tls.trustStorePath";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY = "kiwi.tls.trustStorePassword";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_PASSWORD";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY = "tls.trustStorePassword";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY = "kiwi.tls.trustStoreType";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE = "KIWI_TLS_TRUSTSTORE_TYPE";

    @VisibleForTesting
    static final String DEFAULT_TRUSTSTORE_TYPE_EXTERNAL_PROPERTY_KEY = "tls.trustStoreType";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY = "kiwi.tls.verifyHostname";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE = "KIWI_TLS_VERIFY_HOSTNAME";

    @VisibleForTesting
    static final String DEFAULT_VERIFY_HOSTNAME_EXTERNAL_PROPERTY_KEY = "tls.verifyHostname";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_SYSTEM_PROPERTY = "kiwi.tls.protocol";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_ENV_VARIABLE = "KIWI_TLS_PROTOCOL";

    @VisibleForTesting
    static final String DEFAULT_PROTOCOL_EXTERNAL_PROPERTY_KEY = "tls.protocol";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY = "kiwi.tls.supportedProtocols";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE = "KIWI_TLS_SUPPORTED_PROTOCOLS";

    @VisibleForTesting
    static final String DEFAULT_SUPPORTED_PROTOCOLS_EXTERNAL_PROPERTY_KEY = "tls.supportedProtocols";

    private static final Map<String, Map<String, String>> DEFAULTS_FOR_PROPERTIES = Map.of(
            KEYSTORE_PATH_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_KEYSTORE_PATH_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_KEYSTORE_PATH_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_PATH_EXTERNAL_PROPERTY_KEY),
            KEYSTORE_PASSWORD_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY),
            KEYSTORE_TYPE_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_KEYSTORE_TYPE_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_KEYSTORE_TYPE_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_KEYSTORE_TYPE_EXTERNAL_PROPERTY_KEY),
            TRUSTSTORE_PATH_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_PATH_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_TRUSTSTORE_PATH_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_PATH_EXTERNAL_PROPERTY_KEY),
            TRUSTSTORE_PASSWORD_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_PASSWORD_SYSTEM_PROPERTY, ENV_PROPERTY,
                    DEFAULT_TRUSTSTORE_PASSWORD_ENV_VARIABLE, EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_PASSWORD_EXTERNAL_PROPERTY_KEY),
            TRUSTSTORE_TYPE_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_TRUSTSTORE_TYPE_EXTERNAL_PROPERTY_KEY),
            VERIFY_HOSTNAME_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_VERIFY_HOSTNAME_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_VERIFY_HOSTNAME_ENV_VARIABLE,
                    EXTERNAL_PROPERTY, DEFAULT_VERIFY_HOSTNAME_EXTERNAL_PROPERTY_KEY),
            PROTOCOL_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_PROTOCOL_SYSTEM_PROPERTY, ENV_PROPERTY, DEFAULT_PROTOCOL_ENV_VARIABLE, EXTERNAL_PROPERTY,
                    DEFAULT_PROTOCOL_EXTERNAL_PROPERTY_KEY),
            SUPPORTED_PROTOCOLS_FIELD, Map.of(SYSTEM_PROPERTY, DEFAULT_SUPPORTED_PROTOCOLS_SYSTEM_PROPERTY, ENV_PROPERTY,
                    DEFAULT_SUPPORTED_PROTOCOLS_ENV_VARIABLE, EXTERNAL_PROPERTY, DEFAULT_SUPPORTED_PROTOCOLS_EXTERNAL_PROPERTY_KEY)
    );

    @Getter
    private final TlsContextConfiguration tlsContextConfiguration;

    private ResolvedBy tlsContextConfigurationResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStorePathResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStorePasswordResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy keyStoreTypeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStorePathResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStorePasswordResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy trustStoreTypeResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy verifyHostnameResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy protocolResolvedBy;

    @Setter(AccessLevel.PRIVATE)
    private ResolvedBy supportedProtocolsResolvedBy;

    @SuppressWarnings("java:S107")
    @Builder
    private TlsPropertyProvider(ExternalPropertyProvider externalPropertyProvider,
                                KiwiEnvironment kiwiEnvironment,
                                FieldResolverStrategy<String> keyStorePathResolverStrategy,
                                FieldResolverStrategy<String> keyStorePasswordResolverStrategy,
                                FieldResolverStrategy<String> keyStoreTypeResolverStrategy,
                                FieldResolverStrategy<String> trustStorePathResolverStrategy,
                                FieldResolverStrategy<String> trustStorePasswordResolverStrategy,
                                FieldResolverStrategy<String> trustStoreTypeResolverStrategy,
                                FieldResolverStrategy<Boolean> verifyHostnameResolverStrategy,
                                FieldResolverStrategy<String> protocolResolverStrategy,
                                FieldResolverStrategy<List<String>> supportedProtocolsResolverStrategy,
                                Supplier<TlsContextConfiguration> tlsContextConfigurationSupplier) {

        var originalConfiguration = getSuppliedConfigurationOrDefault(tlsContextConfigurationSupplier);

        var resolvedKiwiEnvironment = isNull(kiwiEnvironment) ? new DefaultEnvironment() : kiwiEnvironment;
        var extProvider = getExternalPropertyProviderOrDefault(externalPropertyProvider);

        tlsContextConfiguration = TlsContextConfiguration.builder()
                .protocol(resolveProperty(PROTOCOL_FIELD, getResolverOrDefault(protocolResolverStrategy), extProvider, resolvedKiwiEnvironment,
                        originalConfiguration.getProtocol(), this::setProtocolResolvedBy))
                .keyStorePath(resolveProperty(KEYSTORE_PATH_FIELD, getResolverOrDefault(keyStorePathResolverStrategy), extProvider, resolvedKiwiEnvironment,
                        originalConfiguration.getKeyStorePath(), this::setKeyStorePathResolvedBy))
                .keyStorePassword(resolveProperty(KEYSTORE_PASSWORD_FIELD, getResolverOrDefault(keyStorePasswordResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.getKeyStorePassword(), this::setKeyStorePasswordResolvedBy))
                .keyStoreType(resolveProperty(KEYSTORE_TYPE_FIELD, getResolverOrDefault(keyStoreTypeResolverStrategy), extProvider, resolvedKiwiEnvironment,
                        originalConfiguration.getKeyStoreType(), this::setKeyStoreTypeResolvedBy))
                .trustStorePath(resolveProperty(TRUSTSTORE_PATH_FIELD, getResolverOrDefault(trustStorePathResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.getTrustStorePath(), this::setTrustStorePathResolvedBy))
                .trustStorePassword(resolveProperty(TRUSTSTORE_PASSWORD_FIELD, getResolverOrDefault(trustStorePasswordResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.getTrustStorePassword(), this::setTrustStorePasswordResolvedBy))
                .trustStoreType(resolveProperty(TRUSTSTORE_TYPE_FIELD, getResolverOrDefault(trustStoreTypeResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.getTrustStoreType(), this::setTrustStoreTypeResolvedBy))
                .verifyHostname(resolveProperty(VERIFY_HOSTNAME_FIELD, getResolverOrDefault(verifyHostnameResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.isVerifyHostname(), this::setVerifyHostnameResolvedBy, Boolean::parseBoolean))
                .supportedProtocols(resolveProperty(SUPPORTED_PROTOCOLS_FIELD, getResolverOrDefault(supportedProtocolsResolverStrategy), extProvider,
                        resolvedKiwiEnvironment, originalConfiguration.getSupportedProtocols(),
                        this::setSupportedProtocolsResolvedBy, str -> Arrays.asList(str.split(","))))
                .build();
    }

    private TlsContextConfiguration getSuppliedConfigurationOrDefault(Supplier<TlsContextConfiguration> tlsContextConfigurationSupplier) {
        if (nonNull(tlsContextConfigurationSupplier)) {
            setAllResolvedByTo(ResolvedBy.EXPLICIT_VALUE);
            return tlsContextConfigurationSupplier.get();
        }

        setAllResolvedByTo(ResolvedBy.DEFAULT);
        return TlsContextConfiguration.builder().build();
    }

    private ExternalPropertyProvider getExternalPropertyProviderOrDefault(ExternalPropertyProvider providedProvider) {
        return nonNull(providedProvider) ? providedProvider : ExternalPropertyProvider.builder().build();
    }

    private String resolveProperty(String fieldName,
                                  FieldResolverStrategy<String> resolver,
                                  ExternalPropertyProvider externalPropertyProvider,
                                  KiwiEnvironment kiwiEnvironment,
                                  String originalValue,
                                  Consumer<ResolvedBy> resolvedBySetter) {
        return resolveProperty(fieldName, resolver, externalPropertyProvider, kiwiEnvironment, originalValue, resolvedBySetter, value -> value);
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveProperty(String fieldName,
                                  FieldResolverStrategy<T> resolver,
                                  ExternalPropertyProvider externalPropertyProvider,
                                  KiwiEnvironment kiwiEnvironment,
                                  T originalValue,
                                  Consumer<ResolvedBy> resolvedBySetter,
                                  Function<String, T> convertFromString) {

        var defaultFields = DEFAULTS_FOR_PROPERTIES.get(fieldName);

        var fromSystemProperties = System.getProperty(resolver.getSystemPropertyKeyOrDefault(defaultFields.get(SYSTEM_PROPERTY)));
        var fromEnv = kiwiEnvironment.getenv(resolver.getEnvVariableOrDefault(defaultFields.get(ENV_PROPERTY)));

        if (isNotBlank(fromSystemProperties)) {
            resolvedBySetter.accept(ResolvedBy.SYSTEM_PROPERTY);
            return convertFromString.apply(fromSystemProperties);
        } else if (isNotBlank(fromEnv)) {
            resolvedBySetter.accept(ResolvedBy.SYSTEM_ENV);
            return convertFromString.apply(fromEnv);
        } else if (nonNull(resolver.getExplicitValue())) {
            resolvedBySetter.accept(ResolvedBy.EXPLICIT_VALUE);
            return resolver.getExplicitValue();
        }

        var returnVal = new HashMap<String, Object>();
        externalPropertyProvider.usePropertyIfPresent(resolver.getExternalPropertyOrDefault(defaultFields.get(EXTERNAL_PROPERTY)),
                value -> {
                    returnVal.put(RESOLUTION_VALUE_KEY, convertFromString.apply(value));
                    returnVal.put(RESOLUTION_METHOD_KEY, ResolvedBy.EXTERNAL_PROPERTY);
                },
                () -> {
                    var value = resolver.getValueSupplierOrDefault(originalValue).get();
                    returnVal.put(RESOLUTION_VALUE_KEY, value);
                    returnVal.put(RESOLUTION_METHOD_KEY, isNull(value) ? ResolvedBy.NONE : ResolvedBy.DEFAULT);
                });

        resolvedBySetter.accept((ResolvedBy) returnVal.get(RESOLUTION_METHOD_KEY));
        return (T) returnVal.get(RESOLUTION_VALUE_KEY);
    }

    private <T> FieldResolverStrategy<T> getResolverOrDefault(FieldResolverStrategy<T> providedStrategy) {
        return isNull(providedStrategy) ? FieldResolverStrategy.<T>builder().build() : providedStrategy;
    }

    private void setAllResolvedByTo(ResolvedBy resolvedBy) {
        tlsContextConfigurationResolvedBy = resolvedBy;
        keyStorePathResolvedBy = resolvedBy;
        keyStorePasswordResolvedBy = resolvedBy;
        keyStoreTypeResolvedBy = resolvedBy;
        trustStorePathResolvedBy = resolvedBy;
        trustStorePasswordResolvedBy = resolvedBy;
        trustStoreTypeResolvedBy = resolvedBy;
        verifyHostnameResolvedBy = resolvedBy;
        protocolResolvedBy = resolvedBy;
        supportedProtocolsResolvedBy = resolvedBy;
    }

    @Override
    public boolean canProvide() {
        return nonNull(tlsContextConfiguration);
    }

    @Override
    public Map<String, ResolvedBy> getResolvedBy() {
        return Map.of(
                "tlsContextConfiguration", tlsContextConfigurationResolvedBy,
                KEYSTORE_PATH_FIELD, keyStorePathResolvedBy,
                KEYSTORE_PASSWORD_FIELD, keyStorePasswordResolvedBy,
                KEYSTORE_TYPE_FIELD, keyStoreTypeResolvedBy,
                TRUSTSTORE_PATH_FIELD, trustStorePathResolvedBy,
                TRUSTSTORE_PASSWORD_FIELD, trustStorePasswordResolvedBy,
                TRUSTSTORE_TYPE_FIELD, trustStoreTypeResolvedBy,
                VERIFY_HOSTNAME_FIELD, verifyHostnameResolvedBy,
                PROTOCOL_FIELD, protocolResolvedBy,
                SUPPORTED_PROTOCOLS_FIELD, supportedProtocolsResolvedBy
        );
    }
}
