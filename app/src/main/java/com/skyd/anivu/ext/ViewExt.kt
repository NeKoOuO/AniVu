package com.skyd.anivu.ext

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Rect
import android.view.DisplayCutout
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import androidx.annotation.OptIn
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.skyd.anivu.R


fun View.enable() {
    if (isEnabled) return
    isEnabled = true
}

fun View.disable() {
    if (!isEnabled) return
    isEnabled = false
}

fun View.gone(animate: Boolean = false, dur: Long = 500L) {
    if (isGone) return
    if (animate) startAnimation(AlphaAnimation(1f, 0f).apply { duration = dur })
    isGone = true
}

fun View.visible(animate: Boolean = false, dur: Long = 500L) {
    if (isVisible) return
    isVisible = true
    if (animate) startAnimation(AlphaAnimation(0f, 1f).apply { duration = dur })
}

fun View.invisible(animate: Boolean = false, dur: Long = 500L) {
    if (isInvisible) return
    isInvisible = true
    if (animate) startAnimation(AlphaAnimation(0f, 1f).apply { duration = dur })
}

fun View.clickScale(scale: Float = 0.75f, duration: Long = 100) {
    animate().scaleX(scale).scaleY(scale).setDuration(duration)
        .withEndAction {
            animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
        }.start()
}

val View.activity: Activity
    get() = context.activity

val View.tryActivity: Activity?
    get() = context.tryActivity

/**
 * 判断View和给定的Rect是否重叠（边和点不计入）
 * @return true if overlap
 */
fun View.overlap(rect: Rect): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    val left = location[0]
    val right = location[0] + width
    val top = location[1]
    val bottom = location[1] + height
    return !(left > rect.right || right < rect.left || top > rect.bottom || bottom < rect.top)
}

/**
 * 判断不包括padding的View和给定的Rect是否重叠（边和点不计入）
 * @return true if overlap
 */
fun View.overlapConsiderPaddingMargin(rect: Rect): Boolean {
    val location = IntArray(2)
    getLocationOnScreen(location)
    val left = location[0] + paddingLeft + marginLeft
    val right = location[0] + width - paddingRight - marginRight
    val top = location[1] + paddingTop + marginTop
    val bottom = location[1] + height - paddingBottom - marginBottom
    return !(left > rect.right || right < rect.left || top > rect.bottom || bottom < rect.top)
}

fun View.addInsetsByPadding(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false,
    hook: (View, WindowInsetsCompat) -> WindowInsetsCompat = { _, ins -> ins },
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, ins ->
        val maxInsets = Insets.max(
            ins.getInsets(WindowInsetsCompat.Type.systemBars()),
            ins.getInsets(WindowInsetsCompat.Type.displayCutout())
        )
        if (top) {
            val lastTopPadding = v.getTag(R.id.view_add_insets_padding_top_tag) as? Int ?: 0
            val newTopPadding = maxInsets.top
            v.setTag(R.id.view_add_insets_padding_top_tag, newTopPadding)
            v.updatePadding(top = v.paddingTop - lastTopPadding + newTopPadding)
        }
        if (bottom) {
            val lastBottomPadding = v.getTag(R.id.view_add_insets_padding_bottom_tag) as? Int ?: 0
            val newBottomPadding = maxInsets.bottom
            v.setTag(R.id.view_add_insets_padding_bottom_tag, newBottomPadding)
            v.updatePadding(bottom = v.paddingBottom - lastBottomPadding + newBottomPadding)
        }
        if (left) {
            val lastLeftPadding = v.getTag(R.id.view_add_insets_padding_left_tag) as? Int ?: 0
            val newLeftPadding = maxInsets.left
            v.setTag(R.id.view_add_insets_padding_left_tag, newLeftPadding)
            v.updatePadding(left = v.paddingLeft - lastLeftPadding + newLeftPadding)
        }
        if (right) {
            val lastRightPadding = v.getTag(R.id.view_add_insets_padding_right_tag) as? Int ?: 0
            val newRightPadding = maxInsets.right
            v.setTag(R.id.view_add_insets_padding_right_tag, newRightPadding)
            v.updatePadding(right = v.paddingRight - lastRightPadding + newRightPadding)
        }
        hook(v, ins)
    }
}


fun View.addInsetsByMargin(
    top: Boolean = false,
    bottom: Boolean = false,
    left: Boolean = false,
    right: Boolean = false,
    hook: (View, WindowInsetsCompat) -> WindowInsetsCompat = { _, ins -> ins },
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, ins ->
        val maxInsets = Insets.max(
            ins.getInsets(WindowInsetsCompat.Type.systemBars()),
            ins.getInsets(WindowInsetsCompat.Type.displayCutout())
        )
        if (top) {
            val lastTopMargin = v.getTag(R.id.view_add_insets_margin_top_tag) as? Int ?: 0
            val newTopMargin = maxInsets.top
            v.setTag(R.id.view_add_insets_margin_top_tag, newTopMargin)
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                layoutParams.topMargin = layoutParams.topMargin - lastTopMargin + newTopMargin
                v.layoutParams = layoutParams
            }
        }
        if (bottom) {
            val lastBottomMargin = v.getTag(R.id.view_add_insets_margin_bottom_tag) as? Int ?: 0
            val newBottomMargin = maxInsets.bottom
            v.setTag(R.id.view_add_insets_margin_bottom_tag, newBottomMargin)
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                layoutParams.bottomMargin =
                    layoutParams.bottomMargin - lastBottomMargin + newBottomMargin
                v.layoutParams = layoutParams
            }
        }
        if (left) {
            val lastLeftMargin = v.getTag(R.id.view_add_insets_margin_left_tag) as? Int ?: 0
            val newLeftMargin = maxInsets.left
            v.setTag(R.id.view_add_insets_margin_left_tag, newLeftMargin)
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                layoutParams.leftMargin = layoutParams.leftMargin - lastLeftMargin + newLeftMargin
                v.layoutParams = layoutParams
            }
        }
        if (right) {
            val lastRightMargin = v.getTag(R.id.view_add_insets_margin_right_tag) as? Int ?: 0
            val newRightMargin = maxInsets.right
            v.setTag(R.id.view_add_insets_margin_right_tag, newRightMargin)
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { layoutParams ->
                layoutParams.rightMargin =
                    layoutParams.rightMargin - lastRightMargin + newRightMargin
                v.layoutParams = layoutParams
            }
        }
        hook(v, ins)
    }
}

