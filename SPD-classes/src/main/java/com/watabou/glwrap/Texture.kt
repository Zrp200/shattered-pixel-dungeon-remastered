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
package com.watabou.glwrap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.watabou.utils.RectF

open class Texture(@JvmField var bitmap: Pixmap, private var filtering: Int, private var wrapping: Int) {

    private var id = -1

    val generated
        get() = id != -1

    constructor(bitmap: Pixmap) : this(bitmap, NEAREST, CLAMP)

    private fun generate() {
        id = Gdx.gl.glGenTexture()
        setImage(bitmap)
        filter(filtering)
        wrap(wrapping)
    }

    fun bind() {
        if (id == -1) {
            generate()
        }
        if (id != boundId) {
            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, id)
            boundId = id
        }
    }

    fun filter(mode: Int) {
        filtering = mode
        if (!generated) return
        bind()
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, mode.toFloat())
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, mode.toFloat())
    }

    fun wrap(mode: Int) {
        wrapping = mode
        if (!generated) return
        bind()
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, mode.toFloat())
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, mode.toFloat())
    }

    private fun delete() {
        Gdx.gl.glDeleteTexture(id)
        if (boundId == id) boundId = 0
        bitmap.dispose()
    }

    fun setImage(pixmap: Pixmap) {
        bind()
        Gdx.gl.glTexImage2D(
            GL20.GL_TEXTURE_2D,
            0,
            pixmap.glInternalFormat,
            pixmap.width,
            pixmap.height,
            0,
            pixmap.glFormat,
            pixmap.glType,
            pixmap.pixels
        )
        bitmap = pixmap
    }

    private fun reload() {
        id = -1
        generate()
    }

    fun uvRect(left: Float, top: Float, right: Float, bottom: Float) = RectF(
        left / bitmap.width,
        top / bitmap.height,
        right / bitmap.width,
        bottom / bitmap.height
    )

    fun uvRectBySize(left: Float, top: Float, width: Float, height: Float) = uvRect(left, top, left + width, top + height)

    companion object {
        const val NEAREST = GL20.GL_NEAREST
        const val LINEAR = GL20.GL_LINEAR
        const val REPEAT = GL20.GL_REPEAT
        const val MIRROR = GL20.GL_MIRRORED_REPEAT
        const val CLAMP = GL20.GL_CLAMP_TO_EDGE

        private var boundId = 0

        fun reset() {
            boundId = 0
        }

        private val all = HashMap<Any, Texture>()

        @Synchronized
        fun createSolid(color: Int) = all["1x1:$color"] ?: Texture(Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(color shl 8 or (color ushr 24))
            fill()
        }).also { all["1x1:$color"] = it }

        @Synchronized
        fun createGradient(vararg colors: Int) = all[colors.toString()] ?: Texture(Pixmap(colors.size, 1, Pixmap.Format.RGBA8888).also { map ->
            colors.indices.forEach { map.drawPixel(it, 0, colors[it] shl 8 or (colors[it] ushr 24)) }
        }).apply {
            filter(LINEAR)
            wrap(CLAMP)
            all[colors.toString()] = this
        }

        //texture is created at given size, but size is not enforced if it already exists
        //texture contents are also not enforced, make sure you know the texture's state!
        @Synchronized
        fun create(key: Any, width: Int, height: Int) = all[key] ?: Texture(Pixmap(width, height, Pixmap.Format.RGBA8888)).apply {
            filter(LINEAR)
            wrap(CLAMP)
            all[key] = this
        }

        @Synchronized
        fun remove(key: Any) {
            all[key]?.let {
                all.remove(it)
                it.delete()
            }
        }

        @Synchronized
        operator fun get(key: Any) = all[key] ?: run {
            if (key is Texture) key
            else Texture(
                when (key) {
                    is Pixmap -> key
                    else -> Pixmap(Gdx.files.internal(key.toString()))
                }
            ).also { all[key] = it }
        }

        @Synchronized
        fun clear() {
            all.values.forEach { it.delete() }
            all.clear()
        }

        @Synchronized
        fun reload() {
            all.values.forEach { it.reload() }
        }

        @Synchronized
        operator fun contains(key: Any) = all.containsKey(key)
    }
}