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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Sort/categorize a {@link java.lang.Throwable} object into {@code enum} based on a {@link java.util.Map}-based domain specific language (DSL).
 *
 * <blockquote><pre>{@code  [
 *    {
 *      "class_equals": "java.io.UncheckedIOException",  // Exact match.
 *      "message_matches_either_of": [
 *        "Unexpected"  // Exact match.
 *      ],
 *      "cause_matches_either_of": [
 *        "class_extends": "java.io.IOException",  // The cause is a subclass of IOException.
 *        "message_matches_either_of": [
 *          "/File .+ not found./"  // Regular expression.
 *        ],
 *        "direct": true  // Direct cause.
 *      ],
 *      "sorted_into": "EXPECTED_1"  // It expects the enum contains a constant "EXPECTED_1".
 *    }
 *  ]}</pre></blockquote>
 *
 * <p>It is designed to fail as fast as possible. All DSL errors are intended to be reported in constructing a {@link SortEx}
 * instance, not in sorting a {@link java.lang.Throwable} object.
 */
public final class SortEx<E extends Enum<E>> {
    private SortEx(final Map<ExceptionRule, E> rules, final Class<E> enumClass) {
        this.rules = rules;
        this.enumClass = enumClass;
    }

    /**
     * Builds a {@link SortEx} instance from a {@link java.util.Map}-based DSL representation.
     *
     * <p>This method is designed to fail fast. it throws an {@link Exception} immediately if the DSL representation.
     * It throws an {@link Exception} immediately if the {@code emumClass} does not contain the target specified in
     * {@code sorted_into}. Those are to find mistakes in the DSL representation earlier, not when an {@link Exception}
     * is it actually throws, and it attempts to match.
     *
     * @param <E>  the {@link java.lang.Enum} class to sort {@link java.lang.Throwable} objects into
     * @param maps  a {@link java.util.Map}-based DSL representation
     * @param enumClass  the {@link java.lang.Enum} class object to sort {@link java.lang.Throwable} objects into
     * @return the {@link SortEx} instance
     */
    public static <E extends Enum<E>> SortEx<E> from(final List<Map<String, Object>> maps, final Class<E> enumClass) {
        final RuntimeExceptionBuilder exceptionBuilder = new RuntimeExceptionBuilder();

        if (maps == null) {
            exceptionBuilder.add(new NullPointerException("Value of \"maps\" is null."));
        }
        if (enumClass == null) {
            exceptionBuilder.add(new NullPointerException("Value of \"enumClass\" is null"));
        }
        if (exceptionBuilder.isPresent()) {
            throw exceptionBuilder.get();
        }

        final Method valueOfMethod = getValueOfMethod(enumClass);
        final Set<String> targets = checkEnum(enumClass, valueOfMethod);

        final LinkedHashMap<ExceptionRule, E> rules = new LinkedHashMap<>();

        for (final Map<String, Object> map : maps) {
            final ExceptionRule rule;
            try {
                rule = ExceptionRule.from(map);
            } catch (final RuntimeException ex) {
                exceptionBuilder.add(ex);
                continue;
            }

            if (rule == null) {
                exceptionBuilder.add(new NullPointerException("Value of the exception rule is null."));
                continue;
            }

            if (!map.containsKey("sorted_into")) {
                exceptionBuilder.add(new IllegalArgumentException("No sorted_info specified in the exception rule: " + rule));
                continue;
            }

            final Object sortedIntoObject = map.get("sorted_into");
            if (sortedIntoObject == null) {
                exceptionBuilder.add(new NullPointerException("Value of \"sorted_into\" is null."));
                continue;
            }
            if (!(sortedIntoObject instanceof String)) {
                exceptionBuilder.add(new ClassCastException("Value of \"sorted_into\" is not a string."));
                continue;
            }

            final String sortedInto = (String) sortedIntoObject;
            if (!targets.contains(sortedInto)) {
                exceptionBuilder.add(new IllegalArgumentException(
                        "Value of \"sorted_into\", <\"" + sortedInto + "\">, is not expected in Enum class passed to SortEx. "
                                + "Expected values are: " + targets.toString()));
                continue;
            }

            if (rules.containsKey(rule)) {
                exceptionBuilder.add(new IllegalArgumentException("The exception rule is duplicated: " + rule.toString()));
                continue;
            }

            rules.put(rule, invokeValueOf(enumClass, valueOfMethod, sortedInto));
        }

        if (exceptionBuilder.isPresent()) {
            throw exceptionBuilder.get();
        }

        return new SortEx<E>(Collections.unmodifiableMap(rules), enumClass);
    }

