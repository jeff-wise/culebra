
package com.kispoko.culebra


import java.io.InputStream



// ---------------------------------------------------------------------------------------------
// YAML STRING
// ---------------------------------------------------------------------------------------------

object YamlString
{

    /**
     * Parse an input stream that represents a raw YAML string into a [YamlValue].
     *
     * @param inputStream The inputstream of the raw yaml string.
     * @param trackPath If true, tracks the path of each node in the YamlValue. Useful for
     *                  debugging.
     */
    fun parse(inputStream : InputStream, trackPath : Boolean = true) : YamlParse<YamlValue>
    {
        val yaml = org.yaml.snakeyaml.Yaml()
        val yamlObject = yaml.load(inputStream)

        return if (trackPath)
            parseYaml(yamlObject, ValuePath())
        else
            parseYaml(yamlObject, null)
    }


    /**
     * Parse a raw YAML string into a [YamlValue].
     *
     * @param inputStream The raw YAML string.
     * @param trackPath If true, tracks the path of each node in the YamlValue. Useful for
     *                  debugging.
     */
    fun parse(yamlString : String, trackPath : Boolean = true) : YamlParse<YamlValue>
    {
        val yaml = org.yaml.snakeyaml.Yaml()
        val yamlObject = yaml.load(yamlString)

        return if (trackPath)
            parseYaml(yamlObject, ValuePath())
        else
            parseYaml(yamlObject, null)
    }


    @Suppress("UNCHECKED_CAST")
    private fun parseYaml(yamlObject : Any?,
                          path : ValuePath?) : YamlParse<YamlValue>
    {
        if (yamlObject == null)
            return YamlParseValue(YamlNull(path))

        if (yamlObject is Map<*,*>)
        {
            val yamlObjectMap : Map<String,Any>? = yamlObject as? Map<String,Any>

            if (yamlObjectMap == null)
            {
                return YamlParseErrors(listOf(TypeConversionError("map")))
            }
            else
            {
                val errors = mutableListOf<StringParseError>()
                val yamlDictMap = hashMapOf<String,YamlValue>()

                for ((key, value) in yamlObjectMap)
                {
                    val newPath = path?.withNode(ValueKeyNode(key))
                    val eYamlValue = parseYaml(value, newPath)
                    when (eYamlValue) {
                        is YamlParseErrors -> errors.addAll(eYamlValue.errors)
                        is YamlParseValue -> yamlDictMap.put(key, eYamlValue.value)
                    }
                }

                return if (errors.size > 0)
                    YamlParseErrors(errors)
                else
                    YamlParseValue(YamlDict(yamlDictMap, path))
            }
        }
        else if (yamlObject is ArrayList<*>)
        {
            val yamlObjectList : List<Any> = yamlObject

            val errors = mutableListOf<StringParseError>()
            val yamlValueList = mutableListOf<YamlValue>()

            yamlObjectList.forEachIndexed { index, yamlObject ->
                val newPath = path?.withNode(ValueIndexNode(index))
                val eYamlValue = parseYaml(yamlObject, newPath)
                when (eYamlValue) {
                    is YamlParseErrors -> errors.addAll(eYamlValue.errors)
                    is YamlParseValue  -> yamlValueList.add(eYamlValue.value)
                }
            }

            return if (errors.size > 0)
                YamlParseErrors(errors)
            else
                YamlParseValue(YamlArray(yamlValueList, path))

        }
        else if (yamlObject is Double)
        {
            return YamlParseValue(YamlFloat(yamlObject, path))
        }
        else if (yamlObject is Int)
        {
            return YamlParseValue(YamlInteger(yamlObject.toLong(), path))
        }
        else if (yamlObject is Long)
        {
            return YamlParseValue(YamlInteger(yamlObject, path))
        }
        else if (yamlObject is String)
        {
            return YamlParseValue(YamlText(yamlObject, path))
        }
        else if (yamlObject is Boolean)
        {
            return YamlParseValue(YamlBool(yamlObject, path))
        }

        return YamlParseErrors(listOf(UnknownYamlType(yamlObject.javaClass.simpleName)))
    }

}


// ---------------------------------------------------------------------------------------------
// YAML STRING PARSE
// ---------------------------------------------------------------------------------------------

sealed class YamlParse<A>


data class YamlParseValue<A>(val value : A) : YamlParse<A>()


data class YamlParseErrors<A>(val errors : List<StringParseError>) : YamlParse<A>()


// Yaml String Parse > Errors
// ---------------------------------------------------------------------------------------------

sealed class StringParseError


data class TypeConversionError(val typeName : String) : StringParseError()
{
    override fun toString(): String = """
            |Type Conversion Error:
            |    Type: $typeName
            """.trimMargin()
}


data class UnknownYamlType(val typeName : String) : StringParseError()
{
    override fun toString(): String = """
            |Unknown Yaml Type:
            |    Type: $typeName
            """.trimMargin()
}