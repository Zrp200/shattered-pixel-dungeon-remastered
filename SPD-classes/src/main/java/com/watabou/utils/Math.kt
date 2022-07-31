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
package com.watabou.utils

import kotlin.math.pow
import kotlin.math.sign

const val PI = 3.1415927f
const val HALF_PI = PI / 2
const val PI2 = PI * 2
const val D2R = PI / 180
const val R2D = 180 / PI

fun sin(a: Float) = kotlin.math.sin(a)

fun cos(a: Float) = kotlin.math.cos(a)

fun tan(a: Float) = kotlin.math.tan(a)

fun atan(a: Float) = kotlin.math.atan(a)

fun atan2(a: Float, y: Float) = kotlin.math.atan2(a, y)

fun ceil(a: Float) = kotlin.math.ceil(a).toInt()

fun floor(a: Float) = kotlin.math.floor(a).toInt()

fun pow(a: Float, b: Float) = a.pow(b)

fun sqrt(x: Float) = kotlin.math.sqrt(x)

fun signum(a: Float) = sign(a).toInt()

fun clamp(min: Int, value: Int, max: Int): Int { // TODO: replace use cases with Kotlin clamp
    return if (value < min) {
        min
    } else if (value > max) {
        max
    } else {
        value
    }
}

fun clamp(min: Float, value: Float, max: Float): Float { // TODO: replace use cases with Kotlin clamp
    return if (value < min) {
        min
    } else if (value > max) {
        max
    } else {
        value
    }
}
