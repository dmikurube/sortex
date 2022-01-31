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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

final class MessageMatchesEitherOfRule extends MessageRule {
    private MessageMatchesEitherOfRule(final List<String> patterns) {
        this.patterns = Collections.unmodifiableList(new ArrayList<>(patterns));

        final ArrayList<Pattern> regexPatterns = new ArrayList<>();
        final LinkedHashSet<String> exactPatterns = new LinkedHashSet<>();
        for (final String pattern : patterns) {
            final int length = pattern.length();
            if (length >= 2 && pattern.startsWith("/") && pattern.endsWith("/")) {
                regexPatterns.add(Pattern.compile(pattern.substring(1, length - 1)));
            } else {
                exactPatterns.add(pattern);
            }
        }
        this.regexPatterns = Collections.unmodifiableList(regexPatterns);
        this.exactPatterns = Collections.unmodifiableSet(exactPatterns);
    }

    static MessageMatchesEitherOfRule of(final List<String> patterns) {
        return new MessageMatchesEitherOfRule(patterns);
    }

    static MessageMatchesEitherOfRule of(final String... patterns) {
        return MessageMatchesEitherOfRule.of(Arrays.asList(patterns));
    }

    static MessageMatchesEitherOfRule from(final Object patternsObject) {
        if (patternsObject == null) {
            throw new NullPointerException("Value of \"" + KEY + "\" is null.");
        }

        final ArrayList<String> patterns = new ArrayList<>();
        if (patternsObject instanceof String) {
            patterns.add((String) patternsObject);
        } else if (patternsObject instanceof List) {
            for (final Object patternObject : (List) patternsObject) {
                if (patternObject == null || !(patternObject instanceof String)) {
                    throw new ClassCastException("Value of \"" + KEY + "\" does not consist of strings.");
                }
                patterns.add((String) patternObject);
            }
        } else {
            throw new ClassCastException("Value of \"" + KEY + "\" is neither a string nor a list/array/sequence.");
        }

        return MessageMatchesEitherOfRule.of(patterns);
    }

    @Override
    boolean matches(final Throwable exception) {
        final String message = exception.getMessage();
        if (this.exactPatterns.contains(message)) {
            return true;
        }
        for (final Pattern regexPattern : this.regexPatterns) {
            if (regexPattern.matcher(message).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    Map<String, Object> toMap() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(KEY, this.patterns);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MessageMatchesEitherOfRule.class, this.patterns);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof MessageMatchesEitherOfRule)) {
            return false;
        }
        final MessageMatchesEitherOfRule other = (MessageMatchesEitherOfRule) otherObject;

        // Its equality is defined only with the original pattern strings because
        // {@code java.util.regex.Pattern} does not provide appropriate {@code #equals()} with another {@code Pattern} instance.
        return Objects.equals(this.patterns, other.patterns);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    static final String KEY = "message_matches_either_of";

    private final List<String> patterns;

    private final List<Pattern> regexPatterns;
    private final Set<String> exactPatterns;
}
