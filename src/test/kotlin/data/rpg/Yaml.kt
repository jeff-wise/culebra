
package data.rpg


import com.kispoko.culebra.*
import effect.*


// ---------------------------------------------------------------------------------------------
// *****                                       PARSERS                                     *****
// ---------------------------------------------------------------------------------------------

// CHARACTER
// ---------------------------------------------------------------------------------------------

// Character
// ---------------------------------------------------------------------------------------------

fun parseCharacter(yamlValue : YamlValue) : YamlParser<Character> = when (yamlValue)
{
    is YamlDict -> {
        (yamlValue.at("character_type") ap ::parseCharacterType) ap {
            if (it == CharacterType.FIGHTER)
                yamlValue.at("character") ap ::parseFighter
            else
                yamlValue.at("character") ap ::parseWizard
        }
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// Character > Type
// ---------------------------------------------------------------------------------------------

fun parseCharacterType(yamlValue : YamlValue) : YamlParser<CharacterType> = when (yamlValue)
{
    is YamlText -> {
        when (yamlValue.text)
        {
            "fighter" -> effValue(CharacterType.FIGHTER)
            "wizard"  -> effValue(CharacterType.WIZARD)
            else      -> effError<YamlParseError,CharacterType>(
                            UnexpectedStringValue(yamlValue.text, yamlValue.path))
        }
    }
    else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue), yamlValue.path))
}

// Character > Data
// ---------------------------------------------------------------------------------------------

