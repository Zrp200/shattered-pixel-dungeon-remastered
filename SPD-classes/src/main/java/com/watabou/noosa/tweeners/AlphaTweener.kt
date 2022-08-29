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
package com.watabou.noosa.tweeners

import com.watabou.noosa.Visual

/**
 * [Tweener] that changes alpha value of a [visual][Visual].
 *
 * @param alpha desired alpha value
 * @param interval lifespan of the tweener
 * @property visual visual to work with
 */
open class AlphaTweener(
    private val visual: Visual,
    alpha: Float,
    interval: Float
) : Tweener(interval) {

    var start: Float = visual.alpha
    var delta: Float = alpha - start

    override fun updateValues(progress: Float) {
        visual.alpha = start + delta * progress
    }
}
