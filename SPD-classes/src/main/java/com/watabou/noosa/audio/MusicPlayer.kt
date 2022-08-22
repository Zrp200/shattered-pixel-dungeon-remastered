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

import com.watabou.utils.DeviceCompat
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.watabou.noosa.Game
import com.watabou.utils.Random
import com.watabou.utils.getAsset

object MusicPlayer {

    private var player: Music? = null
    private var lastPlayed: String? = null
    private var looping = false

    var volume = 1f
        @Synchronized
        @JvmName("volume")
        set(value) {
            field = value
            player?.volume = value
        }

    private var trackMap: Map<String, Float> = emptyMap()

    private val trackQueue = ArrayList<String>()

    private fun populateTrackQueue() {
        trackMap.filterValues { Random.Float() < it }
            .keys
            .forEach(trackQueue::add)
    }

    @JvmField
    var shuffle = false

    @Synchronized
    fun play(assetName: String, looping: Boolean) {

        //iOS cannot play ogg, so we use an mp3 alternative instead
        val asset =
            if (DeviceCompat.isiOS()) assetName.replace(".ogg", ".mp3")
            else assetName
        if (isPlaying && lastPlayed != null && lastPlayed == assetName) {
            return
        }
        stop()
        lastPlayed = asset
        trackMap = emptyMap()
        this.looping = looping
        shuffle = false
        if (isEnabled) play(asset, null)
    }

    @Synchronized
    fun playTracks(tracks: Array<String>?, chances: FloatArray, shuffle: Boolean) {
        if (tracks.isNullOrEmpty() || tracks.size != chances.size) {
            stop()
            return
        }

        //iOS cannot play ogg, so we use an mp3 alternative instead
        if (DeviceCompat.isiOS()) {
            repeat(tracks.size) {
                tracks[it] = tracks[it].replace(".ogg", ".mp3")
            }
        }
        // can't do it the other way, unfortunately.
        val map = chances.zip(tracks) { c, t -> t to c }.toMap()
        // trackMap is initialized here
        if (isPlaying && map == trackMap) return
        stop()
        lastPlayed = null
        trackMap = map
        playTracks(shuffle)
    }

    @Synchronized
    fun playTracks(shuffle: Boolean) {
        trackQueue.clear()
        populateTrackQueue()
        looping = false
        this.shuffle = shuffle
        if (!isEnabled || trackQueue.isEmpty()) {
            return
        }
        play(trackQueue.removeAt(0), trackLooper)
    }

    private val trackLooper = Music.OnCompletionListener { music ->
        //we do this in a separate thread to avoid graphics hitching while the music is prepared
        //FIXME this fixes graphics stutter but there's still some audio stutter, perhaps keep more than 1 player alive?
        if (!DeviceCompat.isDesktop()) {
            object : Thread() {
                override fun run() = playNextTrack(music)
            }.start()
        } else {
            //don't use a separate thread on desktop, causes errors and makes no performance difference(?)
            playNextTrack(music)
        }
    }

    @Synchronized
    private fun playNextTrack(music: Music) {
        if (trackMap.isEmpty() || music !== player || player!!.isLooping) {
            return
        }
        stop()
        populateTrackQueue()
        if (shuffle) trackQueue.shuffle()
        if (!isEnabled || trackQueue.isEmpty()) {
            return
        }
        play(trackQueue.removeAt(0), trackLooper)
    }

    @Synchronized
    private fun play(track: String, listener: Music.OnCompletionListener?) {
        runCatching {
            with(Gdx.audio.newMusic(getAsset(track))) {
                player = this
                isLooping = this@MusicPlayer.looping
                volume = this@MusicPlayer.volume

                play()

                setOnCompletionListener(listener)
            }
        }.onFailure(Game::reportException)
    }

    @Synchronized
    fun end() {
        lastPlayed = null
        trackMap = emptyMap()
        stop()
    }

    @Synchronized
    fun pause() {
        player?.pause()
    }

    @Synchronized
    fun resume() {
        player?.run {
            play()
            isLooping = looping
        }
    }

    //TODO do we need to dispose every player? Maybe just stop them and keep an LRU cache of 2 or 3?
    @Synchronized
    fun stop() {
        player?.dispose()
        player = null
    }

    @get:Synchronized
    var isEnabled = true
        @Synchronized
        @JvmName("enable")
        set(value) {
            field = value
            if (isPlaying && !value) {
                stop()
            } else if (!isPlaying && value) {
                if (trackMap.isNotEmpty()) {
                    playTracks(shuffle)
                } else lastPlayed?.let {
                    play(it, looping)
                }
            }
        }

    @get:Synchronized
    val isPlaying
        get() = player?.isPlaying == true
}