fun parseCharacterData(yamlValue : YamlValue) : YamlParser<CharacterData> = when (yamlValue)
{
    is YamlDict -> effApply(::CharacterData,
                            yamlValue.valueList("spoken_languages") ap {
                                    it.mapMI { parseLanguage(it) }
                            },
                            yamlValue.at("ability_scores") ap ::parseAbilityScores,
                            yamlValue.valueList("items") ap {
                                it.mapMI { parseItem(it) }
                            } )
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// Character : Fighter
// ---------------------------------------------------------------------------------------------

fun parseFighter(yamlValue : YamlValue) : YamlParser<Character> = when (yamlValue)
{
    is YamlDict -> effApply(::Fighter,
                            parseCharacterData(yamlValue),
                            yamlValue.valueList("feats") ap { it.mapMI { parseFeat(it) } } )
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}


// Character : Wizard
// ---------------------------------------------------------------------------------------------

fun parseWizard(yamlValue : YamlValue) : YamlParser<Character> = when (yamlValue)
{
    is YamlDict -> effApply(::Wizard,
                            parseCharacterData(yamlValue),
                            yamlValue.valueList("spells") ap { it.mapMI { parseSpell(it) } } )
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// LANGUAGE
// ---------------------------------------------------------------------------------------------

// Language
// ---------------------------------------------------------------------------------------------

fun parseLanguage(yamlValue : YamlValue) : YamlParser<String> = when (yamlValue)
{
    is YamlText -> effValue(yamlValue.text)
    else        -> effError(UnexpectedTypeFound(YamlType.TEXT,
                                                yamlType(yamlValue),
                                                yamlValue.path))
}


// ABILITY SCORES
// ---------------------------------------------------------------------------------------------

fun parseAbilityScores(yamlValue : YamlValue) : YamlParser<AbilityScores> = when (yamlValue)
{
    is YamlDict -> effApply(::AbilityScores,
                            yamlValue.integer("str"),
                            yamlValue.integer("dex"),
                            yamlValue.integer("con"),
                            yamlValue.integer("int"),
                            yamlValue.integer("wis"),
                            yamlValue.integer("cha"))
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// SPELL
// ---------------------------------------------------------------------------------------------

// Spell
// ---------------------------------------------------------------------------------------------

fun parseSpell(yamlValue : YamlValue) : YamlParser<Spell> = when (yamlValue)
{
    is YamlDict -> {
        effApply(::Spell,
                yamlValue.text("name"),
                yamlValue.integer("level"),
                yamlValue.maybeText("description"),
                yamlValue.maybeText("damage"))
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// FEAT
// ---------------------------------------------------------------------------------------------

fun parseFeat(yamlValue : YamlValue) : YamlParser<Feat> = when (yamlValue)
{
    is YamlDict -> {
        effApply(::Feat,
                yamlValue.text("name"),
                yamlValue.long("bonus"))
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// ITEM
// ---------------------------------------------------------------------------------------------

// Item
// ---------------------------------------------------------------------------------------------

fun parseItem(yamlValue : YamlValue) : YamlParser<Item> = when (yamlValue)
{
    is YamlDict -> {
        (yamlValue.at("type") ap ::parseItemType) ap {
            if (it == ItemType.WEAPON)
                yamlValue.at("item") ap ::parseWeapon
            else
                yamlValue.at("item") ap ::parseRing
        }
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}


// Item Type
// ---------------------------------------------------------------------------------------------

fun parseItemType(yamlValue : YamlValue) : YamlParser<ItemType> = when (yamlValue)
{
    is YamlText -> {
        when (yamlValue.text) {
            "weapon" -> effValue(ItemType.WEAPON)
            "ring"   -> effValue(ItemType.RING)
            else     -> effError<YamlParseError,ItemType>(
                            UnexpectedStringValue(yamlValue.text, yamlValue.path))
        }
    }
    else        -> error(UnexpectedTypeFound(YamlType.TEXT, yamlType(yamlValue), yamlValue.path))
}

// Item > Ring
// ---------------------------------------------------------------------------------------------

fun parseRing(yamlValue : YamlValue) : YamlParser<Item> = when (yamlValue)
{
    is YamlDict -> {
        effApply(::Ring,
                yamlValue.text("name"),
                yamlValue.text("description"))
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// Item > Weapon
// ---------------------------------------------------------------------------------------------

fun parseWeapon(yamlValue : YamlValue) : YamlParser<Item> = when (yamlValue)
{
    is YamlDict -> {
        effApply(::Weapon,
                yamlValue.text("name"),
                yamlValue.text("damage"),
                yamlValue.double("weight"))
    }
    else        -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}


// ---------------------------------------------------------------------------------------------
// *****                                      ENCODERS                                     *****
// ---------------------------------------------------------------------------------------------

// CHARACTER
// ---------------------------------------------------------------------------------------------

// Character
// ---------------------------------------------------------------------------------------------

fun encodeCharacter(character : Character) : YamlValue = when (character)
{
    is Fighter -> {
        YamlDict(
            hashMapOf("character_type" to YamlText("fighter"),
                      "character"      to encodeFigher(character)))
    }
    is Wizard -> {
        YamlDict(
            hashMapOf("character_type" to YamlText("wizard"),
                      "character"      to encodeWizard(character)))
    }
}

// Character > Fighter
// ---------------------------------------------------------------------------------------------

fun encodeFigher(fighter : Fighter) : YamlValue
{
    val featValues = fighter.feats.map { encodeFeat(it) }

    return YamlDict(hashMapOf("feats" to YamlArray(featValues)))
               .union(encodeCharacterData(fighter.data))
}


// Character > Wizard
// ---------------------------------------------------------------------------------------------

fun encodeWizard(wizard : Wizard) : YamlValue
{
    val spellValues = wizard.spells.map { encodeSpell(it) }

    return YamlDict(hashMapOf("spells" to YamlArray(spellValues)))
               .union(encodeCharacterData(wizard.data))
}

// Character > Shared Data
// ---------------------------------------------------------------------------------------------

fun encodeCharacterData(characterData : CharacterData) : YamlDict
{
    val languageValues = characterData.languages.map { encodeLanguage(it) }
    val itemValues = characterData.items.map { encodeItem(it) }

    return YamlDict(
               hashMapOf("spoken_languages" to YamlArray(languageValues),
                         "ability_scores"   to encodeAbilityScores(characterData.scores),
                         "items"            to YamlArray(itemValues)))
}


// ABILITY SCORES
// ---------------------------------------------------------------------------------------------

fun encodeAbilityScores(abilityScores : AbilityScores) : YamlDict =
    YamlDict(
        hashMapOf("str" to YamlInteger(abilityScores.str),
                  "dex" to YamlInteger(abilityScores.dex),
                  "con" to YamlInteger(abilityScores.con),
                  "int" to YamlInteger(abilityScores.int),
                  "wis" to YamlInteger(abilityScores.wis),
                  "cha" to YamlInteger(abilityScores.cha)))


// LANGUAGE
// ---------------------------------------------------------------------------------------------

fun encodeLanguage(language : String) : YamlValue = YamlText(language)


// ITEM
// ---------------------------------------------------------------------------------------------

fun encodeItem(item : Item) : YamlValue = when (item)
{
    is Weapon -> {
        YamlDict(
            hashMapOf("type" to YamlText("weapon"),
                      "item" to encodeWeapon(item)))
    }
    is Ring -> {
        YamlDict(
            hashMapOf("type" to YamlText("ring"),
                      "item" to encodeRing(item)))
    }
}

// Item > Weapon
// ---------------------------------------------------------------------------------------------

fun encodeWeapon(weapon : Weapon) : YamlValue =
    YamlDict(
        hashMapOf("name"   to YamlText(weapon.name),
                  "damage" to YamlText(weapon.damage),
                  "weight" to YamlFloat(weapon.weight)))

// Item > Ring
// ---------------------------------------------------------------------------------------------

fun encodeRing(ring : Ring) : YamlValue =
    YamlDict(
        hashMapOf("name"        to YamlText(ring.name),
                  "description" to YamlText(ring.description)))


// SPELL
// ---------------------------------------------------------------------------------------------

fun encodeSpell(spell : Spell) : YamlValue =
    YamlDict(hashMapOf("name"  to YamlText(spell.name),
                       "level" to YamlInteger(spell.level)))
    .maybeUnion(spell.description apply {
        Just(YamlDict(hashMapOf("description" to YamlText(it))))
    })
    .maybeUnion(spell.damage apply {
        Just(YamlDict(hashMapOf("damage" to YamlText(it))))
    })


// FEAT
// ---------------------------------------------------------------------------------------------

fun encodeFeat(feat : Feat) : YamlValue =
    YamlDict(
        hashMapOf("name"  to YamlText(feat.name),
                  "bonus" to YamlInteger(feat.bonus)))
