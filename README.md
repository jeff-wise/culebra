
## :speech_balloon: Deprecated Side-Project

This library was created for use with my
[TaleTable](https://github.com/jeff-wise/taletable-legacy) project
and also for use with the
[Lulo](https://github.com/jeff-wise/lulo-haskell) library as most
configuration was handled with YAML. I wanted a straight-forward
code-level (no reflection, annotations, etc...) library that also
supported sum types and could evolve to work well within the
functional programming paradigm. It's heavily inspired by
[Aeson](https://hackage.haskell.org/package/aeson).

**View the original README below** :arrow_heading_down:

<br/>
<br/>
<br/>


# Culebra

Culebra is a Yaml serialization library for Kotlin built using
the [snakeyaml] library. It provides a flexible interface for
serializing Kotlin values to and from Yaml with the help of the
Applicative / Monadic parsing style. It is heavily influenced by [aeson] Haskell library.

### Why Use Culebra?

 * **Flexible:** Map your data types to and from any YAML file with 
           no constraints on the shape of the data or the names of the fields.
           
 * **Sum Types:** Culebra supports de/serialization of sum types in whichever way 
       you want to represent them.
 
 * **Compositional:** You can easily combine parsers for different datatypes into 
        larger parsers in order to maximize code reuse.
       
 * **No Exceptions:** Errors are encoded in the types so your program's behavior is 
        explicity defined.

### Tutorial Contents

- [Installation](#installation)
  - [Gradle](#gradle)
- [Usage](#usage)
  - [Parsing](#parsing)
    - [Simple Record Type](#simple-record-type)
    - [Wrapped Types](#wrapped-types)
    - [Arrays](#arrays)
    - [Sum Types](#sum-types)
    - [Optional Types](#optional-types)
  - [Encoding](#encoding)
    - [Simple Record Type](#simple-record-type-1)
    - [Sum Types](#sum-types-1)
    - [Optional Types](#optional-types-1)
    - [Nested Types](#nested-types-1)
- [More Examples](#examples)

## Installation

#### Gradle

```
allprojects {
  repositories {
      jcenter()
      maven { url "https://jitpack.io" }
  }
}

dependencies {
    compile 'com.github.jeff-wise:culebra:0.7.0
}
```

## Usage

Good code examples are the most efficient way to learn to use a library. Here are examples for most 
use cases. There are some more (and larger) examples in the tests.

### Parsing

#### Simple Record Type

Let's start with a simple example: a record type containing the three most basic primitive types.

```kotlin
data class Apple(val kind: String, val weightInGrams: Int, val isRipe: Boolean)

val fujiAppleYamlString = """
    kind: Fuji
    weight_in_grams: 90
    is_ripe: yes
    """

fun appleParser(yamlValue: YamlValue): YamlParser<Apple> = when (yamlValue)
{
    is YamlDict -> apply(::Apple,
                         yamlValue.text("kind"),
                         yamlValue.integer("weight_in_grams"),
                         yamlValue.boolean("is_ripe"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}

val myFujiApple = Apple("Fuji", 90, true)

val fujiApple = parseYaml(fujiAppleYamlString, ::appleParser)
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
to build larger ones, we have achieved the ultimate goal in code reuse: *composability*.

#### Wrapped Types

Oftentimes we like to add additional type-safety to our programs by wrapping primitive types 
in new data types. In this case, we are going to create a Grams data type which simply wraps the 
integer type from the previous example. Notice that we change the `Int` to a `Long`. This is 
another benefit to wrapping primitive types: we can change the underlying representation without 
breaking dependencies. Additionally, it makes our program easier to read and prevents us from 
mixing up function parameters by accident.

Of course, Yaml does not understand our custom data types so we need to map them to the 
primitive types we read from Yaml. This is really simple using the monadic interface. 

```kotlin
data class Grams(val value: Long)

data class Apple(val kind: String, val weightInGrams: Grams, val isRipe: Boolean)


val fujiAppleYamlString = """
    kind: Fuji
    weight_in_grams: 90
    is_ripe: yes
    """

fun gramsParser(yamlValue: YamlValue): YamlParser<Grams> = when (yamlValue)
{
    is YamlInteger -> effValue(Grams(yamlValue.number))
    else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                   yamlType(yamlValue),
                                                   yamlValue.path))
}

fun appleParser(yamlValue: YamlValue): YamlParser<Apple> = when (yamlValue)
{
    is YamlDict -> apply(::Apple,
                         yamlValue.text("kind"),
                         // We apply the Grams parser to the YamlValue at "weight_in_grams"
                         yamlValue.at("weight_in_grams").apply(::gramsParser),
                         yamlValue.boolean("is_ripe"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}

val myFujiApple = Apple("Fuji", Grams(90), true)

val fujiApple = parseYaml(fujiAppleYamlString, ::appleParser)
when (fujiApple) {
    is Val -> fujiApple.value shouldBe myFujiApple
    is Err -> fujiApple should beOfType<Eff<YamlParseError,Identity,Apple>>()
}
```

The only big difference in this example from before is the use of `apply` to create our Grams 
data type parser. `yamlValue.at("weight_in_grams")` returns a parser that attempts to parse a 
generic `YamlValue` at the `weight_in_grams` key. `apply` composes that parser with the Grams 
parser, creating a miniature parsing pipeline. If the first stage fails because the key does not 
exist, then our grams parser will/can never be applied. But we don't have to handle those error 
cases explicity. They are encapsulated in the definition of `apply`.

Generally `apply` is just fancy function application. It calls a function on a result, but handles 
some other effects on the side, such as possible failure. In the context of parsing and this 
library, we can think of it as a way to compose / connect yaml parsers.

#### Arrays

To parse arrays of nested data types, we need just one new function: `mapApply`. A combination of 
`map` and `apply`, it allows us to create a parser for each item in a list and combine those parsers 
into one big parser that parsers the entire list, failing if any item in the list cannot be parsed. 

```kotlin
data class Song(val name: String, val length: Int)

data class Album(val name: String, val songs: List<Song>)

fun songParser(yamlValue: YamlValue): YamlParser<Song> = when (yamlValue)
{
    is YamlDict -> apply(::Song, yamlValue.text("name"), yamlValue.integer("length"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}

fun albumParser(yamlValue: YamlValue): YamlParser<Album> = when (yamlValue)
{
    is YamlDict -> apply(::Album,
                         yamlValue.text("name"),
                         yamlValue.array("songs")
                                  .apply { it.mapApply { songParser(it) } })
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}

val javaTalkAlbumString = """
    name: Java Talk
    songs:
    - name: Kotlin
      length: 223
    - name: Java
      length: 407
    - name: Clojure
      length: 188
    - name: Scala
      length: 250
    """

val javaTalkAlbum = Album("Java Talk",
                          listOf(Song("Kotlin", 223),
                                 Song("Java", 407),
                                 Song("Clojure", 188),
                                 Song("Scala", 250)))

val album = parseYaml(javaTalkAlbumString, ::albumParser)
when (album) {
    is Val -> album.value shouldBe javaTalkAlbum
    is Err -> album should beOfType<Eff<YamlParseError,Identity,Album>>()
}
```

To parse the songs we call `yamlValue.array("songs")` to get the `YamlParser<YamlArray>` at the 
*songs* key. Like in the previous example, we cannot call a function directly on this value, 
because it's not a value, but a parser that will try to get our value. We must use `apply` like 
before, and then we have access to the `YamlArray` as if it were not inside the parser object. 

Now we have a list of yaml values, but we need to parse each one. Each value could fail to be 
parsed, in which case we want to fail the list parse as well. But each value is returned 
inside a parser, so we have to check each parser for failure or success and collect the values. 
The code for that procedure would look like this:

```kotlin

fun A parseList(yamlValues : List<YamlValue>, 
                parser : (YamlValue) -> YamlParser<A>) : YamlParser<List<A>>
{
    val values : MutableList<A> = mutableListOf()
    
    yamlValues.forEach { yamlValue ->
        val valueParser = parser(yamlValue)
        when (valueParser) {
            is Val -> values.add(valueParser.value)
            is Err -> return effError(CouldNotParse())
        }
    }

    return effValue(values)
}
```

That's a lot of code. Without this library and without Applicative Functors, parsing like this 
would be impractical. But `mapApply` will do what the `parseList` function does in one line of 
code, so we don't have to worry about the *how* of parsing our Yaml, only the *what*.


#### Sum Types

There are a few ways to implement sum types in Yaml. Any of them can be done with Culebra. Here we 
show one method, where we specify the case of the sum type in a Yaml object and put the actual 
value inside another key in the same object. This is the most extensible / understandable method for 
encoding sum types in Yaml. 

```kotlin
sealed class Home(open val numberOfRooms: Int)

data class Townhouse(override val numberOfRooms: Int,
                     val mortgagePayment: Int) : Home(numberOfRooms)

data class Castle(override val numberOfRooms: Int,
                  val hasPortcullis: Boolean) : Home(numberOfRooms)


fun townhouseParser(yamlValue: YamlValue): YamlParser<Home> = when (yamlValue)
{
    is YamlDict -> apply(::Townhouse,
                         yamlValue.integer("rooms"),
                         yamlValue.integer("mortgage_payment"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}


fun castleParser(yamlValue: YamlValue): YamlParser<Home> = when (yamlValue)
{
    is YamlDict -> apply(::Castle,
                         yamlValue.integer("rooms"),
                         yamlValue.boolean("has_portcullis"))
    else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}


fun homeParser(yamlValue: YamlValue) : YamlParser<Home> = when (yamlValue)
{
    is YamlDict ->
    {
        yamlValue.text("home_type") apply {
            when (it) {
                "townhouse" -> yamlValue.at("home").apply(::townhouseParser)
                "castle"    -> yamlValue.at("home").apply(::castleParser)
                else        -> effError<YamlParseError,Home>(
                                   UnexpectedStringValue(it, yamlValue.path))
            }
        }
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

val myCastleYamlString = """
    home_type: castle
    home:
      rooms: 27
      has_portcullis: true
    """

val myCastle = Castle(27, true) 
                         
val home = parseYaml(myCastleYamlString, ::homeParser)
when (home) {
    is Val -> home.value shouldBe myCastle
    is Err -> home should beOfType<Eff<YamlParseError,Identity,Home>>()
}
```

There's not much new to see here. One thing to note is that Kotlin's type inference can be pretty 
frustrating to deal with at times. We made two adjustments to make this code check. First, the 
`parseTownhouse` and `parseCastle` code have a return type of `YamlParser<Home>` instead of 
`YamlParser<Townhouse>` and `YamlParser<Castle>` respectively. This works here, but can cause 
problems in other code. In that case you will have to do an unchecked cast at the calling site. 
Second, we have to add a type annotation to the error case in `parseHome`. Overall, the code 
remains concise and readable. 

#### Optional Types

Sometimes we have data types with optional fields. Kotlin has good support of `null` values, but 
the `Maybe` type is a more powerful and flexible alternative. 

```kotlin
data class Dinner(val appetizer: Maybe<String>,
                  val mainCourse: String,
                  val dessert: Maybe<String>)

fun parseDinner(yamlValue : YamlValue) : YamlParser<Dinner> = when (yamlValue)
{
    is YamlDict -> apply(::Dinner,
                         yamlValue.maybeText("appetizer"),
                         yamlValue.text("main_course"),
                         yamlValue.maybeText("dessert"))
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

val myDinnerString = """
    main_course: Steak
    dessert: Cake
    """

val myDinner = Dinner(Nothing(), "Steak", Just("Cake"))

val dinner = parseYaml(myDinnerString, ::parseDinner)
when (dinner) {
    is Val -> dinner.value shouldBe myDinner
    is Err -> dinner should beOfType<Eff<YamlParseError,Identity,Dinner>>()
}
```

### Encoding

Encoding our data types into Yaml values is much easier. We already have the data so we just need 
to translate it from our Kotlin data types into `YamlValue`s. 

#### Simple Record Type

```kotlin
data class Grams(val value : Long) : ToYaml
{
    override fun toYaml(): YamlValue = YamlInteger(value)
}

data class Apple(val kind: String, val weightInGrams: Grams, val isRipe: Boolean) : ToYaml
{
    override fun toYaml(): YamlValue =
        YamlDict(
            hashMapOf("kind"            to YamlText(this.kind),
                      "weight_in_grams" to weightInGrams.toYaml(),
                      "is_ripe"         to YamlBool(this.isRipe)))
}

val fujiApple = Apple("Fuji", Grams(90), true)

val fujiAppleYamlString = encodeYaml(fujiApple)

val apple = parseYaml(fujiAppleYamlString, ::parseApple)
when (apple) {
    is Val -> apple.value shouldBe fujiApple
    is Err -> apple should beOfType<Eff<YamlParseError,Identity,Apple>>()
}
```

#### Arrays

```kotlin
data class Song(val name: String, val length: Int) : ToYaml
{
    override fun toYaml(): YamlValue =
        YamlDict(
            hashMapOf("name"   to YamlText(this.name),
                      "length" to YamlInteger(this.length)))
}

data class Album(val name: String, val songs: List<Song>) : ToYaml
{
    override fun toYaml(): YamlValue =
        YamlDict(
            hashMapOf("name"  to YamlText(this.name),
                      "songs" to YamlArray(this.songs.map { it.toYaml() })))
}

val javaTalkAlbum = Album("Java Talk",
                          listOf(Song("Kotlin", 223),
                                 Song("Java", 407),
                                 Song("Clojure", 188),
                                 Song("Scala", 250)))

val javaTalkAlbumYamlString = encodeYaml(javaTalkAlbum)

val album = parseYaml(javaTalkAlbumYamlString, ::parseAlbum)
when (album) {
    is Val -> album.value shouldBe javaTalkAlbum
    is Err -> album should beOfType<Eff<YamlParseError,Identity,Album>>()
}
```

#### Sum Types

```kotlin
sealed class Home(open val numberOfRooms: Int) : ToYaml
{
    
    override fun toYaml() : YamlValue = when (this)
    {
        is Townhouse -> {
            YamlDict(
                hashMapOf("home_type" to YamlText("townhouse"),
                          "home"      to this.toYaml()))
        }
        is Castle -> {
            YamlDict(
                hashMapOf("home_type" to YamlText("castle"),
                          "home"      to this.toYaml()))
        }
    }
}

data class Townhouse(override val numberOfRooms: Int,
                     val mortgagePayment: Int) : Home(numberOfRooms), ToYaml
{

    override fun toYaml(): YamlValue =
        YamlDict(
            hashMapOf("rooms"   to YamlInteger(this.numberOfRooms),
                      "payment" to YamlInteger(this.mortgagePayment)))
}


data class Castle(override val numberOfRooms: Int,
                  val hasPortcullis: Boolean) : Home(numberOfRooms), ToYaml
{
    override fun toYaml(): YamlValue =
        YamlDict(
            hashMapOf("rooms"          to YamlInteger(this.numberOfRooms),
                      "has_portcullis" to YamlBool(this.hasPortcullis)))
}

val myCastle = Castle(27, true)

val myCastleYamlString = encodeYaml(myCastle)

val castle = parseYaml(myCastleYamlString, ::parseCastle)
when (castle) {
    is Val -> castle.value shouldBe myCastle
    is Err -> castle should beOfType<Eff<YamlParseError,Identity,Home>>()
}
```

#### Optional Types

```kotlin
data class Dinner(val appetizer: Maybe<String>,
                  val mainCourse: String,
                  val dessert: Maybe<String>) : ToYaml
{
    override fun toYaml() : YamlValue = YamlDict()
        .maybeUnion(this.appetizer apply {
            Just(YamlDict(hashMapOf("appetizer" to YamlText(it))))
        })
        .union(YamlDict(hashMapOf("main_course" to YamlText(this.mainCourse))))
        .maybeUnion(this.dessert apply {
            Just(YamlDict(hashMapOf("dessert" to YamlText(it))))
        })
}

fun parseDinner(yamlValue : YamlValue) : YamlParser<Dinner> = when (yamlValue)
{
    is YamlDict -> {
        effApply(::Dinner,
                 yamlValue.maybeText("appetizer"),
                 yamlValue.text("main_course"),
                 yamlValue.maybeText("dessert"))
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}


val myDinner = Dinner(Nothing(), "Steak", Just("Cake"))

val myDinnerYamlString = encodeYaml(myDinner)

val dinner = parseYaml(myDinnerYamlString, ::parseDinner, false)
when (dinner) {
    is Val -> dinner.value shouldBe myDinner
    is Err -> dinner should beOfType<Eff<YamlParseError,Identity,Dinner>>()
}
```



[snakeyaml]: https://bitbucket.org/asomov/snakeyaml
[aeson]: http://hackage.haskell.org/package/aeson
