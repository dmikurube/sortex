/*
 * Copyright 2022 Dai MIKURUBE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.jigue.sortex;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

final class YamlUtil {
    private YamlUtil() {
        // No instantiation.
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> loadList(final InputStream input) {
        final List list = YAML.loadAs(input, List.class);
        assertList(list);
        return (List<Map<String, Object>>) list;
    }

    public static List<Map<String, Object>> loadListFromResource(final String resourceName) {
        final ClassLoader classLoader = YamlUtil.class.getClassLoader();
        if (classLoader == null) {
            return loadList(ClassLoader.getSystemResourceAsStream(resourceName));
        }
        return loadList(classLoader.getResourceAsStream(resourceName));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadMap(final InputStream input) {
        final Map map = YAML.loadAs(input, Map.class);
        assertMap(map);
        return (Map<String, Object>) map;
    }

    public static Map<String, Object> loadMapFromResource(final String resourceName) {
        final ClassLoader classLoader = YamlUtil.class.getClassLoader();
        if (classLoader == null) {
            return loadMap(ClassLoader.getSystemResourceAsStream(resourceName));
        }
        return loadMap(classLoader.getResourceAsStream(resourceName));
    }

    private static void assertList(final List list) {
        for (final Object elementObject : list) {
            assertTrue(elementObject instanceof Map);
            assertMap((Map) elementObject);
        }
    }

    private static void assertMap(final Map map) {
        for (final Object entryObject : map.entrySet()) {
            assertTrue(entryObject instanceof Map.Entry);
            final Map.Entry entry = (Map.Entry) entryObject;
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("YAML contains a non-String key.");
            }
            final Object value = entry.getValue();
            if (!(value instanceof Number || value instanceof String || value instanceof List || value instanceof Map)) {
                throw new IllegalArgumentException("YAML contains an invalid value.");
            }
        }
    }

    private static final Yaml YAML = new Yaml();
}
