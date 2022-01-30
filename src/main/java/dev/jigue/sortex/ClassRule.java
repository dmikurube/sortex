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
import java.util.stream.Collectors;

public abstract class ClassRule {
    public static ClassRule from(final Map<String, Object> map) {
        ClassRule classRule = null;

        final ArrayList<String> specifiedClassRules = new ArrayList<>();
        final ArrayList<Exception> suppressions = new ArrayList<>();
        if (map.containsKey(ClassEqualsRule.KEY)) {
            specifiedClassRules.add(ClassEqualsRule.KEY);
            try {
                classRule = ClassEqualsRule.from(map.get(ClassEqualsRule.KEY));
            } catch (final Exception ex) {
                suppressions.add(ex);
            }
        }
        if (map.containsKey(ClassExtendsRule.KEY)) {
            specifiedClassRules.add(ClassExtendsRule.KEY);
            try {
                classRule = ClassExtendsRule.from(map.get(ClassExtendsRule.KEY));
            } catch (final Exception ex) {
                suppressions.add(ex);
            }
        }
        if (map.containsKey(ClassEqualsLiterallyRule.KEY)) {
            specifiedClassRules.add(ClassEqualsLiterallyRule.KEY);
            try {
                classRule = ClassEqualsLiterallyRule.from(map.get(ClassEqualsLiterallyRule.KEY));
            } catch (final Exception ex) {
                suppressions.add(ex);
            }
        }

        if (specifiedClassRules.size() == 1 && classRule != null && suppressions.isEmpty()) {
            return classRule;
        }

        final IllegalArgumentException exception;
        if (specifiedClassRules.size() == 0) {
            exception = new IllegalArgumentException("No class rule specified.");
        } else if (specifiedClassRules.size() > 1) {
            exception = new IllegalArgumentException(
                    "Class rules "
                        + specifiedClassRules.stream().collect(Collectors.joining(", ", "[", "]"))
                        + " cannot co-exist.");
        } else {
            exception = new IllegalArgumentException("Invalid class rule: " + specifiedClassRules.get(0));
        }
        for (final Exception suppressed : suppressions) {
            exception.addSuppressed(suppressed);
        }
        throw exception;
    }

    public abstract boolean matches(Throwable exception);

    public abstract Map<String, Object> toMap();
}