    /**
     * Attempts to match the given input {@link java.lang.Throwable} instance against it.
     *
     * @param exception  an {@link java.lang.Throwable} instance to match
     * @return an {@link java.util.Optional} describing the matched {@link java.lang.Enum} constant
     */
    public Optional<E> matches(final Throwable exception) {
        for (final Map.Entry<ExceptionRule, E> entry : this.rules.entrySet()) {
            if (entry.getKey().matches(exception)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Attempts to match the given input {@link java.lang.Throwable} instance against it.
     *
     * @param exception  an {@link java.lang.Throwable} instance to match
     * @param defaultTarget  an {@code enum} constant to return if the {@link java.lang.Throwable} instance did not match
     * @return the matched {@link java.lang.Enum} constant
     */
    public E matches(final Throwable exception, final E defaultTarget) {
        return this.matches(exception).orElse(defaultTarget);
    }

    /**
     * A {@link java.util.Map}-based DSL representation of this {@link SortEx} instance.
     *
     * @return A {@link java.util.Map}-based DSL representation of this {@link SortEx} instance, not null
     */
    public List<Map<String, Object>> toListOfMaps() {
        final ArrayList<Map<String, Object>> maps = new ArrayList<>();
        for (final Map.Entry<ExceptionRule, E> entry : this.rules.entrySet()) {
            final LinkedHashMap<String, Object> map = new LinkedHashMap<>(entry.getKey().toMap());
            map.put("sorted_into", entry.getValue().name());
            maps.add(Collections.unmodifiableMap(map));
        }
        return Collections.unmodifiableList(maps);
    }

    /**
     * Returns a hash code for this {@link SortEx} instance.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(SortEx.class, this.rules, this.enumClass);
    }

    /**
     * Checks if this {@link SortEx} instance is equal to the specified {@link SortEx} instance.
     *
     * @param otherObject  the other {@link SortEx} instance, null returns false
     * @return {@code true} if the other {@link SortEx} instance is equal to this one
     */
    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof SortEx)) {
            return false;
        }
        final SortEx other = (SortEx) otherObject;
        return Objects.equals(this.rules, other.rules) && Objects.equals(this.enumClass, other.enumClass);
    }

    /**
     * A string representation of this {@link SortEx} instance.
     *
     * @return a {@link String} representation of this {@link SortEx} instance, not null
     */
    @Override
    public String toString() {
        return this.toListOfMaps().toString();
    }

    static <E extends Enum<E>> Set<String> checkEnum(final Class<E> enumClass, final Method valueOfMethod) {
        final E[] values = invokeValues(enumClass);
        checkEnumValues(enumClass, valueOfMethod, values);

        final LinkedHashSet<String> targets = new LinkedHashSet<>();
        for (final E value : values) {
            targets.add(value.name());
        }
        return Collections.unmodifiableSet(targets);
    }

    private static <E extends Enum<E>> E[] invokeValues(final Class<E> enumClass) {
        final Method valuesMethod;
        try {
            valuesMethod = enumClass.getMethod("values");
        } catch (final NoSuchMethodException ex) {
            throw new IllegalArgumentException("Enum class passed to SortEx is unexpectedly broken.", ex);
        }

        final Object valuesObject;
        try {
            valuesObject = valuesMethod.invoke(null);
        } catch (final IllegalAccessException ex) {
            throw new IllegalArgumentException("Enum.values() is unexpectedly inaccessible.", ex);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Enum.values() is unexpectedly invalid.", ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalArgumentException("Enum.values() threw an unexpected Exception.", ex.getTargetException());
        } catch (final NullPointerException ex) {
            throw new IllegalArgumentException("Enum.values() unexpectedly looks to be an instance method.", ex);
        } catch (final ExceptionInInitializerError ex) {
            throw new IllegalArgumentException("Enum class passed to SortEx is unexpectedly broken.", ex);
        }

        try {
            return castValues(valuesObject, enumClass);
        } catch (final ClassCastException ex) {
            throw new IllegalArgumentException("Enum.values() returned an invalid object.", ex);
        }
    }

    private static <E extends Enum<E>> E invokeValueOf(final Class<E> enumClass, final Method valueOfMethod, final String string) {
        final Object enumObject;
        try {
            enumObject = valueOfMethod.invoke(null, string);
        } catch (final IllegalAccessException ex) {
            throw new IllegalArgumentException("Enum.valueOf(String) is unexpectedly inaccessible.", ex);
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Enum.valueOf(String) is unexpectedly invalid.", ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalArgumentException("Enum.valueOf(String) threw an unexpected Exception.", ex.getTargetException());
        } catch (final NullPointerException ex) {
            throw new IllegalArgumentException("Enum.valueOf(String) unexpectedly looks to be an instance method.", ex);
        } catch (final ExceptionInInitializerError ex) {
            throw new IllegalArgumentException("Enum class passed to SortEx is unexpectedly broken.", ex);
        }

        try {
            return castValue(enumObject, enumClass);
        } catch (final ClassCastException ex) {
            throw new IllegalArgumentException("Enum.valueOf(String) returned an invalid object.", ex);
        }
    }

    private static <E extends Enum<E>> void checkEnumValues(final Class<E> enumClass, final Method valueOfMethod, final E[] values) {
        for (final E originalValue : values) {
            final String originalName = originalValue.name();

            final E reobtainedValue = invokeValueOf(enumClass, valueOfMethod, originalName);

            if (originalValue != reobtainedValue) {  // Enum values should be comparable with '=='.
                throw new IllegalArgumentException(
                        "Enum class passed to SortEx is invalid: Enum#name() and Enum.valueOf() mismatch.");
            }
            if (!originalName.equals(reobtainedValue.name())) {
                throw new IllegalArgumentException(
                        "Enum class passed to SortEx is invalid: Enum#name() and Enum.valueOf() mismatch.");
            }
        }
    }

    private static <E extends Enum<E>> Method getValueOfMethod(final Class<E> enumClass) {
        try {
            return enumClass.getMethod("valueOf", String.class);
        } catch (final NoSuchMethodException ex) {
            throw new IllegalArgumentException("Enum class passed to SortEx is unexpectedly broken.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E[] castValues(final Object valuesObject, final Class<E> enumClass) {
        return (E[]) valuesObject;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E castValue(final Object valueObject, final Class<E> enumClass) {
        return (E) valueObject;
    }

    private final Map<ExceptionRule, E> rules;
    private final Class<E> enumClass;
}
