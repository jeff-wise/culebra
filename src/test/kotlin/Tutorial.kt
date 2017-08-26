
import com.kispoko.culebra.*
import data.rpg.Feat
import data.rpg.parseCharacter
import data.rpg.wizardCharacter
import data.rpg.wizardYaml
import effect.Val
import effect.effApply
import effect.effError
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec



/**
 * Parse Data Tests
 */
class Tutorial : StringSpec()
{

    init
    {

        "Simple parsing example" {

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
        }

    }

}

