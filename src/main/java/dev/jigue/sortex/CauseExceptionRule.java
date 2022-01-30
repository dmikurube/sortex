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

public class CauseExceptionRule {
    private CauseExceptionRule(
            final ExceptionRule exceptionRule,
            final boolean isDirect) {
        this.exceptionRule = exceptionRule;
        this.isDirect = isDirect;
    }

    public static CauseExceptionRule from(final Map<String, Object> map) {
        if (!map.containsKey("direct")) {
            throw new IllegalArgumentException("Exception in cause does not contain \"direct\".");
        }
        final Object directObject = map.get("direct");
        if (directObject == null) {
            throw new NullPointerException("Exception in cause contains \"direct\" that is null.");
        }
        if (!(directObject instanceof Boolean)) {
            throw new ClassCastException("Exception in cause contains \"direct\" that is not boolean.");
        }
        return new CauseExceptionRule(ExceptionRule.from(map), (Boolean) directObject);
    }

    public boolean matches(final Throwable exception) {
        Throwable cause = exception.getCause();
        do {
            if (this.exceptionRule.matches(cause)) {
                return true;
            }
            cause = cause.getCause();
        } while (cause != null && !this.isDirect);
        return false;
    }

    public Map<String, Object> toMap() {
        final LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.putAll(this.exceptionRule.toMap());
        map.put("direct", this.isDirect);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CauseExceptionRule.class, this.exceptionRule, this.isDirect);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof CauseExceptionRule)) {
            return false;
        }
        final CauseExceptionRule other = (CauseExceptionRule) otherObject;

        return Objects.equals(this.exceptionRule, other.exceptionRule)
                && Objects.equals(this.isDirect, other.isDirect);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    private final ExceptionRule exceptionRule;
    private final boolean isDirect;
}
