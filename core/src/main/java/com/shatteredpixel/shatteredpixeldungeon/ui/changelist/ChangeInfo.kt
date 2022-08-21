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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock
import com.watabou.noosa.ColorBlock
import com.watabou.noosa.ui.Component

@InfoDSL
class ChangeInfo(
    title: String? = null,
    @JvmField var major: Boolean = true,
    text: String? = null
) : Component() {
    protected var line = ColorBlock(1f, 1f, if (major) -0xddddde else -0xcccccd)
        .also(::add)
    var title: RenderedTextBlock = PixelScene.renderTextBlock(title, if(major) 9 else 7)
        .also(::add)
        private set

    private var text = text
        ?.takeUnless(""::equals)
        ?.run { PixelScene.renderTextBlock(text, 6) }
        ?.also(::add)

    private val buttons = ArrayList<ChangeButton>()

    fun hardlight(color: Int) = title.hardlight(color)

    fun addButton(button: ChangeButton) {
        buttons.add(button)
        add(button)
        button.setSize(16f, 16f)
        layout()
    }
    operator fun ChangeButton.unaryPlus() = addButton(this)

    fun onClick(x: Float, y: Float) = buttons.find { it.inside(x, y) }?.onClick() != null

    public override fun layout() {
        var posY = y + 3
        if (major) posY += 2f
        title.setPos(
            x + (width - title.width()) / 2f,
            posY
        )
        PixelScene.align(title)
        posY += title.height() + 2
        text?.run {
            maxWidth(width().toInt())
            setPos(x, posY)
            posY += height()
        }
        var posX = x
        var tallest = 0f
        for (change in buttons) {
            if (posX + change.width() >= right()) {
                posX = x
                posY += tallest
                tallest = 0f
            }

            //centers
            if (posX == x) {
                var offset = width
                for (b in buttons) {
                    offset -= b.width()
                    if (offset <= 0) {
                        offset += b.width()
                        break
                    }
                }
                posX += offset / 2f
            }
            change.setPos(posX, posY)
            posX += change.width()
            if (tallest < change.height()) {
                tallest = change.height()
            }
        }
        posY += tallest + 2
        height = posY - y
        fun setLine(
            width: Float = width(),
            height: Float = height(),
            x: Float = this.x,
            y: Float = this.y
        ) = line.let {
            it.size(width, height)
            it.x = x
            it.y = y
        }
        if(major) {
            setLine(height = 1f, y = y+2)
        } else if (x == 0f) {
            setLine(width = 1f, x = width)
        } else {
            setLine(width = 1f)
        }

    }

    inline operator fun invoke(@InfoDSL builder: ChangeInfo.() -> Unit) = apply {
        builder()
        layout()
    }

}