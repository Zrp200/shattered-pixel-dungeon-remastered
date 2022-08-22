package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.shatteredpixel.shatteredpixeldungeon.ui.Window.TITLE_COLOR

@DslMarker
annotation class ChangeDSL
@DslMarker
annotation class InfoDSL
@DslMarker
annotation class ButtonDSL

typealias ChangeList = MutableList<ChangeInfo>

inline operator fun ChangeList.invoke(
    build: ChangeListBuilder.() -> Unit
) = ChangeListBuilder(this).build()


typealias ButtonBuilder = ChangeButton.() -> Unit

@ChangeDSL
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
        build: InfoBuilder.()->Unit
    ) = ChangeInfo(title, major, text)
        .apply {
            hardlight(color)
            InfoBuilder(this).build()
        }

    @InfoDSL
    inner class InfoBuilder(val info: ChangeInfo) {
        init {
            +info
        }

        operator fun ChangeButton.unaryPlus() = info.addButton(this)
        operator fun Item.invoke(
            title: String = name(),
            message: String = "",
            @ButtonDSL build: ButtonBuilder = {}
        ) = +ChangeButton(this, message, title).also(build)

        fun commentary(
            releaseDate: String, vararg milestones: Pair<String, String>,
            builder: ButtonBuilder = {}
        ) = +ChangeButton(Icons.get(Icons.SHPX), "Developer Commentary").apply {
            +buildList {
                add("Released $releaseDate")
                milestones.mapTo(this) { (delta, version) -> "$delta since $version" }
            }
            +""
            builder()
        }
    }
}