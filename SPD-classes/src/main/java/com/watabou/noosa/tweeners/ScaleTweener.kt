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
 * [Tweener] that scales a [visual][Visual] to a specified size.
 *
 * @param scale desired alpha value
 * @param interval lifespan of the tweener
 * @property visual visual to work with
 */
open class ScaleTweener(
    private var visual: Visual,
    scale: PointF,
    interval: Float
) : Tweener(interval) {

    var start: PointF = visual.scale
    var end: PointF = scale

    override fun updateValues(progress: Float) {
        visual.scale = PointF.inter(start, end, progress)
    }
}
