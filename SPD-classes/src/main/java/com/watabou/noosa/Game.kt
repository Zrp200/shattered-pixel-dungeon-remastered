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
package com.watabou.noosa

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.GLVersion
import com.badlogic.gdx.utils.TimeUtils
import com.watabou.glwrap.Texture
import com.watabou.glwrap.VertexDataset
import com.watabou.glwrap.useDefault
import com.watabou.input.ControllerHandler
import com.watabou.input.InputHandler
import com.watabou.input.PointerEvent
import com.watabou.noosa.Script.Companion.get
import com.watabou.noosa.Script.Companion.reset
import com.watabou.noosa.audio.MusicPlayer
import com.watabou.noosa.audio.Sample
import com.watabou.noosa.graph.graphOn
import com.watabou.noosa.graph.updateGraph
import com.watabou.utils.Callback
import com.watabou.utils.DeviceCompat
import com.watabou.utils.PlatformSupport
import com.watabou.utils.Reflection
import java.io.PrintWriter
import java.io.StringWriter

abstract class Game(c: Class<out Scene>, platform: PlatformSupport) : ApplicationAdapter() {
    // Current scene
    @JvmField
    protected var scene: Scene = SceneStub()

    // New scene we are going to switch to
    private lateinit var requestedScene: Scene

    // true if scene switch is requested
    private var requestedReset = true

    // callback to perform logic during scene change
    private var onChange: SceneChangeCallback? = null

    private lateinit var versionContextRef: GLVersion

    //FIXME this is a temporary workaround to improve start times on android (first frame is 'cheated' and skips rendering)
    //this is partly to improve stats on google play, and partly to try and diagnose what the cause of slow loading times is
    //ultimately once the cause is found it should be fixed and this should no longer be needed
    private var justResumed = true

    init {
        sceneClass = c
        instance = this
        Game.platform = platform
    }

    override fun create() {
        density = Gdx.graphics.density
        if (density == Float.POSITIVE_INFINITY) {
            density = 100f / 160f //assume 100PPI if density can't be found
        }
        displayHeight = Gdx.graphics.displayMode.height
        displayWidth = Gdx.graphics.displayMode.width
        inputHandler = InputHandler(Gdx.input)
        if (ControllerHandler.controllersSupported()) {
            Controllers.addListener(ControllerHandler())
        }

        //refreshes texture and vertex data stored on the gpu
        versionContextRef = Gdx.graphics.glVersion
        useDefault()
        Texture.reload()
        VertexDataset.reload()
    }

    override fun resize(width: Int, height: Int) {
        val w = width
        var h = height
        if (w == 0 || h == 0) {
            return
        }

        // If the EGL context was destroyed, we need to refresh some data stored on the GPU.
        // This checks that by seeing if GLVersion has a new object reference
        if (versionContextRef !== Gdx.graphics.glVersion) {
            versionContextRef = Gdx.graphics.glVersion
            useDefault()
            Texture.reload()
            VertexDataset.reload()
        }
        h -= bottomInset
        if (h != Companion.height || w != Companion.width) {
            Companion.width = w
            Companion.height = h

            //TODO might be better to put this in platform support
            if (Gdx.app.type != Application.ApplicationType.Android) {
                displayWidth = Companion.width
                displayHeight = Companion.height
            }
            resetScene()
        }
    }

    override fun render() {

        // Prevents rare cases where the app is running twice.
        if (instance !== this) {
            finish()
            return
        }

        if (justResumed) {
            justResumed = false
            if (DeviceCompat.isAndroid()) return
        }

        get().resetCamera()

        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        scene.draw()
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)

        if (requestedReset) {
            requestedReset = false
            requestedScene = Reflection.newInstance(sceneClass)
            switchScene(requestedScene)
        }

        elapsed = timeScale * Gdx.graphics.deltaTime
        timeTotal += elapsed
        realTime = TimeUtils.millis()

        inputHandler.processAllEvents()

        Sample.INSTANCE.update()

        scene.update()
        Camera.updateAll()

        scene.let { if (graphOn) updateGraph(it) }
    }

    override fun pause() {
        PointerEvent.clearPointerEvents()
        scene.onPause()
        reset()
    }

    override fun resume() {
        justResumed = true
    }

    override fun dispose() {
        scene.destroy()
        MusicPlayer.INSTANCE.stop()
        Sample.INSTANCE.reset()
    }

    open fun finish() {
        Gdx.app.exit()
    }

    protected open fun switchScene(newScene: Scene) {
        scene.destroy()
        Camera.reset()
        VertexDataset.clear()

        scene = newScene
        onChange?.beforeCreate()
        scene.create()
        onChange?.afterCreate()
        onChange = null

        elapsed = 0f
        timeScale = 1f
        timeTotal = 0f
    }

    interface SceneChangeCallback {
        fun beforeCreate()
        fun afterCreate()
    }

    companion object {

		lateinit var instance: Game

        // New scene class
        @JvmField
        var sceneClass: Class<out Scene> = SceneStub::class.java

        //actual size of the display
		@JvmField
		var displayWidth = 0

        @JvmField
		var displayHeight = 0

        // Size of the EGL surface view
		@JvmField
		var width = 0

        @JvmField
		var height = 0

        //number of pixels from bottom of view before rendering starts
		@JvmField
		var bottomInset = 0

        // Density: mdpi=1, hdpi=1.5, xhdpi=2...
		@JvmField
		var density = 1f

		lateinit var version: String

        @JvmField
		var versionCode = 0

        var timeScale = 1f

        @JvmField
		var elapsed = 0f

        @JvmField
		var timeTotal = 0f

        @JvmField
		var realTime: Long = 0

		lateinit var inputHandler: InputHandler

		lateinit var platform: PlatformSupport

        @JvmStatic
		fun resetScene() {
            switchScene(sceneClass)
        }

        @JvmStatic
		@JvmOverloads
        fun switchScene(c: Class<out Scene>, callback: SceneChangeCallback? = null) {
            sceneClass = c
            instance.requestedReset = true
            instance.onChange = callback
        }

        @JvmStatic
		fun scene(): Scene {
            return instance.scene
        }

        @JvmStatic
		fun reportException(tr: Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            tr.printStackTrace(pw)
            pw.flush()
            if (Gdx.app != null) {
                Gdx.app.error("GAME", sw.toString())
            } else {
                //fallback if error happened in initialization
                System.err.println(sw)
            }
        }

        @JvmStatic
		fun runOnRenderThread(c: Callback) {
            Gdx.app.postRunnable { c.call() }
        }

        @JvmStatic
		fun vibrate(milliseconds: Int) {
            platform.vibrate(milliseconds)
        }
    }
}