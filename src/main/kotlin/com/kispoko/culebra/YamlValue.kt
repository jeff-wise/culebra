
package com.kispoko.culebra


import effect.*
import effect.Nothing
import java.util.HashMap



// ---------------------------------------------------------------------------------------------
// YAML VALUE
// ---------------------------------------------------------------------------------------------

sealed class YamlValue(open val path : ValuePath?)
{

    //@Suppress("UNCHECKED_CAST")
    fun toPojo() : Any = when (this)
    {
        is YamlDict ->
        {
            val hm = hashMapOf<String,Any>()
            this.map.forEach { key, yamlValue ->
                hm.put(key, yamlValue.toPojo())
            }
            hm
        }
        is YamlArray -> {
            val arrayList = arrayListOf<Any>()
            this.list.forEach { arrayList.add(it.toPojo()) }
            arrayList
        }
        is YamlText -> this.text
        is YamlInteger -> this.number
        is YamlFloat -> this.number
        is YamlBool -> this.bool
        is YamlNull -> "null"
    }

}


// Yaml Value > Dictionary
// ---------------------------------------------------------------------------------------------

data class YamlDict(val map : HashMap<String,YamlValue>,
                    override val path : ValuePath?)
                     : YamlValue(path)
{

    // -----------------------------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------

    constructor(map : HashMap<String,YamlValue>) : this(map, null)


    // -----------------------------------------------------------------------------------------
    // API
    // -----------------------------------------------------------------------------------------

    // Parsing Combinators
    // -----------------------------------------------------------------------------------------

    /**
     * Get a parser for the value at the given key. The parser fails if the key does not exist.
     *
     * @param key The key in the yaml dictionary/object that contains the value to be parsed.
     */
    fun at(key : String) : YamlParser<YamlValue>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        return effValue(map.getValue(key))
    }


    /**
     * Try to get a parser for the value at the given key. If the key is not present, the
     * parse does not fail, instead it returns [Nothing].
     *
     * @param key The key in the yaml dictionary/object that contains the value to be parsed.
     */
    fun maybeAt(key : String) : YamlParser<Maybe<YamlValue>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        return effValue(Just(map.getValue(key)))
    }


    /**
     * Get a parser for an array at the given key. The parser fails if the key does not exist, or
     * if the value is not a [YamlArray].
     *
     * @param key The key in the yaml dictionary/object that contains the array to be parsed.
     */
    fun array(key : String) : YamlParser<YamlArray>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
        // Found Array, return it
            is YamlArray -> effValue(yamlValueAtKey)
        // Is other type, return detailed error
            else         -> effError(UnexpectedTypeFound(YamlType.ARRAY,
                                                         yamlType(yamlValueAtKey),
                                                         path))
        }
    }


    /**
     * Try to get a parser for an array at the given key. The parser does not fail if the key
     * does not exist, instead it returns [Nothing]. If the value cannot be parsed as a [YamlArray]
     * the parser will fail.
     *
     * @param key The key in the yaml dictionary/object that contains the array to be parsed.
     */
    fun maybeArray(key : String) : YamlParser<Maybe<YamlArray>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlArray -> effValue(Just(yamlValueAtKey))
            // Is other type, return detailed error
            else         -> effError(UnexpectedTypeFound(YamlType.ARRAY,
                                                         yamlType(yamlValueAtKey),
                                                         path))
        }
    }


    /**
     * Get a parser for an array at the given key. The parser fails if the key does not exist, or
     * if the value is not a [YamlArray].
     *
     * @param key The key in the yaml dictionary/object that contains the array to be parsed.
     */
    fun valueList(key : String) : YamlParser<List<YamlValue>> =
        this.array(key) ap { effValue<YamlParseError,List<YamlValue>>(it.list) }


    /**
     * Get a parser for an string set at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as a set of strings.
     *
     * @param key The key in the yaml dictionary/object that contains the string set to be parsed.
     */
    fun stringSet(key : String) : YamlParser<Set<String>>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlArray -> yamlValueAtKey.asStringSet()
            // Is other type, return detailed error
            else         -> effError(UnexpectedTypeFound(YamlType.ARRAY,
                                                         yamlType(yamlValueAtKey),
                                                         path))
        }
    }


    /**
     * Get a parser for an [Int] at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as an integer.
     *
     * @param key The key in the yaml dictionary/object that contains the integer to be parsed.
     */
    fun integer(key : String) : YamlParser<Int>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
        // Found Array, return it
            is YamlInteger -> effValue(yamlValueAtKey.number.toInt())
        // Is other type, return detailed error
            else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                           yamlType(yamlValueAtKey),
                                                           path))
        }
    }


    /**
     * Try to get a parser for an [Int] at the given key. The parser does not fail if the key is
     * not present, instead it returns Nothing. The parser will fail if the value cannot be parsed
     * as an integer.
     *
     * @param key The key in the yaml dictionary/object that contains the integer to be parsed.
     */
    fun maybeInteger(key : String) : YamlParser<Maybe<Int>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
        // Found Array, return it
            is YamlInteger -> effValue(Just(yamlValueAtKey.number.toInt()))
        // Is other type, return detailed error
            else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                           yamlType(yamlValueAtKey),
                                                           path))
        }
    }


    /**
     * Get a parser for an [Long] at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as an long.
     *
     * @param key The key in the yaml dictionary/object that contains the long to be parsed.
     */
    fun long(key : String) : YamlParser<Long>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlInteger -> effValue(yamlValueAtKey.number)
            // Is other type, return detailed error
            else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                           yamlType(yamlValueAtKey),
                                                           path))
        }
    }


    /**
     * Try to get a parser for an [Long] at the given key. The parser does not fail if the key is
     * not present, instead it returns [Nothing]. The parser will fail if the value cannot be
     * parsed as a long integer.
     *
     * @param key The key in the yaml dictionary/object that may contain the long to be parsed.
     */
    fun maybeLong(key : String) : YamlParser<Maybe<Long>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlInteger -> effValue(Just(yamlValueAtKey.number))
            // Is other type, return detailed error
            else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                           yamlType(yamlValueAtKey),
                                                           path))
        }
    }


    /**
     * Get a parser for an [Float] at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as an long.
     *
     * @param key The key in the yaml dictionary/object that contains the long to be parsed.
     */
    fun double(key : String) : YamlParser<Double>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Float, return it
            is YamlFloat -> effValue(yamlValueAtKey.number)
            // Is other type, return detailed error
            else         -> effError(UnexpectedTypeFound(YamlType.FLOAT,
                                                         yamlType(yamlValueAtKey),
                                                         path))
        }
    }


    /**
     * Try to get a parser for an [Double] at the given key. The parser does not fail if the key is
     * not present, instead it returns [Nothing]. The parser will fail if the value cannot be
     * parsed as a double.
     *
     * @param key The key in the yaml dictionary/object that may contain the double to be parsed.
     */
    fun maybeDouble(key : String) : YamlParser<Maybe<Double>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Float, return it
            is YamlFloat -> effValue(Just(yamlValueAtKey.number))
            // Is other type, return detailed error
            else         -> effError(UnexpectedTypeFound(YamlType.FLOAT,
                                                         yamlType(yamlValueAtKey),
                                                         path))
        }
    }


    /**
     * Get a parser for an [Long] at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as an long.
     *
     * @param key The key in the yaml dictionary/object that contains the long to be parsed.
     */
    fun text(key : String) : YamlParser<String>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Text, return it
            is YamlText -> effValue(yamlValueAtKey.text.trim())
            // Is other type, return detailed error
            else        -> effError(UnexpectedTypeFound(YamlType.TEXT,
                                                        yamlType(yamlValueAtKey),
                                                        path))
        }
    }


    /**
     * Try to get a parser for an [String] at the given key. The parser does not fail if the key is
     * not present, instead it returns [Nothing]. The parser will fail if the value cannot be
     * parsed as a string.
     *
     * @param key The key in the yaml dictionary/object that may contain the string to be parsed.
     */
    fun maybeText(key : String) : YamlParser<Maybe<String>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Text, return it
            is YamlText -> effValue(Just(yamlValueAtKey.text.trim()))
            // Is other type, return detailed error
            else        -> effError(UnexpectedTypeFound(YamlType.TEXT,
                                                        yamlType(yamlValueAtKey),
                                                        path))
        }
    }


    /**
     * Get a parser for an [Boolean] at the given key. The parser fails if the key does not exist,
     * or if the value cannot be parsed as an long.
     *
     * @param key The key in the yaml dictionary/object that contains the boolean to be parsed.
     */
    fun boolean(key : String) : YamlParser<Boolean>
    {
        if (!map.containsKey(key))
            return effError(KeyDoesNotExist(key, path))

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlBool -> effValue(yamlValueAtKey.bool)
            // Is other type, return detailed error
            else        -> effError(UnexpectedTypeFound(YamlType.BOOL,
                                                        yamlType(yamlValueAtKey),
                                                        path))
        }
    }


    /**
     * Try to get a parser for an [Boolean] at the given key. The parser does not fail if the key is
     * not present, instead it returns [Nothing]. The parser will fail if the value cannot be
     * parsed as a boolean.
     *
     * @param key The key in the yaml dictionary/object that contains the boolean to be parsed.
     */
    fun maybeBoolean(key : String) : YamlParser<Maybe<Boolean>>
    {
        if (!map.containsKey(key))
            return effValue(Nothing())

        val yamlValueAtKey = map[key]
        return when (yamlValueAtKey)
        {
            // Found Array, return it
            is YamlBool -> effValue(Just(yamlValueAtKey.bool))
            // Is other type, return detailed error
            else        -> effError(UnexpectedTypeFound(YamlType.BOOL,
                                                        yamlType(yamlValueAtKey),
                                                        path))
        }
    }


    // Other
    // -----------------------------------------------------------------------------------------

    /**
     * Returns the union of this Yaml Dictionary and another.
     *
     * @param otherYamlDict The other yaml dictionary to merge with this one.
     *
     * @return The Yaml Dictionary that has the key-value pairs from both dictionaries.
     */
    fun union(otherYamlDict : YamlDict) : YamlDict
    {
        // Is this really the easiest way to do this?
        val bothEntries = this.map.entries.plus(otherYamlDict.map.entries)
        //bothEntries.addAll(otherYamlDict.map.entries)
        val newMap = hashMapOf<String,YamlValue>()

        bothEntries.forEach { newMap.put(it.key, it.value) }

        return YamlDict(newMap)
    }


    /**
     * Returns the union of this [YamlDict] and another. If the other dictionary is not present,
     * then this dictionary is returned with no modifications.
     *
     * @param otherYamlDict The other yaml dictionary to merge with this one. It may not exist.
     */
    fun maybeUnion(maybeOtherYamlDict : Maybe<YamlDict>) : YamlDict =
        when (maybeOtherYamlDict) {
            is Just    -> this.union(maybeOtherYamlDict.value)
            is Nothing -> this
        }
}


