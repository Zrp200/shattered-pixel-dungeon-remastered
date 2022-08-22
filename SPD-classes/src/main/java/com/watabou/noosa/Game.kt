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
import com.watabou.glwrap.useDefaultBlending
import com.watabou.input.ControllerHandler
import com.watabou.input.InputHandler
import com.watabou.input.PointerEvent
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

abstract class Game : ApplicationAdapter() {

    /**
     * Current scene.
     */
    @JvmField
    var scene: Scene = SceneStub()

    /**
     * New scene we are going to switch to.
     */
    private lateinit var requestedScene: Scene

    /**
     * New scene class we are going to switch to.
     */
    @JvmField
    protected var sceneClass: Class<out Scene> = SceneStub::class.java

    /**
     * Whether the scene switch is requested.
     */
    private var requestedReset = true

    /**
     * Callback to perform logic during scene change.
     */
    private var onChange: SceneChangeCallback? = null

    private lateinit var versionContextRef: GLVersion

    //FIXME this is a temporary workaround to improve start times on android (first frame is 'cheated' and skips rendering)
    //this is partly to improve stats on google play, and partly to try and diagnose what the cause of slow loading times is
    //ultimately once the cause is found it should be fixed and this should no longer be needed
    private var justResumed = true

    /**
     * Platform the game is running on.
     */
    lateinit var platform: PlatformSupport

    /**
     * Actual width of the display.
     */
    @JvmField
    var displayWidth = 0

    /**
     * Actual height of the display.
     */
    @JvmField
    var displayHeight = 0

    /**
     * Width of the EGL surface view.
     */
    @JvmField
    var width = 0

    /**
     * Height of the EGL surface view.
     */
    @JvmField
    var height = 0

    /**
     * Number of pixels from bottom of view before rendering starts.
     */
    @JvmField
    var bottomInset = 0

    /**
     * Screen density: mdpi=1, hdpi=1.5, xhdpi=2...
     */
    @JvmField
    var density = 1f

    private var timeScale = 1f

    @JvmField
    var elapsed = 0f // FIXME: make setters private here and for other similar properties

    @JvmField
    var timeTotal = 0f

    @JvmField
    var realTime: Long = 0

    lateinit var inputHandler: InputHandler

    override fun create() {

        density = Gdx.graphics.density
        if (density == Float.POSITIVE_INFINITY) density = 100f / 160f // Assume 100PPI if density can't be found

        displayHeight = Gdx.graphics.displayMode.height
        displayWidth = Gdx.graphics.displayMode.width
        inputHandler = InputHandler(Gdx.input)
        if (ControllerHandler.controllersSupported()) {
            Controllers.addListener(ControllerHandler())
        }

        //refreshes texture and vertex data stored on the gpu
        versionContextRef = Gdx.graphics.glVersion
        useDefaultBlending()
        Texture.reload()
        VertexDataset.reload()
    }

    override fun resize(newWidth: Int, newHeight: Int) {

        val w = newWidth
        var h = newHeight
        if (w == 0 || h == 0) {
            return
        }

        // If the EGL context was destroyed, we need to refresh some data stored on the GPU.
        // This checks that by seeing if GLVersion has a new object reference.
        if (versionContextRef !== Gdx.graphics.glVersion) {
            versionContextRef = Gdx.graphics.glVersion
            useDefaultBlending()
            Texture.reload()
            VertexDataset.reload()
        }
        h -= bottomInset
        if (h != height || w != width) {
            width = w
            height = h

            //TODO might be better to put this in platform support
            if (Gdx.app.type != Application.ApplicationType.Android) {
                displayWidth = width
                displayHeight = height
            }
            resetScene()
        }
    }

    override fun render() {

        if (justResumed) {
            justResumed = false
            if (DeviceCompat.isAndroid()) return
        }

        Script.get().resetCamera()

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

        Sample.update()

        scene.update()
        Camera.updateAll()

        scene.let { if (graphOn) updateGraph(it) }
    }

    override fun pause() {
        PointerEvent.clearPointerEvents()
        scene.onPause()
        Script.reset()
    }

    override fun resume() {
        justResumed = true
    }

    override fun dispose() {
        scene.destroy()
        MusicPlayer.stop()
        Sample.reset()
    }

    /**
     * Tries to close the game.
     */
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

    /**
     * Requests a scene switch.
     * @param cl class of the scene to change the current one to
     * @param callback callback to perform logic during scene change
     */
    @JvmOverloads
    fun switchScene(cl: Class<out Scene>, callback: SceneChangeCallback? = null) {
        sceneClass = cl
        requestedReset = true
        onChange = callback
    }

    /**
     * Requests a scene reset.
     * @param callback callback to perform logic during scene reset
     */
    @JvmOverloads
    fun resetScene(callback: SceneChangeCallback? = null) {
        switchScene(sceneClass, callback)
    }

    /**
     * Callback to perform logic during scene change.
     */
    interface SceneChangeCallback {

        /**
         * Logic to perform right before the scene is created.
         */
        fun beforeCreate()

        /**
         * Logic to perform right after the scene is created.
         */
        fun afterCreate()
    }

    companion object {

        /**
         * Game singleton.
         */
        lateinit var INSTANCE: Game

        /**
         * Version of the game in a string form.
         */
        lateinit var version: String

        /**
         * Version code of the game.
         */
        @JvmField
        var versionCode = 0

        /**
         * Reports exception.
         * @param th throwable to report
         */
        @JvmStatic
        fun reportException(tr: Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            tr.printStackTrace(pw)
            pw.flush()
            if (Gdx.app != null) {
                Gdx.app.error("GAME", sw.toString())
            } else {
                // Fallback if error happened in initialization.
                System.err.println(sw)
            }
        }

        /**
         * Runs specified code on the render thread.
         * Use this to manipulate the UI from actor context.
         */
        @JvmStatic
        fun runOnRenderThread(c: Callback) {
            Gdx.app.postRunnable { c.call() }
        }
    }
}