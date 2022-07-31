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

fun interpolate(A: Int, B: Int, p: Float): Int {
    if (p <= 0) {
        return A
    } else if (p >= 1) {
        return B
    }
    val ra = A shr 16
    val ga = A shr 8 and 0xFF
    val ba = A and 0xFF
    val rb = B shr 16
    val gb = B shr 8 and 0xFF
    val bb = B and 0xFF
    val p1 = 1 - p
    val r = (p1 * ra + p * rb).toInt()
    val g = (p1 * ga + p * gb).toInt()
    val b = (p1 * ba + p * bb).toInt()
    return (r shl 16) + (g shl 8) + b
}

fun interpolate(p: Float, vararg colors: Int): Int {
    if (p <= 0) {
        return colors[0]
    } else if (p >= 1) {
        return colors[colors.size - 1]
    }
    val segment = ((colors.size - 1) * p).toInt()
    return interpolate(colors[segment], colors[segment + 1], p * (colors.size - 1) % 1)
}

fun random(a: Int, b: Int): Int {
    return interpolate(a, b, Random.Float())
}
