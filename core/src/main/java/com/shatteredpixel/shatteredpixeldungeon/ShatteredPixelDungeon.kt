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
package com.shatteredpixel.shatteredpixeldungeon

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfDread
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfSirensSong
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepSleep
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfFear
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Dazzling
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.CleansingDart
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.CrystalVaultRoom
import com.shatteredpixel.shatteredpixeldungeon.plants.Mageroyal
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene
import com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene
import com.watabou.noosa.Game
import com.watabou.noosa.Scene
import com.watabou.noosa.audio.MusicPlayer
import com.watabou.noosa.audio.Sample
import com.watabou.utils.Bundle.Companion.addAliases
import com.watabou.utils.DeviceCompat
import com.watabou.utils.PlatformSupport

class ShatteredPixelDungeon(platform: PlatformSupport)
    : Game(WelcomeScene::class.java, platform) {

    init {
        addAliases(
            mapOf(
                //pre-v1.3.0
                "com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm\$FallBleed" to Bleeding::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.plants.Dreamfoil" to Mageroyal::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.plants.Dreamfoil\$Seed" to Mageroyal.Seed::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Exhausting" to Dazzling::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Fragile" to Dazzling::class.java,

                //pre-v1.2.0
                "com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.SleepDart" to CleansingDart::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.VaultRoom" to CrystalVaultRoom::class.java,

                //pre-v1.1.0
                "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPetrification" to ScrollOfDread::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfAffection" to ScrollOfSirensSong::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfConfusion" to ScrollOfChallenge::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfHolyFuror" to PotionOfDivineInspiration::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfAdrenalineSurge" to PotionOfMastery::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPolymorph" to ScrollOfMetamorphosis::class.java,

                //pre-v1.0.0
                "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAffection" to StoneOfFear::class.java,
                "com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDeepenedSleep" to StoneOfDeepSleep::class.java
            )
        )
    }

    override fun create() {
        super.create()
        platform.updateSystemUI()
        SPDAction.loadBindings()
        MusicPlayer.INSTANCE.enable(SPDSettings.music())
        MusicPlayer.INSTANCE.volume(SPDSettings.musicVol() * SPDSettings.musicVol() / 100f)
        Sample.INSTANCE.enable(SPDSettings.soundFx())
        Sample.INSTANCE.volume(SPDSettings.SFXVol() * SPDSettings.SFXVol() / 100f)
        Sample.INSTANCE.load(*Assets.Sounds.all)
    }

    override fun finish() {
        if (!DeviceCompat.isiOS()) super.finish()
        else
            //can't exit on iOS (Apple guidelines), so just go to title screen
            switchScene(TitleScene::class.java)
    }

    override fun switchScene(newScene: Scene) {
        super.switchScene(newScene)
        (scene as? PixelScene)?.restoreWindows()
    }

    override fun resize(width: Int, height: Int) {
        if (width == 0 || height == 0) return
        if (height != Game.height || width != Game.width) {
            (scene as? PixelScene)?.apply {
                PixelScene.noFade = true
                saveWindows()
            }
        }
        super.resize(width, height)
        platform.updateDisplaySize()
    }

    override fun dispose() {
        super.dispose()
        GameScene.endActorThread()
    }

    companion object {

        // Constants for specific older versions of shattered used for data conversion.
        // Versions older than v0.9.3c are no longer supported, and data from them is ignored.
        const val v0_9_3c = 557 // 557 on iOS, 554 on other platforms
        const val v1_2_3 = 628
        const val v1_3_0 = 642

        @JvmStatic
        @JvmOverloads
        fun switchNoFade(c: Class<out Scene>, callback: SceneChangeCallback? = null) {
            PixelScene.noFade = true
            switchScene(c, callback)
        }

        @JvmStatic
		@JvmOverloads
        fun seamlessResetScene(callback: SceneChangeCallback? = null) {
            (scene() as? PixelScene)?.apply {
                saveWindows()
                switchNoFade(sceneClass, callback)
            } ?: resetScene()
        }
    }
}