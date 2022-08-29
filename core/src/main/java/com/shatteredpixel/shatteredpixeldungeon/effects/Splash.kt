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
@file:JvmName("Splash")

package com.shatteredpixel.shatteredpixeldungeon.effects

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap
import com.watabou.noosa.particles.Emitter
import com.watabou.noosa.particles.PixelParticle.Shrinking
import com.watabou.utils.HALF_PI
import com.watabou.utils.PI
import com.watabou.utils.PointF
import com.watabou.utils.Random

@JvmName("at")
fun splash(cell: Int, color: Int, n: Int) {
    splash(DungeonTilemap.tileCenterToWorld(cell), color, n)
}

@JvmName("at")
private inline fun splash(
    pos: PointF,
    color: Int,
    n: Int,
    dir: Float,
    cone: Float,
    action: Emitter.() -> Unit = { burst(SplashFactory, n) }
) {
    val emitter = GameScene.emitter()?.takeUnless { n <= 0 } ?: return
    emitter.pos(pos)
    SplashFactory.let {
        it.color = color
        it.dir = dir
        it.cone = cone
    }
    emitter.action()
}

@JvmName("at")
fun splash(p: PointF, color: Int, n: Int) = splash(p, color, n, dir = -HALF_PI, cone = PI)

@JvmName("at")
fun splash(p: PointF, dir: Float, cone: Float, color: Int, n: Int) = splash(p, color, n, dir, cone)

@JvmName("at")
fun splash(p: PointF, dir: Float, cone: Float, color: Int, n: Int, interval: Float) =
    splash(p, color, n, dir, cone) { start(SplashFactory, interval, n) }

private object SplashFactory : Emitter.Factory {
    var color = 0
    var dir = 0f
    var cone = 0f
    override fun Emitter.emit(index: Int, x: Float, y: Float) {
        recycle<Shrinking>().apply {
            reset(x, y, color, 4f, Random.Float(0.5f, 1.0f))
            speed.polar(Random.Float(dir - cone / 2, dir + cone / 2), Random.Float(40f, 80f))
            acc[0f] = +100f
        }
    }
}