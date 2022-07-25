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
package com.watabou.gltextures

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap

open class Texture {
    @JvmField
	var id = -1
    protected open fun generate() {
        id = Gdx.gl.glGenTexture()
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

    open fun filter(minMode: Int, maxMode: Int) {
        bind()
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, minMode.toFloat())
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, maxMode.toFloat())
    }

    open fun wrap(s: Int, t: Int) {
        bind()
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, s.toFloat())
        Gdx.gl.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, t.toFloat())
    }

    open fun delete() {
        Gdx.gl.glDeleteTexture(id)
        if (boundId == id) boundId = 0
    }

    open fun bitmap(pixmap: Pixmap) {
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
    }

    companion object {
        const val NEAREST = GL20.GL_NEAREST
        const val LINEAR = GL20.GL_LINEAR
        const val REPEAT = GL20.GL_REPEAT
        const val MIRROR = GL20.GL_MIRRORED_REPEAT
        const val CLAMP = GL20.GL_CLAMP_TO_EDGE

        private var boundId = 0

        @JvmStatic
		fun clear() {
            boundId = 0
        }
    }
}