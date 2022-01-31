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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class CauseMatchesEitherOfRule extends CauseRule {
    private CauseMatchesEitherOfRule(final List<CauseExceptionRule> causeRules) {
        this.causeRules = Collections.unmodifiableList(new ArrayList<>(causeRules));
    }

    static CauseMatchesEitherOfRule of(final List<CauseExceptionRule> causeRules) {
        return new CauseMatchesEitherOfRule(causeRules);
    }

    static CauseMatchesEitherOfRule of(final CauseExceptionRule... causeRules) {
        return CauseMatchesEitherOfRule.of(Arrays.asList(causeRules));
    }

    static CauseMatchesEitherOfRule from(final Object causeRulesObject) {
        if (causeRulesObject == null) {
            throw new NullPointerException("Value of \"" + KEY + "\" is null.");
        }

        final ArrayList<CauseExceptionRule> causeRules = new ArrayList<>();
        if (causeRulesObject instanceof List) {
            for (final Object causeRuleObject : (List) causeRulesObject) {
                if (causeRuleObject == null || !(causeRuleObject instanceof Map)) {
                    throw new ClassCastException("Value of \"" + KEY + "\" does not consist of maps.");
                }
                causeRules.add(buildCauseExceptionRuleFromMap((Map) causeRuleObject));
            }
        } else {
            throw new ClassCastException("Value of \"" + KEY + "\" is not a list/array/sequence.");
        }

        return CauseMatchesEitherOfRule.of(causeRules);
    }

    @Override
    boolean matches(final Throwable exception) {
        for (final CauseExceptionRule causeRule : this.causeRules) {
            if (causeRule.matches(exception)) {
                return true;
            }
        }
        return false;
    }

    @Override
    Map<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(KEY, this.causeRules);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CauseMatchesEitherOfRule.class, this.causeRules);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof CauseMatchesEitherOfRule)) {
            return false;
        }
        final CauseMatchesEitherOfRule other = (CauseMatchesEitherOfRule) otherObject;

        return Objects.equals(this.causeRules, other.causeRules);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    @SuppressWarnings("unchecked")
    private static CauseExceptionRule buildCauseExceptionRuleFromMap(final Map map) {
        for (final Object key : map.keySet()) {
            if (!(key instanceof String)) {
                throw new ClassCastException("Value of \"" + KEY + "\" contains a map whose key is not a string.");
            }
        }
        return CauseExceptionRule.from((Map<String, Object>) map);
    }

    static final String KEY = "cause_matches_either_of";

    private final List<CauseExceptionRule> causeRules;
}
