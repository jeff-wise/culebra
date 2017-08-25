
package data.rpg


import com.kispoko.culebra.*
import effect.effApply
import effect.effError
import effect.effValue
import effect.mapM


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
                                    it.mapM { parseLanguage(it) }
                            },
                            yamlValue.at("ability_scores") ap ::parseAbilityScores,
                            yamlValue.valueList("items") ap {
                                it.mapM { parseItem(it) }
                            } )
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}

// Character : Fighter
// ---------------------------------------------------------------------------------------------

fun parseFighter(yamlValue : YamlValue) : YamlParser<Character> = when (yamlValue)
{
    is YamlDict -> effApply(::Fighter,
                            parseCharacterData(yamlValue),
                            yamlValue.valueList("feats") ap { it.mapM { parseFeat(it) } } )
    else         -> error(UnexpectedTypeFound(YamlType.DICT, yamlType(yamlValue), yamlValue.path))
}


// Character : Wizard
// ---------------------------------------------------------------------------------------------

fun parseWizard(yamlValue : YamlValue) : YamlParser<Character> = when (yamlValue)
{
    is YamlDict -> effApply(::Wizard,
                            parseCharacterData(yamlValue),
                            yamlValue.valueList("spells") ap { it.mapM { parseSpell(it) } } )
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

// Spells
// ---------------------------------------------------------------------------------------------

//fun parseSpells(yamlValue : YamlValue) : YamlParser<List<Spell>> = when (yamlValue)
//{
//    is YamlArray -> yamlValue.map { parseSpell(it) }
//    else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//}

// FEAT
// ---------------------------------------------------------------------------------------------

// Feat
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

// Feats
// ---------------------------------------------------------------------------------------------

//fun parseFeats(yamlValue : YamlValue) : YamlParser<List<Feat>> = when (yamlValue)
//{
//    is YamlArray -> yamlValue.map { parseFeat(it) }
//    else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue)))
//}


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

// Item List
// ---------------------------------------------------------------------------------------------

//fun parseItems(yamlValue : YamlValue) : YamlParser<List<Item>> = when (yamlValue)
//{
//    is YamlArray -> yamlValue.map { parseItem(it) }
//    else         -> error(UnexpectedTypeFound(YamlType.ARRAY, yamlType(yamlValue), yamlValue.path))
//}

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
