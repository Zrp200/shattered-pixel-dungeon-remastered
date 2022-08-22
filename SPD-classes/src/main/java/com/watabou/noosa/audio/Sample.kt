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
package com.watabou.noosa.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.watabou.noosa.Game
import com.watabou.utils.getAsset
import java.util.HashMap
import java.util.HashSet
import kotlin.math.max

object Sample {

    private var ids = HashMap<String, Sound>()

    @set:JvmName("enable")
    var isEnabled = true

    @set:JvmName("volume")
    var globalVolume = 1f

    @Synchronized
    fun reset() {
        for (sound in ids.values) sound.dispose()
        ids.clear()
        delayedSFX.clear()
    }

    @Synchronized
    fun pause() {
        for (sound in ids.values) sound.pause()
    }

    @Synchronized
    fun resume() {
        for (sound in ids.values) sound.resume()
    }

    @Synchronized
    fun load(vararg assets: String) {
        val toLoad = assets.filterNot(ids::containsKey)

        //don't make a new thread of all assets are already loaded
        if (toLoad.isEmpty()) return

        //load in a separate thread to prevent this blocking the UI
        object : Thread() {
            override fun run() {
                for (asset in toLoad) {
                    val newSound = Gdx.audio.newSound(getAsset(asset))
                    synchronized(this@Sample) { ids[asset] = newSound }
                }
            }
        }.start()
    }

    @Synchronized
    fun unload(src: String) {
        ids.remove(src)?.dispose()
    }

    @JvmOverloads
    fun play(id: String, volume: Float = 1f, pitch: Float = 1f) = play(id, volume, volume, pitch)

    @Synchronized
    fun play(id: String, leftVolume: Float, rightVolume: Float, pitch: Float): Long {
        val volume = max(leftVolume, rightVolume)
        return ids[id]
            ?.takeIf { isEnabled }
            ?.play(globalVolume * volume, pitch, rightVolume - leftVolume)
            ?: -1
    }

    private data class DelayedSoundEffect(
        var id: String,
        var delay: Float,
        var leftVol: Float,
        var rightVol: Float,
        var pitch: Float
    )

    private val delayedSFX = HashSet<DelayedSoundEffect>()

    @JvmOverloads
    fun playDelayed(id: String, delay: Float, volume: Float = 1f, pitch: Float = 1f) =
        playDelayed(id, delay, volume, volume, pitch)

    fun playDelayed(id: String, delay: Float, leftVolume: Float, rightVolume: Float, pitch: Float) {
        if (delay <= 0) {
            play(id, leftVolume, rightVolume, pitch)
            return
        }
        synchronized(delayedSFX) {
            delayedSFX.add(
                DelayedSoundEffect(
                    id = id,
                    delay = delay,
                    leftVol = leftVolume,
                    rightVol = rightVolume,
                    pitch = pitch,
                )
            )
        }
    }

    fun update() {
        synchronized(delayedSFX) {
            if (delayedSFX.isEmpty()) return
            for (sfx in delayedSFX.toTypedArray()) {
                sfx.delay -= Game.INSTANCE.elapsed
                if (sfx.delay <= 0) {
                    delayedSFX.remove(sfx)
                    play(sfx.id, sfx.leftVol, sfx.rightVol, sfx.pitch)
                }
            }
        }
    }
}