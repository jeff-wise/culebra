

import com.kispoko.culebra.*
import io.kotlintest.matchers.shouldBe

import io.kotlintest.specs.StringSpec



/**
 * Yaml Value Parsing Tests
 *
 * Given any valid YAML string, it should be parsed into the correct YamlValue, or if given an
 * invalid YAML string, should return the appropriate errors.
 */
class PrimitiveParseTests : StringSpec()
{
    init
    {

        "Parses a dictionary with an Integer value" {

            // Input
            val yamlString = "a : 5"

            // Output
            val result = YamlParseValue(
                             YamlDict(
                                 hashMapOf("a" to YamlInteger(5, null)),
                                 null
                             )
                         )

            YamlString.parse(yamlString, false) shouldBe result
        }


        "Parses a dictionary with an Float value" {

            // Input
            val yamlString = "a : 5.0"

            // Output
            val result = YamlParseValue(
                             YamlDict(
                                 hashMapOf("a" to YamlFloat(5.0, null)),
                                 null
                             )
                         )

            YamlString.parse(yamlString, false) shouldBe result
        }


        "Parses a dictionary with an String value" {

            // Input
            val yamlString = "a : culebras"

            // Output
            val result = YamlParseValue(
                             YamlDict(
                                 hashMapOf("a" to YamlText("culebras", null)),
                                 null
                             )
                         )

            YamlString.parse(yamlString, false) shouldBe result
        }


        "Parses a dictionary with multiple String values" {

            // Input
            val yamlString = """
                a : Bogota
                b : Medellin
                c : Bucaramanga
                """

            // Output
            val result =
                    YamlParseValue(
                        YamlDict(
                            hashMapOf(
                                "a" to YamlText("Bogota", null),
                                "b" to YamlText("Medellin", null),
                                "c" to YamlText("Bucaramanga", null)),
                            null
                        )
                    )

            YamlString.parse(yamlString, false) shouldBe result
        }

    }
}