// Yaml Value > Array
// ---------------------------------------------------------------------------------------------

@Suppress("UNCHECKED_CAST")
data class YamlArray(val list : List<YamlValue>,
                     override val path : ValuePath?)
                      : YamlValue(path)
{

    // -----------------------------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------------------------

    constructor(valueList : List<YamlValue>) : this(valueList, null)


    // -----------------------------------------------------------------------------------------
    // API
    // -----------------------------------------------------------------------------------------

    fun asStringSet() : YamlParser<Set<String>>
    {
        val stringSet = mutableSetOf<String>()

        this.list.forEachIndexed { index, yamlValue ->

            when (yamlValue) {
                is YamlText -> stringSet.add(yamlValue.text)
                else        -> return effError(UnexpectedTypeFound(YamlType.TEXT,
                                                                   yamlType(yamlValue),
                                                                   path))
            }
        }

        return effValue(stringSet)
    }


}


// Yaml Value > Integer
// ---------------------------------------------------------------------------------------------

data class YamlInteger(val number : Long,
                       override val path : ValuePath?) : YamlValue(path)
{
    constructor(value : Long) : this(value, null)

    constructor(value : Int) : this(value.toLong(), null)
}


// Yaml Value > Float
// ---------------------------------------------------------------------------------------------

data class YamlFloat(val number : Double,
                     override val path : ValuePath?) : YamlValue(path)
{
    constructor(value : Double) : this(value, null)
}


