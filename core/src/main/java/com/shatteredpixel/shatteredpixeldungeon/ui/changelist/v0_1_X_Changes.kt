/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

@file:JvmName("v0_1_X_Changes")

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.items.Ankh
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons

fun addAllChanges(changeInfos: ChangeList) = changeInfos {
    version("v0.1.X") {
        section("v0.1.1") {
            + ChangeButton(Icons.get(Icons.SHPX), "Developer Commentary")
                .apply {
                    +listOf(
                        "Released August 15th, 2014",
                        "10 days after Shattered v0.1.0"
                    )
                    +"\nv0.1.1 was the first update that added new content! I added new ankh functionality and a new type of food based on suggestions and feedback from v0.1.0."
                    +"\nThis update also included Shattered's first contentious change: I removed the automatic revival feature from the dew vial. This led to many accidental deaths for players who were used to the automatic revive from Pixel Dungeon. I kept receiving complaints about it years later!"
                    +"\nThese early updates were much smaller and less polished compared to more modern ones, which meant I released them much faster. I eventually shifted towards slower updates with more size and quality."
                }
            Blandfruit()() {
                +"Players who chance upon gardens or who get lucky while trampling grass may come across a new plant: the _Blandfruit._"
                +"\nAs the name implies, the fruit from this plant is pretty unexceptional, and will barely do anything for you on its own. Perhaps there is some way to prepare the fruit with another ingredient..."
            }

            Ankh()("Revival Item Changes") {
                +"When the Dew Vial was initially added to Pixel Dungeon, its essentially free revive made ankhs pretty useless by comparison. To fix this, both items have been adjusted to combine to create a more powerful revive."
                +"\nDew Vial nerfed:"
                +listOf(
                    "Still grants a full heal at full charge, but grants less healing at partial charge.",
                    "No longer revives the player if they die.",
                )
                +"\nAnkh buffed:"
                +listOf(
                    "Can now be blessed with a full dew vial, to gain the vial's old revive effect."
                )
            }
            +ChangeButton(
                ItemSprite(ItemSpriteSheet.SCROLL_BERKANAN, null),
                "Misc Item Changes",
                """
                Sungrass buffed:
                _-_ Heal scaling now scales with max hp.
        
                Scroll of Psionic Blast rebalanced:
                _-_ Now deals less self damage, and damage is more consistent.
                _-_ Duration of self stun/blind effect increased.
        
                Scroll of lullaby reworked:
                _-_ No longer instantly sleeps enemies, now afflicts them with drowsy, which turns into magical sleep.
                _-_ Magically slept enemies will only wake up when attacked.
                _-_ Hero is also affected, and will be healed by magical sleep.
                """)
        }
        section("v0.1.0") {
            commentary(
                "August 5th, 2014",
                "69 days" to "Pixel Dungeon v1.7.1",
                "9 days" to "v1.7.1 source release"
            ) {
                +"v0.1.0 and v0.1.1 were extremely early Shattered updates that were only distributed via the Pixel Dungeon Subreddit. At this stage of development Shattered was basically the same game as Pixel Dungeon v1.7.1."
                +"\nI started playing Pixel Dungeon in mid 2013. I loved the game but was frustrated with the balance of some items. When Pixel Dungeon went open source I decided to make Shattered as a balance modification. I called it Shattered as 'Shattered Pixel' was an old trade name I had, and the mod was going to 'shatter' Pixel Dungeon's balance."
                +"\nAt the time I didn't have any plans to add new content, I thought I was just going to spend a couple months rebalancing the game and that was it!"
            }
            +ChangeButton(
                ItemSprite(ItemSpriteSheet.SEED_EARTHROOT, null), "Seed Changes"
            ).apply {
                +listOf(
                    "Blindweed buffed, now cripples as well as blinds.\n",
                    "Sungrass nerfed, heal scales up over time, total heal reduced by combat.\n",
                    "Earthroot nerfed, damage absorb down to 50% from 100%, total shield unchanged.\n",
                    "Icecap buffed, freeze effect is now much stronger in water.\n",
                )
            }
            +ChangeButton(ItemSprite(ItemSpriteSheet.POTION_SILVER, null), "Potion Changes").apply {
                +listOf(
                    "Potion of Purity buffed, immunity duration increased to 10 turns from 5, clear effect radius increased.\n",
                    "Potion of Frost buffed, freeze effect is now much stronger in water."
                )
            }
            +ChangeButton(ItemSprite(ItemSpriteSheet.SCROLL_BERKANAN, null), "Scroll Changes")
                .apply {
                    +listOf(
                        "Scroll of Psionic blast reworked, now rarer and much stronger, but deals damage to the hero.\n",
                        "Scroll of Challenge renamed to Scroll of Rage, now amoks nearby enemies."
                    )
                }
        }
    }
}