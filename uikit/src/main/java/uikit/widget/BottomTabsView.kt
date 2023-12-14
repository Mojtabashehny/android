package uikit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.LinearLayoutCompat
import uikit.R
import uikit.extensions.createRipple
import uikit.extensions.dp
import uikit.extensions.useAttributes

@SuppressLint("RestrictedApi")
class BottomTabsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyle) {

    private val menu: MenuBuilder by lazy { MenuBuilder(context) }
    private val menuInflater: MenuInflater by lazy { MenuInflater(context) }
    private var selectedItemId = 0
        set(value) {
            if (field != value) {
                field = value
                updateSelected()
            }
        }

    var doOnClick: ((itemId: Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL

        context.useAttributes(attrs, R.styleable.BottomTabsView) {
            if (it.hasValue(R.styleable.BottomTabsView_menu)) {
                inflateMenu(it.getResourceId(R.styleable.BottomTabsView_menu, 0))
            }
        }
    }

    private fun inflateMenu(resId: Int) {
        menuInflater.inflate(resId, menu)
        if (menu.size() > 0) {
            initMenu()
        }
    }

    private fun initMenu() {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.isChecked) {
                selectedItemId = item.itemId
            }
            addMenu(item)
        }

        updateSelected()
    }

    private fun updateSelected() {
        val colorActive = context.getColor(R.color.tabBarActiveIcon)
        val colorInactive = context.getColor(R.color.tabBarInactiveIcon)

        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val iconView = view.findViewById<ImageView>(R.id.icon)
            val titleView = view.findViewById<TextView>(R.id.title)
            if (view.tag == selectedItemId) {
                iconView.imageTintList = ColorStateList.valueOf(colorActive)
                titleView.setTextColor(colorActive)
            } else {
                iconView.imageTintList = ColorStateList.valueOf(colorInactive)
                titleView.setTextColor(colorInactive)
            }
        }
    }

    private fun addMenu(menuItem: MenuItem) {
        val view = inflate(context, R.layout.view_bottom_tab, null)
        view.tag = menuItem.itemId
        view.background = context.createRipple()
        val iconView = view.findViewById<ImageView>(R.id.icon)
        val titleView = view.findViewById<TextView>(R.id.title)
        iconView.setImageDrawable(menuItem.icon!!)
        titleView.text = menuItem.title!!

        if (menuItem.isCheckable) {
            view.setOnClickListener {
                selectedItemId = menuItem.itemId
                doOnClick?.invoke(menuItem.itemId)
            }
        }

        addView(view, LayoutParams(0, LayoutParams.MATCH_PARENT, 1f))
    }

    fun setItemChecked(itemId: Int) {
        selectedItemId = itemId
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(64.dp, MeasureSpec.EXACTLY))
    }
}