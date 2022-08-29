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

private var falseArray = booleanArrayOf()

/**
 * Sets every value in the [BooleanArray] to false.
 *
 * This is MUCH faster than making a new [boolean[]][BooleanArray] or using [Arrays.fill][java.util.Arrays.fill];
 */

fun BooleanArray.setFalse() {
    if (falseArray.size < size) falseArray = BooleanArray(size)
    falseArray.copyInto(this, endIndex = size)
}

/**
 * Applies [Boolean.and] for each index in the BooleanArray and [b].
 */
infix fun BooleanArray.and(b: BooleanArray) = and(b, null)

/**
 * [Records][BooleanArray.populate] the result of applying [Boolean.and] for each index in the BooleanArray and [b].
 * @param result The [BooleanArray] to overwrite.
 */
fun BooleanArray.and(b: BooleanArray, result: BooleanArray?) =
    result.populate(size) { this[it] && b[it] }

/**
 * Applies [Boolean.or] for each index in [BooleanArray] and [b].
 */
infix fun BooleanArray.or(b: BooleanArray) = or(b, result = null)

/**
 * [Records][BooleanArray.populate] the result of applying [Boolean.or] for each index in the BooleanArray and [b].
 * @param offset the index to start the operation at
 * @param length the amount of indices to apply the operation to
 * @param result The [BooleanArray] to overwrite. Must have a [length][BooleanArray.size] of at least [offset]+[length].
 */
@JvmOverloads
fun BooleanArray.or(
    b: BooleanArray,
    offset: Int = 0,
    length: Int = size - offset,
    result: BooleanArray?
) = result.populate(length, offset) { get(it) || b[it] }

/**
 * inverts all values in the [BooleanArray].
 */
operator fun BooleanArray.not() = not(null)

/**
 * inverts all values in the [BooleanArray] and [records][BooleanArray.populate] it into [result]
 */
fun BooleanArray.not(result: BooleanArray?) = result.populate(size) { !this[it] }

/**
 * Returns a [BooleanArray] representing the indices where [v1] appears in the [IntArray]
 */
infix fun IntArray.`is`(v1: Int) = `is`(null, v1)

/**
 * [Records][BooleanArray.populate] representing all indices where [v1] appears in the [IntArray]
 */
fun IntArray.`is`(result: BooleanArray?, v1: Int) = mapInto(result, v1::equals)

fun IntArray.isOneOf(result: BooleanArray?, vararg v: Int) = mapInto(result, v::contains)

fun IntArray.isNot(result: BooleanArray?, v1: Int) = mapInto(result) { it != v1 }

fun IntArray.isNotOneOf(result: BooleanArray?, vararg v: Int) = mapInto(result) { it !in v }

// functions for mapping used in the above.

/**
 * [IntArray.mapTo], but the result is a [BooleanArray] and the [existing array][BooleanArray] is overwritten by the results of [predicate] if it already exists.
 **/
private inline fun IntArray.mapInto(result: BooleanArray?, predicate: (value: Int) -> Boolean) =
    result.populate(size) { predicate(this[it]) }

/**
 * overwrites a section of the [BooleanArray] using [predicate].
 * If there is no [boolean array][BooleanArray], one is created.
 *
 * @param offset index to start the operation at (leaving prior ones untouched).
 * @param length amount of indices to operate on
 */
private inline fun BooleanArray?.populate(
    length: Int,
    offset: Int = 0,
    predicate: (index: Int) -> Boolean,
) = (this ?: BooleanArray(length + offset)).apply {
    for (i in offset until offset + length) set(i, predicate(i))
}