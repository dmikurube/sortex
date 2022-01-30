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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class TestMessageRule {
    @Test
    public void testEquality() {
        assertEquals(MessageMatchesEitherOfRule.of("foo", "/f.*o/"), MessageMatchesEitherOfRule.of("foo", "/f.*o/"));
        assertNotEquals(MessageMatchesEitherOfRule.of("foo", "/f.*o/"), MessageMatchesEitherOfRule.of("bar", "/f.*o/"));
    }

    @Test
    public void testEmpty() {
        final Optional<MessageRule> ruleOptional = MessageRule.from(new HashMap<>());
        assertFalse(ruleOptional.isPresent());
    }

    @Test
    public void testNoPatterns() {
        assertMessageMatchesEitherOfRule(false, "");
        assertMessageMatchesEitherOfRule(false, "foo");
    }

    @Test
    public void testExactMatches() {
        assertMessageMatchesEitherOfRule(true, "foo", "foo", "bar", "baz", "qux");
        assertMessageMatchesEitherOfRule(true, "baz", "foo", "bar", "baz", "qux");
        assertMessageMatchesEitherOfRule(true, "qux", "foo", "bar", "baz", "qux");
        assertMessageMatchesEitherOfRule(false, "hoge", "foo", "bar", "baz", "qux");
    }

    @Test
    public void testMessageRegex() {
        assertMessageMatchesEitherOfRule(true, "foo", "/foo/");
        assertMessageMatchesEitherOfRule(true, "f", "/./");
        assertMessageMatchesEitherOfRule(false, "foo", "/./");
        assertMessageMatchesEitherOfRule(true, "foo", "/f[op][no]/");
    }

    private static void assertMessageMatchesEitherOfRule(
            final boolean expected, final String message, final String... patterns) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("message_matches_either_of", Arrays.asList(patterns));
        final Optional<MessageRule> ruleOptional = MessageRule.from(map);
        assertTrue(ruleOptional.isPresent());
        final MessageRule rule = ruleOptional.get();
        final boolean actual = rule.matches(new Exception(message));
        assertEquals(expected, actual);
    }
}
