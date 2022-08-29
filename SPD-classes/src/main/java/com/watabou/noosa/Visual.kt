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

import com.watabou.glwrap.Matrix
import com.watabou.utils.Point
import com.watabou.utils.PointF

open class Visual(
    @JvmField var x: Float = 0f,
    @JvmField var y: Float = 0f,
    @JvmField var width: Float = 0f,
    @JvmField var height: Float = 0f
) : Gizmo() {
    @JvmField
    var scale = PointF(1f, 1f)

    init {
        resetColor()
    }

    @JvmField
    var origin = PointF()

    @JvmField
    var matrix = Matrix()

    @JvmField
    var rm = 0f

    @JvmField
    var gm = 0f

    @JvmField
    var bm = 0f

    @JvmField
    var am = 0f

    @JvmField
    var ra = 0f

    @JvmField
    var ga = 0f

    @JvmField
    var ba = 0f

    @JvmField
    var aa = 0f

    @JvmField
    var speed = PointF()

    @JvmField
    var acc = PointF()

    @JvmField
    var angle = 0f

    @JvmField
    var angularSpeed = 0f

    private var lastX = 0f
    private var lastY = 0f
    private var lastW = 0f
    private var lastH = 0f
    private var lastA = 0f
    private val lastScale = PointF()
    private val lastOrigin = PointF()

    override fun update() = updateMotion()

    //TODO caching the last value of all these variables does improve performance a bunch
    // by letting us skip many calls to updateMatrix, but it is quite messy. It would be better to
    // control their editing and have a single boolean to tell if the matrix needs updating.
    override fun draw() {
        if (lastX != x || lastY != y || lastW != width || lastH != height || lastA != angle || lastScale.x != scale.x || lastScale.y != scale.y || lastOrigin.x != origin.x || lastOrigin.y != origin.y) {
            lastX = x
            lastY = y
            lastW = width
            lastH = height
            lastA = angle
            lastScale.x = scale.x
            lastScale.y = scale.y
            lastOrigin.x = origin.x
            lastOrigin.y = origin.y
            updateMatrix()
        }
    }

    protected open fun updateMatrix() = with(matrix) {
        setIdentity()
        translate(x, y)
        if (origin.x != 0f || origin.y != 0f) translate(origin.x, origin.y)
        if (angle != 0f) {
            rotate(angle)
        }
        if (scale.x != 1f || scale.y != 1f) {
            scale(scale.x, scale.y)
        }
        if (origin.x != 0f || origin.y != 0f) translate(-origin.x, -origin.y)
    }

    var point: PointF
        @JvmName("point")
        get() = PointF(x, y)
        // this is redundant with the point method, but we need it anyway because we can't return anything with setter.
        @JvmSynthetic
        set(p) {
            point(p)
        }

    open fun point(x: Float, y: Float) = also {
        it.x = x
        it.y = y
    }

    fun point(p: PointF) = point(p.x, p.y)
    fun point(p: Point) = point(p.x.toFloat(), p.y.toFloat())

    var center: PointF
        @JvmName("center")
        get() = PointF(x + width() / 2, y + height() / 2)
        @JvmSynthetic // see [center]
        set(p) {
            x = p.x - width() / 2
            y = p.y - height() / 2
        }

    fun center(p: PointF) = p.also { center = it }

    //returns the point needed to center the argument visual on this visual
    fun center(v: Visual) = PointF(
        x + (width() - v.width()) / 2f,
        y + (height() - v.height()) / 2f
    )

    fun originToCenter() {
        origin[width / 2] = height / 2
    }

    open fun width(): Float = width * scale.x
    open fun height(): Float = height * scale.y

    protected fun updateMotion() {
        if (acc.x != 0f) speed.x += acc.x * Game.INSTANCE.elapsed
        if (speed.x != 0f) x += speed.x * Game.INSTANCE.elapsed
        if (acc.y != 0f) speed.y += acc.y * Game.INSTANCE.elapsed
        if (speed.y != 0f) y += speed.y * Game.INSTANCE.elapsed
        if (angularSpeed != 0f) angle += angularSpeed * Game.INSTANCE.elapsed
    }

    var alpha: Float
        @JvmName("alpha")
        get() = am + aa
        @JvmName("alpha")
        set(value) {
            am = value
            aa = 0f
        }

    fun invert() {
        bm = -1f
        gm = bm
        rm = gm
        ba = +1f
        ga = ba
        ra = ga
    }

    fun lightness(value: Float) {
        if (value < 0.5f) {
            bm = value * 2f
            gm = bm
            rm = gm
            ba = 0f
            ga = ba
            ra = ga
        } else {
            bm = 2f - value * 2f
            gm = bm
            rm = gm
            ba = value * 2f - 1f
            ga = ba
            ra = ga
        }
    }

    fun brightness(value: Float) {
        bm = value
        gm = bm
        rm = gm
    }

    fun tint(r: Float, g: Float, b: Float, strength: Float) {
        bm = 1f - strength
        gm = bm
        rm = gm
        ra = r * strength
        ga = g * strength
        ba = b * strength
    }

    @JvmSynthetic
    fun tint(color: Color, strength: Float) = tint(color.r, color.g, color.b, strength)
    fun tint(color: Int, strength: Float) = tint(Color(color), strength)

    //color must include an alpha component (e.g. 0x80FF0000 for red at 0.5 strength)
    @JvmSynthetic
    fun tint(color: Color) =
        tint(color.hex and 0xFFFFFF, ((color.hex shr 24 and 0xFF) / 0xf).toFloat())

    fun tint(color: Int) = tint(Color(color))

    fun color(r: Float, g: Float, b: Float) {
        bm = 0f
        gm = bm
        rm = gm
        ra = r
        ga = g
        ba = b
    }

    @JvmSynthetic
    fun color(color: Color) = color(color.r, color.g, color.b)
    fun color(color: Int) = color(Color(color))

    fun hardlight(r: Float, g: Float, b: Float) {
        0f.also {
            ba = it
            ga = it
            ra = it
        }
        rm = r
        gm = g
        bm = b
    }

    @JvmSynthetic
    fun hardlight(color: Color) = hardlight(color.r, color.g, color.b)
    fun hardlight(color: Int) = hardlight(Color(color))

    open fun resetColor() {
        alpha = 1f.also {
            bm = it
            gm = it
            rm = it
        }
        aa.also {
            ba = it
            ga = it
            ra = it
        }
    }

    open fun overlapsPoint(x: Float, y: Float) =
        x >= this.x && x < this.x + width * scale.x
                && y >= this.y && y < this.y + height * scale.y

    open fun overlapsScreenPoint(x: Int, y: Int) = camera
        ?.takeUnless { !it.hitTest(x.toFloat(), y.toFloat()) }
        ?.screenToCamera(x, y)?.let { overlapsPoint(it.x, it.y) } == true

    // true if its bounding box intersects its camera's bounds
    override var visible: Boolean
        get() {
            val c = camera
            if (c == null || !super.visible) return false

            //FIXME, the below calculations ignore angle, so assume visible if angle != 0
            if (angle != 0f) return super.visible

            //x coord
            if (x > c.scroll.x + c.width) return false
            else if (x < c.scroll.x && x + width() < c.scroll.x) return false

            //y coord
            if (y > c.scroll.y + c.height) return false
            else if (y < c.scroll.y && y + height() < c.scroll.y) return false

            return super.visible
        }
        set(visible) {
            super.visible = visible
        }
}

operator fun Visual.component1() = x
operator fun Visual.component2() = y
operator fun Visual.component3() = width()
operator fun Visual.component4() = height()

@JvmInline
// note, all instances of methods that use this need to be marked as @JvmSynthetic if they're already being used from Java.
// Java 1.8 can't actually make use of this
value class Color(val hex: Int) {
    inline val r get() = (hex shr 16 and 0xFF) / 255f
    inline val g get() = (hex shr 8 and 0xFF) / 255f
    inline val b get() = (hex shr 0 and 0xFF) / 255f
    operator fun component1() = r
    operator fun component2() = g
    operator fun component3() = b
}