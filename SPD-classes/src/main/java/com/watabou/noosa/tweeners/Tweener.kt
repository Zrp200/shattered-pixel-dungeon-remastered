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

import com.watabou.noosa.Game
import com.watabou.noosa.Gizmo

/**
 * Performs a certain progress-dependant [action][updateValues] over a set time [interval].
 *
 * @property interval lifespan of the tweener
 */
abstract class Tweener(
    private val interval: Float
) : Gizmo() {

    private var elapsed = 0f

    var listener: Listener? = null

    override fun update() {
        elapsed += Game.elapsed

        // it's better to skip this frame ahead and finish one frame early
        // if doing one more frame would result in lots of overshoot
        if (interval - elapsed < Game.elapsed / 2f) elapsed = interval
        if (elapsed >= interval) {
            updateValues(1f)
            finish()
        } else {
            updateValues(elapsed / interval)
        }
    }

    /**
     * Finish execution of this tweener.
     */
    fun finish() {
        onComplete()
        kill()
    }

    /**
     * Actions to execute upon [completion][finish].
     */
    protected open fun onComplete() {
        listener?.onComplete(this)
    }

    /**
     * Action to execute on [update] during the [lifespan][interval] of this tweener.
     *
     * @param progress
     */
    protected abstract fun updateValues(progress: Float)

    // TODO: replace with a callback
    interface Listener {
        fun onComplete(tweener: Tweener?)
    }
}
