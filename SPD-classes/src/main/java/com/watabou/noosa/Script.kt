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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.BufferUtils
import com.watabou.glwrap.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.roundToInt

open class Script {

    private val handle: Int = Gdx.gl.glCreateProgram()

    var uModel: Uniform
    private var uCamera: Uniform
    private var uTex: Uniform
    private var uColorM: Uniform
    private var uColorA: Uniform
    private val aXY: Attribute
    private val aUV: Attribute

    private var lastCamera: Camera? = null

    init {
        initShaders()
        link()
        uCamera = uniform("uCamera")
        uModel = uniform("uModel")
        uTex = uniform("uTex")
        uColorM = uniform("uColorM")
        uColorA = uniform("uColorA")
        aXY = attribute("aXYZW")
        aUV = attribute("aUV")

        setupIndices()
    }

    fun drawElements(vertexBuffer: FloatBuffer, indices: ShortBuffer, size: Int) {
        vertexBuffer.position(0)
        aXY.describeData(2, 4, vertexBuffer)
        vertexBuffer.position(2)
        aUV.describeData(2, 4, vertexBuffer)

        releaseIndices()
        Gdx.gl20.glDrawElements(
            GL20.GL_TRIANGLES,
            size,
            GL20.GL_UNSIGNED_SHORT,
            indices
        )
        bindIndices()
    }

    fun drawQuad(vertexDataset: VertexDataset) {
        vertexDataset.updateGlData()
        vertexDataset.bind()
        aXY.describeData(2, 4, 0)
        aUV.describeData(2, 4, 2)
        vertexDataset.release()

        Gdx.gl20.glDrawElements(
            GL20.GL_TRIANGLES,
            VALUES_SIZE,
            GL20.GL_UNSIGNED_SHORT,
            0
        )
    }

    @JvmOverloads // TODO: remove this after moving use cases to kotlin
    fun drawQuadSet(vertexBuffer: FloatBuffer, size: Int = 1) {
        if (size == 0) return

        vertexBuffer.position(0)
        aXY.describeData(2, 4, vertexBuffer)
        vertexBuffer.position(2)
        aUV.describeData(2, 4, vertexBuffer)

        Gdx.gl20.glDrawElements(
            GL20.GL_TRIANGLES,
            VALUES_SIZE * size,
            GL20.GL_UNSIGNED_SHORT,
            0
        )
    }

    fun drawQuadSet(vertexDataset: VertexDataset, length: Int, offset: Int) {
        if (length == 0) return

        vertexDataset.updateGlData()
        vertexDataset.bind()
        aXY.describeData(2, 4, 0)
        aUV.describeData(2, 4, 2)
        vertexDataset.release()

        Gdx.gl20.glDrawElements(
            GL20.GL_TRIANGLES,
            VALUES_SIZE * length,
            GL20.GL_UNSIGNED_SHORT,
            VALUES_SIZE * java.lang.Short.SIZE / 8 * offset
        )
    }

    fun lighting(rm: Float, gm: Float, bm: Float, am: Float, ra: Float, ga: Float, ba: Float, aa: Float) {
        uColorM.set(rm, gm, bm, am)
        uColorA.set(ra, ga, ba, aa)
    }

    fun resetCamera() {
        lastCamera = null
    }

    fun setCamera(camera: Camera?) {
        val cam = camera ?: Camera.main
        if (cam !== this.lastCamera && cam!!.matrix != null) { // TODO: rewrite after moving camera logic to kotlin
            lastCamera = cam
            uCamera.set(cam.matrix)

            if (cam.fullScreen) Gdx.gl20.glDisable(GL20.GL_SCISSOR_TEST)
            else {
                Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST)

                // This fixes pixel scaling issues on some hidpi displays (mainly on macOS)
                // because for some reason all other openGL operations work on virtual pixels
                // but glScissor operations work on real pixels
                val xScale = Gdx.graphics.backBufferWidth / Game.width.toFloat()
                val yScale = (Gdx.graphics.backBufferHeight - Game.bottomInset) / Game.height.toFloat()
                Gdx.gl20.glScissor(
                    (cam.x * xScale).roundToInt(),
                    ((Game.height - cam.screenHeight - cam.y) * yScale).roundToInt() + Game.bottomInset,
                    (cam.screenWidth * xScale).roundToInt(),
                    (cam.screenHeight * yScale).roundToInt()
                )
            }
        }
    }

    private fun initShaders() {
        Gdx.gl.glAttachShader(handle, Shader.get(Shader.VERTEX, "vertex.shader"))
        Gdx.gl.glAttachShader(handle, Shader.get(Shader.FRAGMENT, "fragment.shader"))
    }

    private fun link() {
        Gdx.gl.glLinkProgram(handle)

        val status = BufferUtils.newIntBuffer(1)
        Gdx.gl.glGetProgramiv(handle, GL20.GL_LINK_STATUS, status)
        if (status.get() == GL20.GL_FALSE) {
            Game.reportException(RuntimeException(Gdx.gl.glGetProgramInfoLog(handle)))
        }
    }

    private fun use() {
        Gdx.gl.glUseProgram(handle)
        aXY.enable()
        aUV.enable()
    }

    private fun delete() {
        Gdx.gl.glDeleteProgram(handle)
    }

    private fun attribute(name: String): Attribute {
        return Attribute(Gdx.gl.glGetAttribLocation(handle, name))
    }

    private fun uniform(name: String): Uniform {
        return Uniform(Gdx.gl.glGetUniformLocation(handle, name))
    }

    companion object {

        private var instance: Script? = null

        @JvmStatic // TODO: remove this after moving use cases to kotlin
        fun get(): Script {
            return instance ?: Script().also {
                it.use()
                instance = it
            }
        }

        @JvmStatic // TODO: remove this after moving use cases to kotlin
        fun unuse() {
            instance = null
        }

        @JvmStatic // TODO: remove this after moving use cases to kotlin
        fun reset() {
            instance?.delete()
            unuse()
        }
    }
}