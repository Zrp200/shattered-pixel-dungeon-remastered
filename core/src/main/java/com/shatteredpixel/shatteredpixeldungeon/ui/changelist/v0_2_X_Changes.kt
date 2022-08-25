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

@file:JvmName("v0_2_X_Changes")

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.Assets
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.*

@JvmName("addAllChanges")
fun ChangeList.add02XChanges() = this {
    version("0.2.X") {
        section("v0.2.4") {
            commentary("February 23rd, 2015", "48 days" to "Shattered v0.2.3") {
                +"""
                    v0.2.4 was a very important update, even if it was mainly porting another update from Pixel Dungeon's source code. This is because the v1.7.5 source included a change that was quite controversial: Degradation.

                    In Pixel Dungeon (after v1.7.5) upgraded gear degrades as it is used, and this degradation is usually reset by upgrading an item further. The goal of this change was to discourage hoarding upgrades and dumping them on a single weapon, but (especially in v.1.7.5) many players felt that degradation ruined the fun of the game.

                    After a lot of consideration, I decided to not implement degradation into Shattered. Instead I started planning other changes to solve this problem without restricting gameplay quite as much. Those changes would eventually show up in updates like v0.4.0 and v0.8.0.
                """.trimIndent()
            }
            Honeypot()("Pixel Dungeon v1.7.5") {
                +"v1.7.3 - v1.7.5 Source Implemented, with exceptions:"
                list(
                    "Degredation not implemented.",
                    "Badge syncing not implemented",
                    "Scroll of Weapon Upgrade renamed to Magical Infusion, works on armor.",
                    "Scroll of Enchantment not implemented, Arcane stylus has not been removed.",
                    "Bombs have been reworked/nerfed: they explode after a delay, no longer stun, deal more damage at the center of the blast, affect the world (destroy items, blow up other bombs).",
                    spacing = 2,
                )
            }
            item(BANDOLIER, "New Content") {
                list(
                    "The huntress has been buffed: starts with Potion of Mind Vision identified, now benefits from strength on melee attacks, and has a chance to reclaim a single used ranged weapon from each defeated enemy.",
                    "A new container: The Potion Bandolier! Potions can now shatter from frost, but the bandolier can protect them.\n",
                    "Shops now stock a much greater variety of items, some item prices have been rebalanced.",
                    "Added Merchant's Beacon.",
                    "Added initials for IDed scrolls/potions.",
                    spacing = 2
                )
            }
            misc {
                -"Going down stairs no longer increases hunger, going up still does."
                +""
                -"Many, many bugfixes."
                -"Some UI improvements."
                -"Ingame audio quality improved."
                -"Unstable spellbook buffed."
                -"Psionic blasts deal less self-damage."
                -"Potions of liquid flame affect a 3x3 grid."
            }
        }
        section("v0.2.3") {
            commentary("January 6th, 2015", "64 days" to "Shattered v0.2.2") {
                +"""
                    v0.2.3 was another update made of many small improvements. The most significant game content in this update was major additions and refinements to artifacts, including preventing duplicates.
        
                    This was also the first update where I started really trying to focus on game stability and code quality. In v0.2.3 I made some big changes to the internal code of the game's save system, which fixed lots of cases where the game would fail to save and load properly.
        
                    Lastly, v0.2.3 brought the addition of the game's supporter system! While monetization isn't as exciting as new game content, the supporter system is the primary reason why I've been able to work on the game for so long.
                """.trimIndent()
            }
            item(ARTIFACT_HOURGLASS, "Artifact Changes") {
                +"Added 4 new artifacts:"
                -"Alchemist's Toolkit"
                -"Unstable Spellbook"
                -"Timekeeper's Hourglass"
                -"Dried Rose"
                +""
                -"Artifacts are now unique over each run"
                -"Artifacts can now be cursed!"
                -"Cloak of Shadows is now exclusive to the rogue"
                -"Smaller Balance Changes and QOL improvements to almost every artifact"
            }
            item(POTION_CRIMSON, "Balance Changes") {
                -"Health potion farming has been nerfed from all sources"
                -"Freerunner now moves at very high speeds when invisible"
                -"Ring of Force buffed significantly"
                -"Ring of Evasion reworked again"
                -"Improved the effects of some blandfruit types"
                -"Using throwing weapons now cancels stealth"
            }
            misc {
                list(
                    "Implemented a donation system in the Google Play version of Shattered",
                    "Significantly increased the stability of the save system",
                    "Increased the number of visible rankings to 11 from 6",
                    "Improved how the player is interrupted by harmful events",
                    spacing = 2
                )
            }
        }
        section("v0.2.2") {
            commentary("November 3rd, 2014", "21 days" to "Shattered v0.2.1") {
                +"v0.2.2 was Shattered's first update that didn't have a specific focus. Instead this update was focused on making a bunch of little improvements."
                +""
                +"The largest change was the integration of Pixel Dungeon's source code from v1.7.2, which included synchronous movement! It's something we take for granted now, but before this change every on-screen character had to move one at a time. This slowed the pace of the game to a crawl whenever enemies were on screen."
                +""
                +"Heroes remains also received big changes this update. In Pixel Dungeon you could use remains to consistently pass highly upgraded armor from one run to the next. I felt this violated the roguelike nature of the game, and so I nerfed remains to prevent this."
            }
            item(STONE_AUGMENTATION, "Pixel Dungeon v1.7.2") {
                +"Implemented directly from v1.7.2:"
                -"Synchronous Movement"
                -"Challenges"
                -"UI improvements"
                -"Vertigo debuff"
                +""
                +"Implement with changes:"
                -"Weightstone: Increases either speed or damage, at the cost of the other, instead of increasing either speed or accuracy."
                +""
                +"Not Implemented:"
                -"Key ring and unstackable keys"
                -"Blindweed has not been removed"
            }
            button(Assets.Environment.TERRAIN_FEATURES, 112, 112, 16, 16, "New Plants") {
                +"Added two new plants:"
                -"Stormvine, which brews into levitation"
                -"Dreamfoil, which brews into purity"
                +""
                -"Potion of levitation can now be thrown to make a cloud of confusion gas\n"
                +""
                +"Removed gas collision logic, gasses can now stack without limitation."
            }
            item(REMAINS, "Heroes Remains") {
                +"Heroes remains have been significantly adjusted to prevent strategies that exploit them, but also to increase their average loot."
                +"\nRemains have additional limitations:"
                -"Heros will no longer drop remains if they have obtained the amulet of yendor, or die 5 or more floors above the deepest floor they have reached"
                -"Class exclusive items can no longer appear in remains"
                -"Items found in remains are now more harshly level-capped"
                -"Remains are not dropped, and cannot be found, when challenges are enabled."

                +"\nHowever:"
                -"Remains can now contain useful items from the inventory, not just equipped items."
                -"Remains are now less likely to contain gold."
            }
        }
        section("v0.2.1") {
            commentary("October 13th, 2014", "28 days" to "Shattered v0.2.0") {
                +"v0.2.1 was the first in a short lived series of 'region overhaul' updates. Thanks to releasing on Google Play, Shattered was getting a huge influx of new players, and I wanted to make some changes that they would appreciate. The three new minibosses and Goo changes were all made to try and help new players get used to the game."
                +""
                +"This update also continued v0.2.0's trend of expanding Shattered's scope. I was no longer just planning to change items, but was now making additions and reworks to regions of the dungeon as well!"
            }
            button(Assets.Sprites.GHOST, 0, 0, 14, 15, "New Sewer Quests") {
                list(
                    "Removed the dried rose quest (the rose will return...)",
                    "Tweaked the mechanics of the fetid rat quest",
                    "Added a gnoll trickster quest",
                    "Added a great crab quest",
                    spacing = 2
                )
            }
            button(Assets.Sprites.GOO, 43, 3, 14, 11, "Goo Changes") {
                +"Goo's animations have been overhauled, including a particle effect for the area of its pumped up attack."
                +""
                +"Goo's arena has been updated to give more room to maneuver, and to be more variable."
            }
            item(GUIDE_PAGE, "Story & Signpost Changes") {
                +"Most text in the sewers has been overhauled, including descriptions, quest dialogues, signposts, and story scrolls"
            }
        }
        section("v0.2.0") {
            commentary("September 15th, 2014", "31 days" to "Shattered v0.1.1") {
                +"""
                    v0.2.0 was the first version of Shattered to release on Google Play! I had originally wanted to wait longer, but I was getting flooded with messages about it.
                    
                    Artifacts came from realizing it would be very difficult to make some rings worth upgrading by just buffing them. Instead, I decided to put their mechanics  on a new class of item that didn't need upgrades. Artifacts ended up becoming Shattered's first flagship feature!
                    
                    I feel like this was the start of a new era for Shattered's development, as updates become about making much more significant changes to content than just balance adjustments.
                    
                    Giving the Cloak of Shadows to the Rogue was also my first attempt at a class rework. It was a much more simple change than later reworks, and I ended up revisiting the Rogue in v0.6.2.
                """.trimIndent()
            }
            HornOfPlenty()("Artifacts!") {
                +"""
                Added artifacts to the game!
                    
                Artifacts are unique items which offer new gameplay opportunities and grow stronger through unique means.
                    
                Removed 7 Rings... And Replaced them with 7 Artifacts!""".trimIndent()
                -"Ring of Shadows becomes Cloak of Shadows"
                -"Ring of Satiety becomes Horn of Plenty"
                -"Ring of Mending becomes Chalice of Blood"
                -"Ring of Thorns becomes Cape of Thorns"
                -"Ring of Haggler becomes Master Thieves' Armband"
                -"Ring of Naturalism becomes Sandals of Nature"
            }
            item(RING_DIAMOND, "New Rings!") {
                +"To replace the lost rings, 6 new rings have been added:"
                -"Ring of Force"
                -"Ring of Furor"
                -"Ring of Might"
                -"Ring of Tenacity"
                -"Ring of Sharpshooting"
                -"Ring of Wealth"

                +"\nThe 4 remaining rings have also been tweaked or reworked entirely:"
                -"Ring of Accuracy"
                -"Ring of Elements"
                -"Ring of Evasion"
                -"Ring of Haste"
            }
            misc {
                list(
                    "Nerfed farming health potions from fly swarms.",
                    "Buffed crazed bandit and his drops.",
                    "Made Blandfruit more common.",
                    "Nerfed Assassin bonus damage slightly, to balance with him having an artifact now.",
                    "Added a welcome page!",
                    spacing = 2
                )
            }
        }
    }
}