SortEx
=======

A Java Exception sorter -- to categorize Java Exceptions into `Enum` with a `Map`-based DSL (such as YAML or JSON).

(Note that "sort" does not mean reordering here, but categorizing. It is inspired from [coin sorters](https://en.wikipedia.org/wiki/Currency-counting_machine#Coin_sorter).)

Quick usage
------------

```
enum Target {
    ONE,
    TWO,
    THREE,
    DEFAULT,
    ;
}
```

```
final List<Map<String, Object>> dslMaps = load(/* Map-based representation of DSL converted from something, such as YAML */);

final SortEx<Target> sortex = SortEx.from(dslMaps, Target.class);

final Target sorted = sortex.matches(exceptionToSort, Target.DEFAULT);

switch (sorted) {
    case ONE:
        ...
        break;
    case TWO:
        ...
        break;
    case THREE:
        ...
        break;
    default:
        ...
        break;
}
```

Quick overview of DSL
----------------------

The `Map`-based DSL for sorting would look like below, for example. This example representation is with YAML.

```
- class_equals: java.io.UncheckedIOException  # Exact match.
  message_matches_either_of:
    - "Unexpected"  # Exact match.
  cause_matches_either_of:
    - class_extends: java.io.IOException  # The cause is a subclass of IOException.
      message_matches_either_of:
        - "/File .+ not found./"  # Regular expression.
      direct: true  // Direct cause.
  sorted_into: "EXPECTED_1"  // It expects the enum contains a constant "EXPECTED_1".
```
