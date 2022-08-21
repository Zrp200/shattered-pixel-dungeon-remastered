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
package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon
import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite
import com.watabou.noosa.Image
import com.watabou.noosa.ui.Component

//not actually a button, but functions as one.
class ChangeButton(
    protected var icon: Image,
    title: String? = null,
    protected var message: String? = ""
) : Component() {

    init {
        add(icon)
    }

    protected val title: String = Messages.titleCase(title?:"")

    init {
        layout()
    }

    @JvmOverloads
    constructor(item: Item, message: String? = "", title: String = item.name()) : this(ItemSprite(item), title, message)
    fun onClick() = ShatteredPixelDungeon.scene.add(
        ChangesWindow(Image(icon),
            title,
            message?.trimIndent()?.trim()
        ))

    public override fun layout() {
        super.layout()
        icon.x = x + (width - icon.width()) / 2f
        icon.y = y + (height - icon.height()) / 2f
        PixelScene.align(icon)
    }

    operator fun String.unaryPlus() {
        message += "$this\n"
        layout()
    }
    operator fun Iterable<String>.unaryPlus() = forEach { +"_-_ $it" }

    inline operator fun invoke( builder: ChangeButton.()->Unit ) = apply {
        builder()
        layout()
    }
}