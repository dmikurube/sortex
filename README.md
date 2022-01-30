SortEx
=======

A Java Exception sorter -- to categorize Java Exceptions into `Enum` with a `Map`-based DSL (such as YAML or JSON).

(Note that "sort" does not mean reordering here, but categorizing. It is inspired from [coin sorters](https://en.wikipedia.org/wiki/Currency-counting_machine#Coin_sorter).)

Quick overview of DSL
----------------------

The `Map`-based DSL for sorting would look like below, for example. The representation is with YAML.

```
- class_equals: java.io.UncheckedIOException
  message_matches_either_of:
    - "foo"
  cause_matches_either_of:
    - class_equals: java.io.IOException
      direct: true
  sorted_into: "ONE"
- class_equals: java.io.UncheckedIOException
  message_matches_either_of:
    - "bar"
  cause_matches_either_of:
    - class_equals: java.io.IOException
      direct: true
  sorted_into: "TWO"
```

Quick overview to sort an Exception
------------------------------------

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
final List<Map<String, Object>> dslMaps = /* Map-based representation of DSL converted from something (ex. YAML) */;

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
