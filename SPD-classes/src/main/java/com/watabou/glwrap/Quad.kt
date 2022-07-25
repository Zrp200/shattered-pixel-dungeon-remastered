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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * 0-1
 *
 * | \ |
 *
 * 3-2
 */
private val VALUES = arrayOf(0, 1, 2, 0, 2, 3)
const val VALUES_SIZE = 6
private const val VERTEX_COUNT = 4
private const val INDEX_SIZE = Short.MAX_VALUE
private const val MATRIX_SIZE = 16

private val indices: ShortBuffer by lazy {
    ByteBuffer
        .allocateDirect(INDEX_SIZE * VALUES_SIZE * Short.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asShortBuffer()
        .put((0 until INDEX_SIZE).map { ofs ->
            VALUES.map { value ->
                (ofs * VERTEX_COUNT + value).toShort()
            }
        }.flatten().toShortArray())
        .position(0)
}

private val bufferIndex: Int by lazy {
    Gdx.gl.glGenBuffer()
}

/**
 * Creates a buffer for one image.
 *
 * @return [FloatBuffer] for one image
 */
fun create(): FloatBuffer = createSet(1)

/**
 * Creates a buffer for several images.
 *
 * @return [FloatBuffer] for several images
 */
fun createSet(size: Int): FloatBuffer = ByteBuffer
    .allocateDirect(size * MATRIX_SIZE * Float.SIZE_BYTES)
    .order(ByteOrder.nativeOrder())
    .asFloatBuffer()

/**
 * Sets up 32k quads for drawing.
 */
fun setupIndices() {
    bindIndices()
    Gdx.gl.glBufferData(
        GL20.GL_ELEMENT_ARRAY_BUFFER,
        INDEX_SIZE * VALUES_SIZE * Short.SIZE_BYTES,
        indices,
        GL20.GL_STATIC_DRAW
    )
}

fun bindIndices() = Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferIndex)

fun releaseIndices() = Gdx.gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0)

/**
 * Creates an array for the float buffer.
 *
 * @return created matrix
 */
fun fill(
    x1: Float, x2: Float, y1: Float, y2: Float,
    u1: Float, u2: Float, v1: Float, v2: Float
) = floatArrayOf(x1, y1, u1, v1, x2, y1, u2, v1, x2, y2, u2, v2, x1, y2, u1, v2)
