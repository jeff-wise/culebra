
import com.kispoko.culebra.*
import effect.*
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec



/**
 * Parse Data Tests
 */
class Tutorial : StringSpec()
{

    init
    {

        "Simple record type parsing example" {

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
                is Err -> fujiApple should beOfType<Eff<YamlParseError,Identity,Apple>>()
            }
        }


        "Simple wrapped type parsing example" {

            data class Grams(val value: Long)

            data class Apple(val kind: String, val weightInGrams: Grams, val isRipe: Boolean)


            val fujiAppleYamlString = """
                kind: Fuji
                weight_in_grams: 90
                is_ripe: yes
                """

            fun parseGrams(yamlValue: YamlValue): YamlParser<Grams> = when (yamlValue)
            {
                is YamlInteger -> effValue(Grams(yamlValue.number))
                else           -> effError(UnexpectedTypeFound(YamlType.INTEGER,
                                                               yamlType(yamlValue),
                                                               yamlValue.path))
            }

            fun parseApple(yamlValue: YamlValue): YamlParser<Apple> = when (yamlValue)
            {
                is YamlDict -> effApply(::Apple,
                                        yamlValue.text("kind"),
                                        yamlValue.at("weight_in_grams").apply(::parseGrams),
                                        yamlValue.boolean("is_ripe"))
                else        -> effError(UnexpectedTypeFound(YamlType.DICT,
                                                            yamlType(yamlValue),
                                                            yamlValue.path))
            }

            val myFujiApple = Apple("Fuji", Grams(90), true)

            val fujiApple = parseYaml(fujiAppleYamlString, ::parseApple, false)
            when (fujiApple) {
                is Val -> fujiApple.value shouldBe myFujiApple
                is Err -> fujiApple should beOfType<Eff<YamlParseError,Identity,Apple>>()
            }
        }
    }

}

