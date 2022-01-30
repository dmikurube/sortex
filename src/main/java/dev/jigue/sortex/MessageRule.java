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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class MessageRule {
    public static Optional<MessageRule> from(final Map<String, Object> map) {
        MessageRule messageRule = null;

        final ArrayList<String> specifiedMessageRules = new ArrayList<>();
        final ArrayList<Exception> suppressions = new ArrayList<>();
        if (map.containsKey(MessageMatchesEitherOfRule.KEY)) {
            specifiedMessageRules.add(MessageMatchesEitherOfRule.KEY);
            try {
                messageRule = MessageMatchesEitherOfRule.from(map.get(MessageMatchesEitherOfRule.KEY));
            } catch (final Exception ex) {
                suppressions.add(ex);
            }
        }

        if (specifiedMessageRules.size() <= 1 && suppressions.isEmpty()) {
            return Optional.ofNullable(messageRule);
        }

        final IllegalArgumentException exception;
        if (specifiedMessageRules.size() > 1) {
            exception = new IllegalArgumentException(
                    "Message rules "
                        + specifiedMessageRules.stream().collect(Collectors.joining(", ", "[", "]"))
                        + " cannot co-exist.");
        } else {
            exception = new IllegalArgumentException("Invalid message rule: " + specifiedMessageRules.get(0));
        }
        for (final Exception suppressed : suppressions) {
            exception.addSuppressed(suppressed);
        }
        throw exception;
    }

    public abstract boolean matches(Throwable messaage);

    public abstract Map<String, Object> toMap();
}
