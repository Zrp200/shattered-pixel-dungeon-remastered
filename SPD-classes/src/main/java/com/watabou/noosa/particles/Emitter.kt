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
package com.watabou.noosa.particles

import com.watabou.glwrap.inLightMode
import com.watabou.noosa.Game
import com.watabou.noosa.Group
import com.watabou.noosa.Visual
import com.watabou.utils.PointF
import com.watabou.utils.Random

open class Emitter : Group() {
    @JvmField
    protected var lightMode = false

    @JvmField
    var x = 0f

    @JvmField
    var y = 0f

    @JvmField
    var width = 0f

    @JvmField
    var height = 0f

    @set:JvmName("pos")
    var target: Visual? = null

    @JvmField
    var fillTarget = true

    protected var interval = 0f
    protected var quantity = 0

    @JvmField
    var on = false

    private var started = false

    @JvmField
    var autoKill = true

    @JvmField
    protected var count = 0

    @JvmField
    protected var time = 0f

    protected lateinit var factory: Factory


    fun pos(p: PointF) {
        pos(p.x, p.y, 0f, 0f)
    }

    @JvmOverloads
    fun pos(x: Float, y: Float, width: Float = 0f, height: Float = 0f) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        target = null
    }

    fun pos(target: Visual?, x: Float, y: Float, width: Float, height: Float) {
        pos(x, y, width, height)
        this.target = target
    }

    fun burst(factory: Factory, quantity: Int) {
        start(factory, 0f, quantity)
    }

    fun pour(factory: Factory, interval: Float) {
        start(factory, interval, 0)
    }

    fun start(factory: Factory, interval: Float, quantity: Int) {
        started = true
        this.factory = factory
        lightMode = factory.lightMode()
        this.interval = interval
        this.quantity = quantity
        count = 0
        time = Random.Float(interval)
        on = true
    }

    protected open val isFrozen: Boolean
        get() = Game.INSTANCE.timeTotal > 1 && freezeEmitters

    override fun update() {
        if (isFrozen) return

        if (on) {
            time += Game.INSTANCE.elapsed
            while (time > interval) {
                time -= interval
                emit(count++)
                if (quantity in 1..count) {
                    on = false
                    break
                }
            }
        } else if (started && autoKill && children.isEmpty()) {
            kill()
        }
        super.update()
    }

    override fun revive() {
        started = false
        super.revive()
    }

    protected open fun emit(index: Int) = with(factory) {
        target?.let {
            val (width, height) = if (fillTarget) it.width to it.height else width to height
            emit(
                index,
                it.x + Random.Float(width),
                it.y + Random.Float(height)
            )
        } ?: emit(
            index,
            x + Random.Float(width),
            y + Random.Float(height)
        )
    }

    override fun draw() {
        if (lightMode) {
            inLightMode { super.draw() }
        } else {
            super.draw()
        }
    }

    fun interface Factory {
        fun Emitter.emit(index: Int, x: Float, y: Float)
        fun lightMode() = false
    }

    companion object {
        @JvmField
        var freezeEmitters = false
    }
}