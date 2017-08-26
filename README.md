
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

- [Usage Examplese](#usage-examples)
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
                            yamlValue.integer("bonus"),
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

parse dictionary with integer, bool, and string

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
