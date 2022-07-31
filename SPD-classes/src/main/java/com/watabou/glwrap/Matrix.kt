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

import com.watabou.utils.D2R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix {

    var values: FloatArray = identity.clone()

    fun copy(src: Matrix) {
        values = src.values.clone()
    }

    fun setIdentity() {
        values = identity.clone()
    }

    fun setValue(index: Int, value: Float) { // TODO: remove this after moving use cases to kotlin
        values[index] = value
    }

    fun rotate(angle: Float) {
        val sin = sin(angle * D2R)
        val cos = cos(angle * D2R)
        val m0 = values[0]
        val m1 = values[1]
        val m4 = values[4]
        val m5 = values[5]
        values[0] = m0 * cos + m4 * sin
        values[1] = m1 * cos + m5 * sin
        values[4] = -m0 * sin + m4 * cos
        values[5] = -m1 * sin + m5 * cos
    }

    fun skewX(angle: Float) {
        val tan = tan(angle * D2R)
        values[4] += -values[0] * tan
        values[5] += -values[1] * tan
    }

    fun scale(x: Float, y: Float) {
        values[0] *= x
        values[1] *= x
        values[2] *= x
        values[3] *= x
        values[4] *= y
        values[5] *= y
        values[6] *= y
        values[7] *= y
    }

    fun translate(x: Float, y: Float) {
        values[12] += values[0] * x + values[4] * y
        values[13] += values[1] * x + values[5] * y
    }

    companion object {
        private val identity = floatArrayOf(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }
}