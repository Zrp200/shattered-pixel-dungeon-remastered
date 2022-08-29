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
import com.watabou.utils.PointF

/**
 * [Tweener] that moves a [visual][Visual] from its current position to a specified one.
 *
 * @param pos desired position of the visual
 * @param interval lifespan of the tweener
 * @property visual visual to work with
 */
open class PosTweener(
    private var visual: Visual,
    pos: PointF,
    interval: Float
) : Tweener(interval) {

    var start: PointF = visual.point
    var end: PointF = pos

    override fun updateValues(progress: Float) {
        visual.point(PointF.inter(start, end, progress))
    }
}
