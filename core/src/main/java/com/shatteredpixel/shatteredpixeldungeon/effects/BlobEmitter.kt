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
package com.shatteredpixel.shatteredpixeldungeon.effects

import com.shatteredpixel.shatteredpixeldungeon.Dungeon
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap
import com.watabou.noosa.particles.Emitter
import com.watabou.utils.Random
import com.watabou.utils.RectF

class BlobEmitter(private val blob: Blob) : Emitter() {
    var bound = RectF(0f, 0f, 1f, 1f)
    override fun emit(index: Int) {
        if (blob.volume <= 0) {
            return
        }
        if (blob.area.isEmpty) blob.setupArea()
        val map = blob.cur
        val size = DungeonTilemap.SIZE.toFloat()
        var cell: Int
        for (i in blob.area.left until blob.area.right) {
            for (j in blob.area.top until blob.area.bottom) {
                cell = i + j * Dungeon.level.width()
                if (cell < Dungeon.level.heroFOV.size && (Dungeon.level.heroFOV[cell] || blob.alwaysVisible)
                    && map[cell] > 0
                ) {
                    val x = (i + Random.Float(bound.left, bound.right)) * size
                    val y = (j + Random.Float(bound.top, bound.bottom)) * size
                    with(factory) { emit(index, x, y) }
                }
            }
        }
    }

    init {
        blob.use(this)
    }
}