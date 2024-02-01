package uikit.base

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uikit.R
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.getSpannable
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.BottomSheetLayout
import uikit.widget.ModalView
import uikit.widget.SwipeBackLayout
import java.util.concurrent.Executor

open class BaseFragment(
    @LayoutRes layoutId: Int
): Fragment(layoutId) {

    interface SwipeBack {

        fun onEndShowingAnimation() {

        }
    }

    interface BottomSheet {
        fun onEndShowingAnimation() {

        }
    }

    interface Modal {

        private val fragment: BaseFragment
            get() = this as BaseFragment

        private val view: ModalView
            get() = fragment.view as ModalView

        val behavior: BottomSheetBehavior<FrameLayout>
            get() = view.behavior

        fun onEndShowingAnimation() {

        }

        fun fixPeekHeight() {
            view.fixPeekHeight()
        }
    }

    val window: Window?
        get() = activity?.window

    val parent: Fragment?
        get() {
            if (parentFragment != null) {
                return parentFragment
            }
            return activity?.supportFragmentManager?.fragments?.lastOrNull {
                it != this && it.isVisible
            }
        }

    val mainExecutor: Executor
        get() = ContextCompat.getMainExecutor(requireContext())

    open val disableShowAnimation: Boolean = false

    open val secure: Boolean = false

    fun getSpannable(@StringRes id: Int): SpannableString {
        return requireContext().getSpannable(id)
    }

    fun registerForPermission(callback: ActivityResultCallback<Boolean>): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = super.onCreateView(inflater, container, savedInstanceState)!!

        val view = if (this is Modal) {
            wrapInModal(inflater.context, contentView, savedInstanceState)
        } else {
            contentView.setBackgroundResource(R.color.backgroundPage)
            when (this) {
                is SwipeBack -> wrapInSwipeBack(inflater.context, contentView, savedInstanceState)
                is BottomSheet -> wrapInBottomSheet(inflater.context, contentView, savedInstanceState)
                else -> contentView
            }
        }
        view.setOnClickListener {  }
        return view
    }

    private fun wrapInModal(context: Context, view: View, savedInstanceState: Bundle?): ModalView {
        this as Modal

        val modalView = ModalView(context)
        modalView.setContentView(view)
        modalView.doOnHide = { finishInternal() }
        if (savedInstanceState == null && !disableShowAnimation) {
            modalView.startShowAnimation()
        } else {
            modalView.doOnLayout { onEndShowingAnimation() }
        }
        return modalView
    }

    private fun wrapInSwipeBack(context: Context, view: View, savedInstanceState: Bundle?): SwipeBackLayout {
        this as SwipeBack

        val swipeBackLayout = SwipeBackLayout(context)
        swipeBackLayout.doOnCloseScreen = {
            finishInternal()
        }
        swipeBackLayout.doOnEndShowingAnimation = {
            onEndShowingAnimation()
        }
        swipeBackLayout.fragment = this
        swipeBackLayout.setContentView(view)
        if (savedInstanceState == null && !disableShowAnimation) {
            swipeBackLayout.startShowAnimation()
        } else {
            swipeBackLayout.doOnLayout { onEndShowingAnimation() }
        }
        return swipeBackLayout
    }

    private fun wrapInBottomSheet(context: Context, view: View, savedInstanceState: Bundle?): BottomSheetLayout {
        this as BottomSheet

        val bottomSheetLayout = BottomSheetLayout(context)
        bottomSheetLayout.doOnCloseScreen = {
            finishInternal()
        }
        bottomSheetLayout.doOnEndShowingAnimation = {
            onEndShowingAnimation()
        }
        bottomSheetLayout.fragment = this
        bottomSheetLayout.setContentView(view)
        if (savedInstanceState == null && !disableShowAnimation) {
            bottomSheetLayout.startShowAnimation()
        } else {
            bottomSheetLayout.doOnLayout { onEndShowingAnimation() }
        }
        return bottomSheetLayout
    }

    open fun onBackPressed(): Boolean {
        finish()
        return false
    }

    fun finish() {
        val view = view ?: return

        when (view) {
            is SwipeBackLayout -> view.startHideAnimation()
            is BottomSheetLayout -> view.startHideAnimation()
            is ModalView -> view.hide(true)
            else -> finishInternal()
        }
    }

    private fun finishInternal() {
        navigation?.remove(this)
    }

    override fun onResume() {
        super.onResume()
        if (secure) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        (view as? ModalView)?.show()
    }

    override fun onPause() {
        super.onPause()
        if (secure) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun removeCallbacks(callback: Runnable) {
        view?.removeCallbacks(callback)
    }

    fun postDelayed(delay: Long, action: Runnable) {
        view?.postDelayed(action, delay)
    }

    fun post(action: Runnable) {
        view?.post(action)
    }

    @ColorInt
    fun getColor(@ColorRes colorRes: Int): Int {
        return requireContext().getColor(colorRes)
    }

    fun getDrawable(
        drawableRes: Int,
        @ColorInt tintColor: Int = Color.TRANSPARENT
    ): Drawable {
        val drawable = ContextCompat.getDrawable(requireContext(), drawableRes)!!
        if (tintColor != Color.TRANSPARENT) {
            drawable.setTint(tintColor)
        }
        return drawable
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

}