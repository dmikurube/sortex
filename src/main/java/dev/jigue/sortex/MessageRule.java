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

import java.util.Map;
import java.util.Optional;

abstract class MessageRule {
    static Optional<MessageRule> from(final Map<String, Object> map) {
        if (map.containsKey(MessageMatchesEitherOfRule.KEY)) {
            try {
                return Optional.of(MessageMatchesEitherOfRule.from(map.get(MessageMatchesEitherOfRule.KEY)));
            } catch (final RuntimeException ex) {
                throw new IllegalArgumentException("Invalid message rule: " + MessageMatchesEitherOfRule.KEY, ex);
            }
        }
        return Optional.empty();
    }

    abstract boolean matches(Throwable messaage);

    abstract Map<String, Object> toMap();
}
