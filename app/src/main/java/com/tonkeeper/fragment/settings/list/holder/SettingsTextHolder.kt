package com.tonkeeper.fragment.settings.list.holder

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tonkeeper.fragment.settings.list.item.SettingsItem
import com.tonkeeper.fragment.settings.list.item.SettingsTextItem
import uikit.widget.item.ItemTextView

class SettingsTextHolder(
    parent: ViewGroup,
    onClick: ((SettingsItem) -> Unit)?
): SettingsHolder<SettingsTextItem>(ItemTextView(parent.context), onClick) {

    private val view = itemView as ItemTextView

    init {
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onBind(item: SettingsTextItem) {
        view.setOnClickListener { onClick?.invoke(item) }
        view.text = getString(item.titleRes)
        view.data = item.data
    }


}