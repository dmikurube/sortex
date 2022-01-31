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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

final class ExceptionRule {
    private ExceptionRule(
            final ClassRule classRule,
            final MessageRule messageRule,
            final CauseRule causeRule) {
        this.classRule = classRule;
        this.messageRule = messageRule;
        this.causeRule = causeRule;
    }

    static ExceptionRule from(final Map<String, Object> map) {
        return new ExceptionRule(
                ClassRule.from(map),
                MessageRule.from(map).orElse(null),
                CauseRule.from(map).orElse(null));
    }

    boolean matches(final Throwable cause) {
        return this.classRule.matches(cause)
                && (this.messageRule == null || this.messageRule.matches(cause))
                && (this.causeRule == null || this.causeRule.matches(cause));
    }

    Map<String, Object> toMap() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.putAll(this.classRule.toMap());
        if (this.messageRule != null) {
            map.putAll(this.messageRule.toMap());
        }
        if (this.causeRule != null) {
            map.putAll(this.causeRule.toMap());
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ExceptionRule.class, this.classRule, this.messageRule, this.causeRule);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof ExceptionRule)) {
            return false;
        }
        final ExceptionRule other = (ExceptionRule) otherObject;
        return Objects.equals(this.classRule, other.classRule)
                && Objects.equals(this.messageRule, other.messageRule)
                && Objects.equals(this.causeRule, other.causeRule);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    private final ClassRule classRule;
    private final MessageRule messageRule;  // Nullable
    private final CauseRule causeRule;  // Nullable
}
