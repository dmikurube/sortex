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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ClassEqualsLiterallyRule extends ClassRule {
    private ClassEqualsLiterallyRule(final String className) {
        this.className = className;
    }

    public static ClassEqualsLiterallyRule of(final String className) {
        return new ClassEqualsLiterallyRule(className);
    }

    public static ClassEqualsLiterallyRule from(final Object classNameObject)
            throws ClassCastException, ClassNotFoundException, NullPointerException {
        if (classNameObject == null) {
            throw new NullPointerException("Value of \"" + KEY + "\" is null.");
        }
        if (!(classNameObject instanceof String)) {
            throw new ClassCastException("Value of \"" + KEY + "\" is not a string.");
        }

        return of((String) classNameObject);
    }

    @Override
    public boolean matches(final Throwable exception) {
        return this.className.equals(exception.getClass().getName());
    }

    @Override
    public Map<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(KEY, this.className);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ClassEqualsLiterallyRule.class, this.className);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof ClassEqualsLiterallyRule)) {
            return false;
        }
        final ClassEqualsLiterallyRule other = (ClassEqualsLiterallyRule) otherObject;
        return Objects.equals(this.className, other.className);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    public static final String KEY = "class_equals_literally";

    private final String className;
}
