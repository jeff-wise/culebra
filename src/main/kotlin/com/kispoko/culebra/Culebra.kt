
package com.kispoko.culebra


import effect.effError
import effect.effValue
import java.io.InputStream



@Suppress("UNCHECKED_CAST")
fun <A> parseYaml(yamlString : String,
                  yamlParser : (YamlValue) -> YamlParser<A>,
                  trackPath : Boolean = true)
                  : YamlParser<A>
{
    val yamlValue = YamlString.parse(yamlString, trackPath)

    return when (yamlValue)
    {
        is YamlParseValue  -> yamlParser(yamlValue.value)
        is YamlParseErrors -> effError(YamlStringError(yamlValue.errors, ValuePath()))
    }
}


@Suppress("UNCHECKED_CAST")
fun <A> parseYaml(yamlInputStream : InputStream,
                  yamlParser : (YamlValue) -> YamlParser<A>,
                  trackPath : Boolean = true)
                   : YamlParser<A>
{
    val yamlValue = YamlString.parse(yamlInputStream, trackPath)

    return when (yamlValue)
    {
        is YamlParseValue  -> yamlParser(yamlValue.value)
        is YamlParseErrors -> effError(YamlStringError(yamlValue.errors, ValuePath()))
    }
}


fun encodeYaml(yamlValue : YamlValue) : String
{
    val yaml = org.yaml.snakeyaml.Yaml()
    return yaml.dump(yamlValue.toPojo())
}


fun encodeYaml(encodableValue : ToYaml) : String
{
    val yaml = org.yaml.snakeyaml.Yaml()
    return yaml.dump(encodableValue.toYaml().toPojo())
}


interface ToYaml
{
    fun toYaml() : YamlValue
}
