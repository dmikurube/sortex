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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class TestClassRule {
    @Test
    public void testEquality() {
        assertNotEquals(ClassEqualsRule.of(IOException.class), ClassExtendsRule.of(IOException.class));
        assertNotEquals(ClassEqualsRule.of(IOException.class), ClassEqualsRule.of(UncheckedIOException.class));
        assertNotEquals(ClassExtendsRule.of(IOException.class), ClassExtendsRule.of(UncheckedIOException.class));
    }

    @Test
    public void testExactClass() {
        assertClassRule(
                ClassEqualsRule.of(IOException.class),
                "class_equals", "java.io.IOException");
    }

    @Test
    public void testLiterallyExactClass() {
        assertClassRule(
                ClassEqualsLiterallyRule.of("dev.jigue.sortex.DummyException"),
                "class_equals_literally", "dev.jigue.sortex.DummyException");
    }

    @Test
    public void testSubclassOf() {
        assertClassRule(
                ClassExtendsRule.of(IOException.class),
                "class_extends", "java.io.IOException");
    }

    @Test
    public void testEmpty() {
        try {
            ClassRule.from(new HashMap<>());
            fail("No expected Exception is thrown.");
        } catch (final IllegalArgumentException ex) {
            assertEquals("No class rule specified.", ex.getMessage());
        }
    }

    @Test
    public void testInvalidValue() {
        try {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("class_equals", new HashMap<String, String>());
            ClassRule.from(map);
            fail("No expected Exception is thrown.");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Invalid class rule: class_equals", ex.getMessage());
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof ClassCastException);
            assertEquals("Value of \"class_equals\" is not a string.", cause.getMessage());
            assertEquals(0, ex.getSuppressed().length);
        }
    }

    @Test
    public void testDuplicated() {
        try {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("class_equals", "java.io.IOException");
            map.put("class_extends", "java.io.IOException");
            ClassRule.from(map);
            fail("No expected Exception is thrown.");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Class rules [class_equals, class_extends] cannot co-exist.", ex.getMessage());
            assertEquals(null, ex.getCause());
            assertEquals(0, ex.getSuppressed().length);
        }
    }

    @Test
    public void testDuplicatedAndInvalid() {
        try {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("class_equals", "java.io.IOException");
            map.put("class_extends", "dev.jigue.sortex.DummyException");
            ClassRule.from(map);
            fail("No expected Exception is thrown.");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Class rules [class_equals, class_extends] cannot co-exist.", ex.getMessage());
            assertEquals(null, ex.getCause());
            assertEquals(0, ex.getSuppressed().length);
        }
    }

    @Test
    public void testClassNotFound() {
        try {
            final HashMap<String, Object> map = new HashMap<>();
            map.put("class_equals", "dev.jigue.sortex.DummyNonExistingException");
            ClassRule.from(map);
            fail("No expected Exception is thrown.");
        } catch (final IllegalArgumentException ex) {
            assertEquals("Invalid class rule: class_equals", ex.getMessage());
            final Throwable cause = ex.getCause();
            assertTrue(cause instanceof IllegalArgumentException);
            assertEquals("A class \"dev.jigue.sortex.DummyNonExistingException\" is not found.", cause.getMessage());
            final Throwable cause2 = cause.getCause();
            assertTrue(cause2 instanceof ClassNotFoundException);
            assertEquals("dev.jigue.sortex.DummyNonExistingException", cause2.getMessage());
        }
    }

    private static void assertClassRule(final ClassRule expected, final String... source) {
        if (source.length % 2 != 0) {
            fail("assertClassRule received a source of odd elements.");
        }
        final HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < source.length / 2; i++) {
            map.put(source[i * 2], source[i * 2 + 1]);
        }
        final ClassRule actual = ClassRule.from(map);
        assertEquals(expected, actual);
    }
}
