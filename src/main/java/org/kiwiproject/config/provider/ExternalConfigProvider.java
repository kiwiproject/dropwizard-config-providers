package org.kiwiproject.config.provider;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.collect.KiwiMaps.isNotNullOrEmpty;

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Config provider that looks up configuration values from a known properties file. This provider loads the properties
 * so that other providers can access the values.
 * <p>
 * The provider will look for the external config file in the following order:
 * <ol>
 *     <li>System property with the given system property key</li>
 *     <li>System property with the default system property key (kiwi.external.config.path)</li>
 *     <li>Environment variable with the given variable name</li>
 *     <li>Environment variable with the default variable name (KIWI_EXTERNAL_CONFIG_PATH)</li>
 *     <li>The given properties path</li>
 *     <li>The default properties path (~/.kiwi.external.config.properties)</li>
 * </ol>
 */
@Slf4j
public class ExternalConfigProvider implements ConfigProvider {

    @VisibleForTesting
    static final Path DEFAULT_CONFIG_PATH = Paths.get(System.getProperty("user.home"), ".kiwi.external.config.properties");

    @VisibleForTesting
    static final String DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY = "kiwi.external.config.path";

    @VisibleForTesting
    static final String DEFAULT_CONFIG_PATH_ENV_VARIABLE = "KIWI_EXTERNAL_CONFIG_PATH";

    @Getter(AccessLevel.PACKAGE)
    private Path propertiesPath;

    private Properties properties;

    /**
     * Builds a new ExternalConfigProvider
     *
     * @param explicitPath        An explicit path to the external properties file
     * @param systemPropertyKey   A System property key that resolves the path to the external properties file
     * @param envVariable         A variable name that resolves the path to the external properties file from the system environment
     * @param environment         The {@link KiwiEnvironment} to use for resolving environment variables
     *
     * @see ExternalConfigProvider#DEFAULT_CONFIG_PATH
     * @see ExternalConfigProvider#DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY
     * @see ExternalConfigProvider#DEFAULT_CONFIG_PATH_ENV_VARIABLE
     */
    @Builder
    private ExternalConfigProvider(Path explicitPath, String systemPropertyKey, String envVariable, KiwiEnvironment environment) {
        var kiwiEnvironment = isNull(environment) ? new DefaultEnvironment() : environment;
        var configPathEnvVariable = isBlank(envVariable) ? DEFAULT_CONFIG_PATH_ENV_VARIABLE : envVariable;
        var configPathSystemPropertyKey = isBlank(systemPropertyKey) ? DEFAULT_CONFIG_PATH_SYSTEM_PROPERTY : systemPropertyKey;

        var pathFromSystemProperties = System.getProperty(configPathSystemPropertyKey);
        var pathFromEnv = kiwiEnvironment.getenv(configPathEnvVariable);

        if (isNotBlank(pathFromSystemProperties)) {
            setPropertiesPath(Path.of(pathFromSystemProperties));
        } else if (isNotBlank(pathFromEnv)) {
            setPropertiesPath(Path.of(pathFromEnv));
        } else if (nonNull(explicitPath)) {
            setPropertiesPath(explicitPath);
        } else {
            setPropertiesPath(DEFAULT_CONFIG_PATH);
        }
    }

    /**
     * This provider can provide if the properties have been loaded from the given file
     *
     * @return {@code true} if properties are loaded, {@code false} otherwise.
     */
    @Override
    public boolean canProvide() {
        return isNotNullOrEmpty(properties);
    }

    private void setPropertiesPath(Path configPath) {
        this.propertiesPath = configPath;
        updateProperties();
    }

    private void updateProperties() {
        properties = new Properties();

        if (Files.isReadable(propertiesPath)) {
            try (var reader = Files.newBufferedReader(propertiesPath)) {
                LOG.debug("Looking up configuration values from file {}", propertiesPath);
                properties.load(reader);
            } catch (IOException e) {
                LOG.error("Unable to load properties from file: {}", propertiesPath, e);
            }
        }
    }

    /**
     * Returns a property for a given key if it exists otherwise an {@link Optional#empty()}.
     *
     * @param propertyKey the key of the property to look up
     * @return An {@link Optional} with the requested property or empty if not found
     */
    public Optional<String> getProperty(String propertyKey) {
        return canProvide() ? Optional.ofNullable(properties.getProperty(propertyKey)) : Optional.empty();
    }

    /**
     * Executes given consumer if the requested property is found, otherwise runs the {@code orElse} function.
     *
     * @param propertyKey           the key of the property to look up
     * @param propertyValueConsumer a consumer to process the value of the property if found
     * @param orElse                a runnable to execute if the property is not found
     */
    public void usePropertyIfPresent(String propertyKey, Consumer<String> propertyValueConsumer, Runnable orElse) {
        var propertyValue = getProperty(propertyKey);
        propertyValue.ifPresentOrElse(propertyValueConsumer, orElse);
    }

    /**
     * Executes a given function if the requested property is found, otherwise runs the {@code orElseSupplier} supplier.
     *
     * @param propertyKey           the key of the property to look up
     * @param propertyValueFunction a function to process the value of the property if found and return a {@link ResolverResult}
     * @param orElseSupplier        a supplier to execute if the property is not found and return a {@link ResolverResult}
     * @param <T>                   The class type of the value inside the ResolverResult
     * @return                      a {@link ResolverResult} containing the resolved value and the resolution method
     */
    public <T> ResolverResult<T> resolveExternalProperty(String propertyKey,
                                                         Function<String, ResolverResult<T>> propertyValueFunction,
                                                         Supplier<ResolverResult<T>> orElseSupplier) {
        var propertyValue = getProperty(propertyKey);
        return propertyValue.map(propertyValueFunction).orElseGet(orElseSupplier);
    }

    /**
     * Will check if a given {@link ExternalConfigProvider} is null and return a new one if so. Otherwise return the one
     * passed in.
     *
     * @param provided an {@link ExternalConfigProvider} to test for null
     * @return A new {@link ExternalConfigProvider} if the given {@code provided} is null or {@code provided}
     */
    public static ExternalConfigProvider getExternalPropertyProviderOrDefault(ExternalConfigProvider provided) {
        return nonNull(provided) ? provided : ExternalConfigProvider.builder().build();
    }
}
