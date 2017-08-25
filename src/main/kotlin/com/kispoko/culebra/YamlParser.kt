
package com.kispoko.culebra


import effect.Eff
import effect.Identity



// ---------------------------------------------------------------------------------------------
// YAML PARSER
// ---------------------------------------------------------------------------------------------

typealias YamlParser<A> = Eff<YamlParseError, Identity, A>


// ---------------------------------------------------------------------------------------------
// PARSE ERRORS
// ---------------------------------------------------------------------------------------------

sealed class YamlParseError(open val path : ValuePath?)


data class UnexpectedTypeFound(private val expected : YamlType,
                               private val found : YamlType,
                               override val path : ValuePath?) : YamlParseError(path)
{
    override fun toString(): String = """
            |Unexpected Type Found:
            |    Expected: $expected
            |    Found: $found
            |    Path : $path
            """.trimMargin()
}


data class KeyDoesNotExist(private val key : String,
                           override val path : ValuePath?) : YamlParseError(path)
{
    override fun toString(): String = """
            |Key Does Not Exist:
            |    Key: $key
            |    Path: $path
            """.trimMargin()
}


data class UnexpectedStringValue(private val value : String,
                                 override val path : ValuePath?) : YamlParseError(path)
{
    override fun toString(): String = """
            |Unexpected String Value:
            |    String: $value
            |    Path: $path
            """.trimMargin()
}


data class YamlStringError(private val errors : List<StringParseError>,
                           override val path : ValuePath?) : YamlParseError(path)
{
    override fun toString(): String = """
            |Unexpected String Value:
            |    Errors: $errors
            |    Path: $path
            """.trimMargin()
}
