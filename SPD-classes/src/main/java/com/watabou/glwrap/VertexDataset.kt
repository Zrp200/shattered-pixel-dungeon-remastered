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
import java.nio.FloatBuffer

class VertexDataset(private var vertexBuffer: FloatBuffer) {

    private var id = Gdx.gl.glGenBuffer()
    private var updateStart = 0
    private var updateEnd = vertexBuffer.limit()
    private var update = true

    init {
        vertexDatasets.add(this)
    }

    @JvmOverloads // TODO: remove this after moving use cases to kotlin
    fun markForUpdate(vertexBuffer: FloatBuffer = this.vertexBuffer, start: Int = 0, end: Int = vertexBuffer.limit()) {
        this.vertexBuffer = vertexBuffer
        updateStart = start.coerceAtMost(updateStart)
        updateEnd = end.coerceAtLeast(updateEnd)
        update = true
    }

    fun updateGlData() {
        if (!update) return
        vertexBuffer.position(updateStart)

        bind()
        if (updateStart == 0 && updateEnd == vertexBuffer.limit())
            Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, vertexBuffer.limit() * Float.SIZE_BYTES, vertexBuffer, GL20.GL_DYNAMIC_DRAW)
        else
            Gdx.gl.glBufferSubData(GL20.GL_ARRAY_BUFFER, updateStart * Float.SIZE_BYTES, (updateEnd - updateStart) * Float.SIZE_BYTES, vertexBuffer)
        release()

        updateStart = 0
        updateEnd = vertexBuffer.limit()
        update = false
    }

    fun bind() {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, id)
    }

    fun release() {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0)
    }

    // TODO: implement this after moving use cases to kotlin
    fun with(block: () -> Any) {
        bind()
        try {
            run(block)
        } finally {
            release()
        }
    }

    fun delete() {
        synchronized(vertexDatasets) {
            Gdx.gl.glDeleteBuffer(id)
            vertexDatasets.remove(this)
        }
    }

    companion object {
        private val vertexDatasets = ArrayList<VertexDataset>()

        @JvmStatic // TODO: remove this after moving use cases to kotlin
        fun clear() {
            synchronized(vertexDatasets) {
                vertexDatasets.toTypedArray().forEach { it.delete() }
            }
        }

        @JvmStatic // TODO: remove this after moving use cases to kotlin
        fun reload() {
            synchronized(vertexDatasets) {
                vertexDatasets.forEach {
                    it.markForUpdate()
                    it.updateGlData()
                }
            }
        }
    }
}
