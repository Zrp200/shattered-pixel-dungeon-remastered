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
package com.watabou.noosa

/**
 * Base building block of the game UI.
 */
open class Gizmo {

    /**
     * Dead gizmos are kept in their [Group] for later [recycle][Group.recycle].
     */
    var alive = true
        get() = parent?.let { field && it.alive } ?: field

    /**
     * Active gizmos are [updated][update] every frame.
     */
	open var active = true
        get() = parent?.let { field && it.active } ?: field

    /**
     * Visible gizmos are [drawn][draw] every frame.
     */
    open var visible = true
        get() = parent?.let { field && it.visible } ?: field

    /**
     * Parents keep track of their [children][Group.children].
     */
    var parent: Group? = null

    /**
     * Camera is used to render visuals.
     */
    open var camera: Camera? = null
        get() = field ?: parent?.camera

    /**
     * Updates this gizmo. Called every frame as long as this gizmo is [active].
     */
    open fun update() {}

    /**
     * Renders this gizmo on the screen. Called every frame as long as this gizmo is [visible].
     */
    open fun draw() {}

    /**
     * [Kills][alive] this gizmo.
     *
     * Killed gizmos can be [recycled][Group.recycle] or [revived][revive].
     */
    open fun kill() {
        alive = false
    }

    /**
     * Brings this gizmo back to [live][alive].
     *
     * Not exactly opposite to [kill] method.
     */
    open fun revive() {
        alive = true
    }

    /**
     * Detaches this gizmo from its [parent] group.
     */
    fun remove() {
        parent?.remove(this)
    }

    /**
     * Detaches this gizmo from its [parent] group.
     *
     * This method supports overrides. Use clean [remove] to avoid any extra destruction logic.
     */
    open fun destroy() = remove()
}
