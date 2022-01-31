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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class TestSortEx {
    @Test
    public void testEnum() {
        final Set<String> targets = SortEx.checkEnum(TestEnum.class, getValueOfMethod(TestEnum.class));
        assertEquals(4, targets.size());
        assertTrue(targets.contains("ONE"));
        assertTrue(targets.contains("TWO"));
        assertTrue(targets.contains("THREE"));
        assertTrue(targets.contains("DEFAULT"));
    }

    @Test
    public void test() {
        assertSortEx(TestEnum.DEFAULT, "sortex_rules/example1.yml",
                     new IOException("foo"));
        assertSortEx(TestEnum.DEFAULT, "sortex_rules/example1.yml",
                     new UncheckedIOException(new IOException("foo")));
        assertSortEx(TestEnum.DEFAULT, "sortex_rules/example1.yml",
                     new UncheckedIOException("bar", new IOException()));
        assertSortEx(TestEnum.ONE, "sortex_rules/example1.yml",
                     new UncheckedIOException("foo", new IOException()));
        assertSortEx(TestEnum.ONE, "sortex_rules/example1.yml",
                     new UncheckedIOException("foo", new IOException(new RuntimeException("bar"))));

        assertSortEx(TestEnum.DEFAULT, "sortex_rules/example2.yml",
                     new IOException("foo"));
        assertSortEx(TestEnum.DEFAULT, "sortex_rules/example2.yml",
                     new UncheckedIOException(new IOException("foo")));
        assertSortEx(TestEnum.TWO, "sortex_rules/example2.yml",
                     new UncheckedIOException("bar", new IOException()));
        assertSortEx(TestEnum.ONE, "sortex_rules/example2.yml",
                     new UncheckedIOException("foo", new IOException()));
        assertSortEx(TestEnum.ONE, "sortex_rules/example2.yml",
                     new UncheckedIOException("foo", new IOException(new RuntimeException("bar"))));
    }

    @Test
    public void testNonExistingTargetError() {
        try {
            SortEx.from(YamlUtil.loadListFromResource("sortex_rules/bad_non_existing_target.yml"), TestEnum.class);
        } catch (final IllegalArgumentException ex) {
            return;
        }
        fail("No expected Exception is thrown.");
    }

    @Test
    public void testDuplicated() {
        try {
            SortEx.from(YamlUtil.loadListFromResource("sortex_rules/bad_duplicated.yml"), TestEnum.class);
        } catch (final IllegalArgumentException ex) {
            return;
        }
        fail("No expected Exception is thrown.");
    }

    enum TestEnum {
        ONE,
        TWO,
        THREE,
        DEFAULT,
        ;
    }

    private static <E extends Enum<E>> Method getValueOfMethod(final Class<E> enumClass) {
        try {
            return enumClass.getMethod("valueOf", String.class);
        } catch (final NoSuchMethodException ex) {
            throw new IllegalArgumentException("Enum class passed to SortEx is unexpectedly broken.", ex);
        }
    }

    private static void assertSortEx(final TestEnum expected, final String ruleResource, final Throwable exception) {
        final SortEx<TestEnum> sortex = SortEx.from(YamlUtil.loadListFromResource(ruleResource), TestEnum.class);
        assertEquals(expected, sortex.matches(exception, TestEnum.DEFAULT));
    }

    private static void assertSortExError(final String ruleResource) {
    }
}
