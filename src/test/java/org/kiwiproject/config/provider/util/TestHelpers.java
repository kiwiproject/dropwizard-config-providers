package org.kiwiproject.config.provider.util;

import static org.mockito.Mockito.when;

import lombok.experimental.UtilityClass;
import org.kiwiproject.base.KiwiEnvironment;

@UtilityClass
public class TestHelpers {

    public static void mockEnvToReturn(KiwiEnvironment env, String property, String valueToReturn) {
        when(env.getenv(property)).thenReturn(valueToReturn);
    }
}
