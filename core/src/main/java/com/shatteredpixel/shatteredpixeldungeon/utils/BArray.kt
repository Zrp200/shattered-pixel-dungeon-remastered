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
@file:JvmName("BArray")

package com.shatteredpixel.shatteredpixeldungeon.utils

private lateinit var falseArray: BooleanArray

//This is MUCH faster than making a new boolean[] or using Arrays.fill;
fun BooleanArray.setFalse() {
    if (!::falseArray.isInitialized || falseArray.size < size) falseArray = BooleanArray(size)
    falseArray.copyInto(this, endIndex = size)
}

infix fun BooleanArray.and(b: BooleanArray) = and(b, null)
fun BooleanArray.and(b: BooleanArray, result: BooleanArray?): BooleanArray {
    result ?: return BooleanArray(size) { get(it) && b[it] }
    repeat(size) { result[it] = get(it) && b[it] }
    return result
}

infix fun BooleanArray.or(b: BooleanArray) = or(b, result = null)

@JvmOverloads
fun BooleanArray.or(
    b: BooleanArray,
    offset: Int = 0,
    length: Int = size - offset,
    result: BooleanArray?
) = (result ?: BooleanArray(offset + length))
    .also { for (i in offset until offset + length) it[i] = get(i) || b[i] }

operator fun BooleanArray.not() = not(null)
fun BooleanArray.not(result: BooleanArray?) = mapInto(result, Boolean::not)

@JvmName("is")
infix fun IntArray.where(v1: Int) = where(null, v1)

@JvmName("is")
fun IntArray.where(result: BooleanArray?, v1: Int) = mapInto(result, v1::equals)

fun IntArray.isOneOf(result: BooleanArray?, vararg v: Int) = mapInto(result, v::contains)

fun IntArray.isNot(result: BooleanArray?, v1: Int) = mapInto(result) { it != v1 }

fun IntArray.isNotOneOf(result: BooleanArray?, vararg v: Int) = mapInto(result) { it !in v }

// functions for mapping because I don't want to do an extra conversion from list

private inline fun IntArray.mapInto(
    result: BooleanArray?,
    predicate: (Int) -> Boolean
): BooleanArray {
    result ?: return BooleanArray(size) { predicate(get(it)) }
    repeat(size) { result[it] = predicate(get(it)) }
    return result
}

private inline fun BooleanArray.mapInto(
    result: BooleanArray?,
    predicate: (Boolean) -> Boolean
): BooleanArray {
    result ?: return BooleanArray(size) { predicate(get(it)) }
    repeat(size) { result[it] = predicate(get(it)) }
    return result
}