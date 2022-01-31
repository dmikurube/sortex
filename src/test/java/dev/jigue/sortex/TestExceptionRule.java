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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;

public class TestExceptionRule {
    @Test
    public void test() {
        final ExceptionRule rule = ExceptionRule.from(YamlUtil.loadMapFromResource("exception_rules/example.yml"));

        assertFalse(rule.matches(new IOException("foo")));
        assertFalse(rule.matches(new UncheckedIOException(new IOException("foo"))));
        assertFalse(rule.matches(new UncheckedIOException("bar", new IOException())));
        assertTrue(rule.matches(new UncheckedIOException("foo", new IOException())));
        assertTrue(rule.matches(new UncheckedIOException("foo", new IOException(new RuntimeException("bar")))));
    }
}