// Yaml Value > Text
// ---------------------------------------------------------------------------------------------

data class YamlText(val text : String,
                    override val path : ValuePath?) : YamlValue(path)
{
    constructor(value : String) : this(value, null)
}


// Yaml Value > Bool
// ---------------------------------------------------------------------------------------------

data class YamlBool(val bool : Boolean,
                    override val path : ValuePath?) : YamlValue(path)
{
    constructor(value : Boolean) : this(value, null)
}


// Yaml Value > Null
// ---------------------------------------------------------------------------------------------

class YamlNull(override val path : ValuePath?) : YamlValue(path)
{
    constructor() : this(null)
}


// ---------------------------------------------------------------------------------------------
// YAML TYPE
// ---------------------------------------------------------------------------------------------

enum class YamlType
{
    DICT,
    ARRAY,
    INTEGER,
    FLOAT,
    TEXT,
    BOOL,
    OTHER,
    NULL
}


fun yamlType(yamlValue : YamlValue?) : YamlType
{
    if (yamlValue == null)
        return YamlType.NULL

    when (yamlValue)
    {
        is YamlDict    -> return YamlType.DICT
        is YamlArray   -> return YamlType.ARRAY
        is YamlInteger -> return YamlType.INTEGER
        is YamlFloat   -> return YamlType.FLOAT
        is YamlText    -> return YamlType.TEXT
        is YamlBool    -> return YamlType.BOOL
        is YamlNull    -> return YamlType.NULL
    }
}


// ---------------------------------------------------------------------------------------------
// VALUE PATH
// ---------------------------------------------------------------------------------------------

data class ValuePath(val nodes : List<ValueNode>)
{

    constructor() : this(listOf())

    infix fun withNode(node : ValueNode) : ValuePath = ValuePath(nodes.plus(node))


    override fun toString(): String
    {
        var pathString = ""

        for (node in this.nodes) {
            pathString += node.toString()
        }

        return pathString;
    }

}



sealed class ValueNode


data class ValueKeyNode(val key : String) : ValueNode()
{
    override fun toString(): String = "." + key
}

data class ValueIndexNode(val index : Int) : ValueNode()
{
    override fun toString(): String
    {
        return "[$index]"
    }
}

