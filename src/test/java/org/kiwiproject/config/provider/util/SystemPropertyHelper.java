package org.kiwiproject.config.provider.util;

import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class SystemPropertyHelper {

    private static final Set<String> systemProperties = new HashSet<>();

    public static void addSystemProperty(String key, String value) {
        System.setProperty(key, value);
        systemProperties.add(key);
    }

    public static void clearAllSystemProperties() {
        systemProperties.forEach(System::clearProperty);
    }
}
