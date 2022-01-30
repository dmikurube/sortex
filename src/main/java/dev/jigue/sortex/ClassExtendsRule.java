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

public final class ClassExtendsRule extends ClassRule {
    private ClassExtendsRule(final Class<? extends Throwable> clazz) {
        this.clazz = clazz;
    }

    public static ClassExtendsRule of(final Class<? extends Throwable> clazz) {
        return new ClassExtendsRule(clazz);
    }

    public static ClassExtendsRule from(final Object classNameObject)
            throws ClassCastException, ClassNotFoundException, NullPointerException  {
        if (classNameObject == null) {
            throw new NullPointerException("Value of \"" + KEY + "\" is null.");
        }
        if (!(classNameObject instanceof String)) {
            throw new ClassCastException("Value of \"" + KEY + "\" is not a string.");
        }

        final Class<?> clazz = Class.forName((String) classNameObject);
        if (Throwable.class.isAssignableFrom(clazz)) {
            return of(castToThrowableClass(clazz));
        }
        throw new ClassCastException("Class " + clazz.getName() + " is not Throwable.");
    }

    @Override
    public boolean matches(final Throwable exception) {
        return this.clazz.isAssignableFrom(exception.getClass());
    }

    @Override
    public Map<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(KEY, this.clazz.getName());
        return Collections.unmodifiableMap(map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ClassExtendsRule.class, this.clazz);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof ClassExtendsRule)) {
            return false;
        }
        final ClassExtendsRule other = (ClassExtendsRule) otherObject;
        return Objects.equals(this.clazz, other.clazz);
    }

    @Override
    public String toString() {
        return this.toMap().toString();
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Throwable> castToThrowableClass(final Class<?> clazz) {
        return (Class<? extends Throwable>) clazz;
    }

    public static final String KEY = "class_extends";

    private final Class<? extends Throwable> clazz;
}
