
import com.kispoko.culebra.encodeYaml
import com.kispoko.culebra.parseYaml
import data.rpg.parseCharacter
import data.rpg.wizardCharacter
import data.rpg.wizardYaml
import effect.Val
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec



/**
 * Parse Data Tests
 */
class RPG : StringSpec()
{

    init
    {

        "Parse the wizard charcter definition" {

            val wizard = parseYaml(wizardYaml, ::parseCharacter, false)
            when (wizard) {
                is Val -> wizard.value shouldBe wizardCharacter
            }
        }


        "Encode the wizard object into YAML and the parse it. It should be the same." {

            val wizardYamlString = encodeYaml(wizardCharacter)

            val wizard = parseYaml(wizardYamlString, ::parseCharacter, false)
            when (wizard) {
                is Val -> wizard.value shouldBe wizardCharacter
            }
        }
    }

}

