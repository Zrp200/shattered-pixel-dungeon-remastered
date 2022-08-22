package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.shatteredpixel.shatteredpixeldungeon.ui.Window.TITLE_COLOR
import com.watabou.noosa.Image
import java.lang.Appendable

@DslMarker
annotation class Container

@DslMarker
annotation class Builder

typealias ChangeList = MutableList<ChangeInfo>

inline operator fun ChangeList.invoke(
    build: ChangeListBuilder.() -> Unit
) = ChangeListBuilder(this).build()

@Container
class ChangeListBuilder @PublishedApi internal constructor(
    private val changeInfos: ChangeList
) {

    operator fun ChangeInfo.unaryPlus() = also(changeInfos::add)

    inline fun version(title: String, text: String = "", build: InfoBuilder.() -> Unit) =
        section(title, text, major = true, build = build)

    inline fun section(
        title: String? = null,
        text: String? = null,
        color: Int = TITLE_COLOR,
        major: Boolean = false,
        build: InfoBuilder.() -> Unit
    ) = ChangeInfo(title, major, text)
        .apply {
            hardlight(color)
            InfoBuilder(this).build()
        }

    @Builder
    inner class InfoBuilder(val info: ChangeInfo) {
        init {
            +info
        }

        operator fun ChangeButton.unaryPlus() = also(info::addButton)

        inline fun button(icon: Image, title: String, build: MessageBuilder.() -> Unit) {
            +MessageBuilder(title).also(build)
                .let { (title, message) -> ChangeButton(icon, title, message.toString()) }
        }

        inline fun item(
            icon: Int,
            name: String,
            glowing: ItemSprite.Glowing? = null,
            build: MessageBuilder.() -> Unit
        ) = button(ItemSprite(icon, glowing), name, build)

        inline operator fun Item.invoke(
            name: String = name(),
            build: MessageBuilder.() -> Unit
        ) = item(image, name, glowing(), build)

        inline fun commentary(
            releaseDate: String, vararg milestones: Pair<String, String>,
            builder: MessageBuilder.()->Unit = {}
        ) = button(Icons.get(Icons.SHPX), "Developer Commentary") {
            list(buildList {
                add("Released $releaseDate")
                milestones.mapTo(this) { (delta, version) -> "$delta since $version" }
            })
            appendLine()
            builder()
        }
    }
}

@Container
@Builder
data class MessageBuilder(val title: String, val message: StringBuilder = StringBuilder()) :
    Appendable by message, CharSequence by message {
    operator fun CharSequence.unaryPlus() = appendLine(this)

    fun list(list: List<CharSequence>, spacing: Int = 1): Unit =
        list(*list.toTypedArray(), spacing = spacing)

    inline fun list(builder: MutableList<CharSequence>.()-> Unit) = list(buildList(builder))

    fun list(vararg items: CharSequence, spacing: Int = 1) {
        items.forEach {
            append("_-_ $it")
            repeat(spacing) { appendLine() }
        }
    }

    override fun toString(): String {
        return message.toString().trim()
    }
}