
# Culebra

Culebra is a Yaml serialization library for Kotlin built using
the [snakeyaml] library. It provides a flexible interface for
serializing Kotlin values to and from Yaml with the help of the
Applicative / Monadic parsing style. This library is heavily
influenced by [aeson] and pure functional programming methods. 

With Culebra you are not limited to using DTOs. You can design both
the structure of your Yaml files and your Kotlin data types however
you like and serialize directly between the two. The tradeoff is that
you have to write the functions yourself. But it's not that hard, and
if they typecheck, they should behave predictably.

**Table of Contents**

- [Usage Examples](#usage-examples)
    - [Parsing](#parsing)
      - [Basic Types](#basic-types)
      - [Sum Types](#sum-types)
      - [Optional Types](#optional-types)
      - [Nested Types](#nested-types)
    - [Encoding](#encoding)
      - [Basic Types](#basic-types-1)
      - [Sum Types](#sum-types-1)
      - [Optional Types](#optional-types-1)
      - [Nested Types](#nested-types-1)
- [More Examples](#examples)


## Usage Examples

### Parsing

#### Basic Types

Let's start with a simple example: a record type containing the three most basic primitive types.

```kotlin
data class Apple(val kind: String, val weightInGrams: Int, val isRipe: Boolean)

val fujiAppleYamlString = """
    kind: Fuji
    weight_in_grams: 90
    is_ripe: yes
    """

fun parseApple(yamlValue: YamlValue): YamlParser<Apple> = when (yamlValue)
{
    is YamlDict -> effApply(::Apple,
                            yamlValue.text("kind"),
                            yamlValue.integer("weight_in_grams"),
                            yamlValue.boolean("is_ripe"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}

val myFujiApple = Apple("Fuji", 90, true)

val fujiApple = parseYaml(fujiAppleYamlString, ::parseApple, false)
when (fujiApple) {
    is Val -> fujiApple.value shouldBe myFujiApple
}
```

Notice that in the `parseApple` function we decide how the yaml values are mapped into our Kotlin
data type. In this case we see that it doesn't matter if the yaml file uses underscores and the
Kotlin data types use camelcase.

Even if you've never used parser combinators before, the code should be fairly intuitive to read.
Let's briefly go over the `parseApple` function. We can tell from the type signature that this
function associates some `YamlValue` with a `YamlParser<Apple>`. The latter type represents a
parser which takes a `YamlValue` and attempts to create an `Apple` from it. Of course, that parser
may fail in many ways. These failure states are largely encapsulated inside the `YamlParser` type,
so we can dictate what we want to parse, and let the how be handled behind the scenes. Whereas in
OOP one tends to encapsulate using objects, functional programming gains great power from
encapsulation of effects and state inside chains of function application. That is called, more or
less, monadic programming. The details of that should be unnecessary to effectively use this
library.

The function starts by branching on the type of the `YamlValue`. If its not an object / dictionary
yaml value, then it cannot possibly we an `Apple`, so we return an error, explaining exactly what
happened. Note that here, as we will see later, we can be as flexible as we like. If we would like
to interpret a yaml string as an `Apple`, we may do just that. That's why it's up to the programmer
to return the error in this case.

Finally, inside the `YamlDict` case, we apply the `Apple` constructor over the apple's values.
The function application is special though, as are the values. Each value such as
`yamlValue.integer("weight_in_grams")` is also a `YamlParser`. That value represents a yaml parser
that tries to read an integer at the key `weight_in_grams`. The `effApply` allows use to apply an
ordinary function (our Apple constructor) over those parsers, as if they were also ordinary values.
It takes care of checking whether each parser has succeeded or not. If every parser is successful,
then we get an Apple. If any parser fails, we get a parsing error.

With this interface we can build parsers for all our datatypes however we would like. We don't have
to worry about checking every value to see if is null. By creating small parsers and using them
to build larger ones, we have achieved the ultimate goal in code reuse: **composability**.

parse with wrapped basic type

parse with array

#### Sum Types

#### Optional Types

parse basic with maybes

#### Nested Types

simple combination of previous examples

### Encoding

#### Basic Types
#### Sum Types
#### Optional Types
#### Nested Types


[snakeyaml]: https://bitbucket.org/asomov/snakeyaml
[aeson]: http://hackage.haskell.org/package/aeson
