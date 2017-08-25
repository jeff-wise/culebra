
package data.rpg

import effect.Just
import effect.Nothing


// ---------------------------------------------------------------------------------------------
// WIZARD
// ---------------------------------------------------------------------------------------------

// Wizard > Yaml File
// ---------------------------------------------------------------------------------------------

val wizardYaml =
        """
    character_type: wizard
    character:
        spoken_languages:
        - common
        - draconic
        ability_scores:
            str: 10
            dex: 15
            con: 12
            int: 17
            wis: 13
            cha: 11
        spells:
        - name: fireball
          damage: 4d6
          level: 3
        - name: teleport
          level: 5
          description: You and anyone in a 10ft radius are transported to the destination you imagine.
        items:
        - type: weapon
          item:
            name: quarterstaff
            damage: 1d6
            weight: 4.0
        - type: ring
          item:
            name: Ring of Protection
            description: +2 bonus to Armor Class
    """


// Wizard > Kotlin Object
// ---------------------------------------------------------------------------------------------

val wizardCharacter =
    Wizard(
        CharacterData(
            // Languages
            listOf("common", "draconic"),
            AbilityScores(10, 15, 12, 17, 13, 11),
            listOf(
                Weapon("quarterstaff", "1d6", 4.0),
                Ring("Ring of Protection", "+2 bonus to Armor Class"))
        ),
        listOf(
            Spell("fireball", 3, Nothing(), Just("4d6")),
            Spell("teleport",
                  5,
                  Just("You and anyone in a 10ft radius are transported to the destination you imagine."),
                  Nothing())
        )
)