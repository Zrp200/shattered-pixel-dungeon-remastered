package com.shatteredpixel.shatteredpixeldungeon.ui.changelist

import com.shatteredpixel.shatteredpixeldungeon.items.Item
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons
import com.shatteredpixel.shatteredpixeldungeon.ui.Window.TITLE_COLOR

@DslMarker annotation class ChangeDSL
@DslMarker annotation class InfoDSL
@DslMarker annotation class ButtonDSL

typealias ChangeList = MutableList<ChangeInfo>

inline operator fun ChangeList.invoke(build: ChangeListBuilder.()->Unit
) = ChangeListBuilder(this).build()


typealias ChangeInfoBuilder = ChangeInfo.()->Unit
typealias ButtonBuilder = ChangeButton.()->Unit

@ChangeDSL
class ChangeListBuilder @PublishedApi internal constructor(
    private val changeInfos: ChangeList) {

    operator fun ChangeInfo.unaryPlus() = also(changeInfos::add)

    inline fun version(title: String, text: String="", builder: ChangeInfoBuilder) = ChangeInfo(title, true, text)
        .apply {
            +this
            hardlight(TITLE_COLOR)
            builder()
        }

    inline fun section(
        title: String? = null,
        color: Int = TITLE_COLOR,
        text: String? = null,
        build: InfoBuilder.()->Unit
    ) = ChangeInfo(title,false,text)
        .apply {
            +this
            hardlight(color)
            InfoBuilder(this).build()
        }

    @InfoDSL
    inner class InfoBuilder(val info: ChangeInfo) {
        init { +info }
        operator fun ChangeButton.unaryPlus() = info.addButton(this)
        operator fun Item.invoke(
            title: String = name(),
            message: String = "",
            @ButtonDSL build: ButtonBuilder = {}
        ) = +ChangeButton(this, message, title).also(build)

        fun commentary(
            releaseDate: String, vararg milestones: Pair<String,String>,
            builder: ButtonBuilder={}
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