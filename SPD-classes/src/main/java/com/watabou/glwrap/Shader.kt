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
import com.badlogic.gdx.utils.BufferUtils

class Shader private constructor(type: Int) {

    private val handle = Gdx.gl.glCreateShader(type)

    private fun source(src: String) {
        Gdx.gl.glShaderSource(handle, src)
    }

    private fun compile() {
        Gdx.gl.glCompileShader(handle)
        val status = BufferUtils.newIntBuffer(1)
        Gdx.gl.glGetShaderiv(handle, GL20.GL_COMPILE_STATUS, status)
        if (status.get() == GL20.GL_FALSE) {
            throw Error(Gdx.gl.glGetShaderInfoLog(handle))
        }
    }

    companion object {
        const val VERTEX = GL20.GL_VERTEX_SHADER
        const val FRAGMENT = GL20.GL_FRAGMENT_SHADER

        fun get(type: Int, fileName: String): Int = Shader(type).also {
            it.source(Gdx.files.internal("shaders/$fileName").readString())
            it.compile()
        }.handle
    }
}