/**
 * 只在fitsSystemWindows="true"并且真正生效时此方法才有效
 */
fun addFabBottomPaddingHook(v: View, ins: WindowInsetsCompat): WindowInsetsCompat {
    val lastFabBottomPadding = v.getTag(R.id.view_add_fab_bottom_padding_tag) as? Int ?: 0
    val fabBottomPadding = 56.dp + v.resources.getDimensionPixelSize(R.dimen.fab_margin)
    v.setTag(R.id.view_add_fab_bottom_padding_tag, fabBottomPadding)
    v.updatePadding(
        bottom = v.paddingBottom - lastFabBottomPadding + fabBottomPadding
    )
    return ins
}

fun View.showSoftKeyboard() {
    if (requestFocus()) {
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.showSoftKeyboard(window: Window) {
    if (requestFocus()) {
        WindowCompat.getInsetsController(window, this).show(WindowInsetsCompat.Type.ime())
    }
}

fun View.hideSoftKeyboard(window: Window) {
    if (requestFocus()) {
        WindowCompat.getInsetsController(window, this).hide(WindowInsetsCompat.Type.ime())
    }
}

@TargetApi(28)
fun View.updateSafeInset(displayCutout: DisplayCutout) {
    val location = IntArray(2)
    getLocationOnScreen(location)
    val left = location[0]
    val right = location[0] + width
    val top = location[1]
    val bottom = location[1] + height

    var leftSolved = false
    var rightSolved = false

    val lastInsetPaddingLeft = getTag(R.id.view_player_insets_tag_left) as? Int ?: 0
    val lastInsetPaddingRight = getTag(R.id.view_player_insets_tag_right) as? Int ?: 0
    val lastInsetPaddingTop = getTag(R.id.view_player_insets_tag_top) as? Int ?: 0
    val lastInsetPaddingBottom = getTag(R.id.view_player_insets_tag_bottom) as? Int ?: 0

    updatePadding(
        left = paddingLeft - lastInsetPaddingLeft,
        right = paddingRight - lastInsetPaddingRight,
        top = paddingTop - lastInsetPaddingTop,
        bottom = paddingBottom - lastInsetPaddingBottom,
    )
    if (!inSafeInset(displayCutout)) {
        // left
        if (left + paddingLeft < displayCutout.safeInsetLeft) {
            val newPaddingLeft = paddingLeft + displayCutout.safeInsetLeft
            setTag(R.id.view_player_insets_tag_left, newPaddingLeft)
            updatePadding(left = newPaddingLeft)
            leftSolved = true
        }

        // right
        if (right - paddingRight > context.screenWidth(true) - displayCutout.safeInsetRight) {
            val newPaddingRight = paddingRight + displayCutout.safeInsetRight
            setTag(R.id.view_player_insets_tag_right, newPaddingRight)
            updatePadding(right = newPaddingRight)
            rightSolved = true
        }

        // top
        if (top + paddingTop < displayCutout.safeInsetTop) {
            if (!leftSolved && !rightSolved) {
                val newPaddingTop = paddingTop + displayCutout.safeInsetTop
                setTag(R.id.view_player_insets_tag_top, newPaddingTop)
                updatePadding(top = newPaddingTop)
            }
        }

        // bottom
        if (bottom - paddingBottom > context.screenHeight(true) - displayCutout.safeInsetBottom) {
            if (!leftSolved && !rightSolved) {
                val newPaddingBottom = paddingBottom + displayCutout.safeInsetBottom
                setTag(R.id.view_player_insets_tag_bottom, newPaddingBottom)
                updatePadding(bottom = newPaddingBottom)
            }
        }
    }
}

@TargetApi(28)
fun View.inSafeInset(displayCutout: DisplayCutout): Boolean {
    displayCutout.boundingRects.forEach {
        if (overlapConsiderPaddingMargin(it)) return false
    }
    return true
}

@OptIn(ExperimentalBadgeUtils::class)
fun View.addBadge(init: BadgeDrawable.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            BadgeDrawable.create(context).apply {
                this.init()
                BadgeUtils.attachBadgeDrawable(this, this@addBadge)
            }
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun View.findMainNavController(): NavController {
    return Navigation.findNavController(activity, R.id.nav_host_fragment_main)
}