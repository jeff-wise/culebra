
package data.rpg

import effect.Maybe


/**
 * Fantasy Game Example
 *
 * We describe some basic data structures for a fantasy roleplaying game along with a
 * culebra parser.
 */


// ---------------------------------------------------------------------------------------------
// *****                                       TYPES                                       *****
// ---------------------------------------------------------------------------------------------


enum class CharacterType {
    FIGHTER,
    WIZARD
}

sealed class Character

data class CharacterData(val languages : List<String>,
                         val scores : AbilityScores,
                         val items : List<Item>)


data class Fighter(val data : CharacterData,
                   val spells : List<Feat>) : Character()


data class Wizard(val data : CharacterData,
                  val spells : List<Spell>) : Character()



data class AbilityScores(val str : Int,
                         val dex : Int,
                         val con : Int,
                         val int : Int,
                         val wis : Int,
                         val cha : Int)



data class Spell(val name : String,
                 val level : Int,
                 val description : Maybe<String>,
                 val damage : Maybe<String>)


data class Feat(val name : String,
                val bonus : Long)


sealed class Item

data class Weapon(val name : String,
                  val damage : String,
                  val weight : Double) : Item()

data class Ring(val name : String,
                val description : String) : Item()


enum class ItemType {
    WEAPON,
    RING
}


