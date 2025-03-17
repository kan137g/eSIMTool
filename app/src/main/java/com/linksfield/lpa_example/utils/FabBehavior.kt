package com.linksfield.lpa_example.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * CreateDate: 2020/8/25 15:06
 * Author: you
 * Description:
 */
class FabBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior(context, attrs) {

    /**
     * 是否正在动画
     */
    private var isAnimateIng = false

    /**
     * 是否已经显示
     */
    private var isShow = true

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        // 手指上滑，隐藏FAB
        if ((dyConsumed > 0 || dyUnconsumed > 0) && !isAnimateIng && isShow) {
            ViewCompat.animate(child)
                    .translationY(350f)
                    .setDuration(400)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setListener(object : StateListener() {
                        override fun onAnimationStart(view: View) {
                            super.onAnimationStart(view)
                            isShow = false
                        }
                    })
                    .start()
        } else if (dyConsumed < 0 || dyUnconsumed < 0 && !isAnimateIng && !isShow) {
            // 手指下滑，显示FAB
            ViewCompat.animate(child)
                    .translationY(0f)
                    .setDuration(400)
                    .setListener(object : StateListener() {
                        override fun onAnimationStart(view: View) {
                            super.onAnimationStart(view)
                            isShow = true
                        }
                    })
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .start()
        }
    }

    open inner class StateListener : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {
            isAnimateIng = true
        }

        override fun onAnimationEnd(view: View) {
            isAnimateIng = false
        }

        override fun onAnimationCancel(view: View) {
            isAnimateIng = false
        }
    }

}