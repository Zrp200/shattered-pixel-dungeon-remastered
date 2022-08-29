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

import com.watabou.glwrap.Texture
import com.watabou.glwrap.VertexDataset
import com.watabou.glwrap.create
import com.watabou.utils.RectF
import java.nio.Buffer
import java.nio.FloatBuffer

open class Image() : Visual() {
    @JvmField
    var texture: Texture? = null

    // this can't be made directly open until all usages of this and are ported.
    var frame: RectF
        @JvmName("frame")
        get() = RectF(frame_field)
        @JvmSynthetic // see #frame(RectF)
        set(frame) = frame(frame)

    @Suppress("PrivatePropertyName")
    // backing property for frame
    private lateinit var frame_field: RectF

    open fun frame(frame: RectF) {
        frame_field = frame
        width = frame.width() * texture!!.width
        height = frame.height() * texture!!.height
        updateFrame()
        updateVertices()
    }

    fun frame(left: Int, top: Int, width: Int, height: Int) {
        frame = texture!!.uvRect(
            left.toFloat(),
            top.toFloat(),
            (left + width).toFloat(),
            (top + height).toFloat()
        )
    }

    @JvmField
    var flipHorizontal = false
    var flipVertical = false

    @JvmField
    protected var vertices: FloatArray = FloatArray(16)

    @JvmField
    protected var vertexBuffer: FloatBuffer = create()

    @JvmField
    protected var vertexDataset: VertexDataset? = null

    @JvmField
    protected var dirty = false

    constructor(src: Image) : this() {
        copy(src)
    }

    constructor(tx: Any) : this() {
        texture(tx)
    }

    constructor(tx: Any, left: Int, top: Int, width: Int, height: Int) : this(tx) {
        @Suppress("LeakingThis") // none of the usages call any methods
        frame(
            texture!!.uvRect(
                left.toFloat(),
                top.toFloat(),
                (left + width).toFloat(),
                (top + height).toFloat()
            )
        )
    }

    fun texture(tx: Any) {
        texture = if (tx is Texture) tx else Texture[tx]
        frame(RectF(0f, 0f, 1f, 1f))
    }

    fun copy(other: Image) {
        texture = other.texture
        frame = RectF(other.frame)
        width = other.width
        height = other.height
        scale = other.scale
        updateFrame()
        updateVertices()
    }

    protected open fun updateFrame() = with(frame) {
        vertices.let {
            if (flipHorizontal) {
                it[2] = right
                it[6] = left
            } else {
                it[2] = left
                it[6] = right
            }
            it[10] = it[6]
            it[14] = it[2]
            if (flipVertical) {
                it[3] = bottom
                it[11] = top
            } else {
                it[3] = top
                it[11] = bottom
            }
            it[7] = it[3]
            it[15] = it[11]
        }
        dirty = true
    }

    protected fun updateVertices() {
        operator fun FloatArray.set(vararg indices: Int, value: Float) =
            indices.forEach { this[it] = value }
        vertices.let {
            it[0, 1, 5, 12] = 0f
            it[4, 8] = width
            it[9, 13] = height
        }
        dirty = true
    }

    override fun draw() {
        if (!dirty && vertexDataset == null) return
        val texture = texture ?: return
        super.draw()
        if (dirty) {
            (vertexBuffer as Buffer).position(0)
            vertexBuffer.put(vertices)
            if (vertexDataset?.run { markForUpdate(vertexBuffer) } == null)
                vertexDataset = VertexDataset(vertexBuffer)
            dirty = false
        }
        Script.get().run {
            texture.bind()
            setCamera(camera)
            uModel.set(matrix)
            lighting(
                rm, gm, bm, am,
                ra, ga, ba, aa
            )
            drawQuad(vertexDataset!!)
        }

    }

    override fun destroy() {
        super.destroy()
        vertexDataset?.delete()
    }
}