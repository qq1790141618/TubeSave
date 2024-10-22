package com.fixeam.tubesave.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import com.fixeam.tubesave.R
import kotlin.math.abs

object PopupWindowManager {
    /**
     * PopupWindow显示方向
     */
    enum class PopupWindowDirection {
        Left,
        Top,
        Right,
        Center,
        Bottom
    }
    /**
     * 创建弹出窗口
     * @param view 显示窗口的内容View
     * @param parentView 显示窗口的容器View
     * @param direction 显示方向PopupWindowDirection
     * @param app 执行显示的活动AppCompatActivity
     * @param focusable 可聚焦，一般用于管理外部点击关闭
     * @param isOutsideTouchable 外部可点击，一般用于管理外部点击关闭
     * @param showOverlay 显示遮罩层
     * @param showAnimation 显示动画
     * @param onDismiss 窗口关闭时执行
     */
    fun createPopupWindow(
        app: AppCompatActivity,
        view: View,
        parentView: View,
        direction: PopupWindowDirection,
        focusable: Boolean = true,
        isOutsideTouchable: Boolean = true,
        showOverlay: Boolean = true,
        showAnimation: Boolean = true,
        onDismiss: () -> Unit = {}
    ): PopupWindow {
        if(showOverlay){
            ActivityDisplay(app).changeBackgroundDim(true)
        }

        var horizontalLayoutParams = ViewGroup.LayoutParams.WRAP_CONTENT
        if(direction == PopupWindowDirection.Left || direction == PopupWindowDirection.Bottom){
            horizontalLayoutParams = ViewGroup.LayoutParams.MATCH_PARENT
        }
        var verticalLayoutParams = ViewGroup.LayoutParams.WRAP_CONTENT
        if(direction == PopupWindowDirection.Left || direction == PopupWindowDirection.Right){
            verticalLayoutParams = ViewGroup.LayoutParams.MATCH_PARENT
        }

        val popupWindow = PopupWindow(
            view,
            horizontalLayoutParams,
            verticalLayoutParams,
            focusable
        )

        popupWindow.isFocusable = isOutsideTouchable
        popupWindow.isOutsideTouchable = isOutsideTouchable
        if(showAnimation){
            popupWindow.animationStyle = when(direction){
                PopupWindowDirection.Left -> R.style.PopupAnimationLeft
                PopupWindowDirection.Top -> R.style.PopupAnimationTop
                PopupWindowDirection.Bottom -> R.style.PopupAnimationBottom
                PopupWindowDirection.Center -> R.style.PopupAnimationCenter
                else -> 0
            }
        }

        val gravity = when(direction){
            PopupWindowDirection.Left -> Gravity.START
            PopupWindowDirection.Right -> Gravity.END
            PopupWindowDirection.Top -> Gravity.TOP
            PopupWindowDirection.Bottom -> Gravity.BOTTOM
            PopupWindowDirection.Center -> Gravity.CENTER
        }
        popupWindow.showAtLocation(
            parentView,
            gravity,
            0,
            0
        )

        view.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0f
            private var lastY = 0f
            private var initialX = 0f
            private var initialY = 0f
            private var startTime = 0L

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        initialX = v.x
                        initialY = v.y
                        startTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        when(direction){
                            PopupWindowDirection.Left -> {
                                if (deltaX < 0) v.x = initialX + deltaX.coerceAtLeast(0f)
                            }
                            PopupWindowDirection.Right -> {
                                if (deltaX > 0) v.x = initialX + deltaX.coerceAtLeast(0f)
                            }
                            PopupWindowDirection.Top -> {
                                if (deltaY < 0) v.y = initialY + deltaY.coerceAtLeast(0f)
                            }
                            PopupWindowDirection.Bottom -> {
                                if (deltaY > 0) v.y = initialY + deltaY.coerceAtLeast(0f)
                            }
                            PopupWindowDirection.Center -> {
                                // 不移动
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        handleRelease(v)
                    }
                }
                return true
            }

            private fun handleRelease(v: View) {
                val useTime = System.currentTimeMillis() - startTime

                val movedX = v.x - initialX
                val movedY = v.y - initialY

                val containerWidth = v.width
                val containerHeight = v.height

                val thresholdDistance = 0.45f
                val thresholdTime = 0.05f

                val conditionDistance = abs(movedX) > containerWidth * thresholdDistance || abs(movedY) > containerHeight * thresholdDistance
                val conditionTime = useTime < 300 && (
                        abs(movedX) > containerWidth * thresholdTime || abs(movedY) > containerHeight * thresholdTime
                        )

                if (conditionDistance || conditionTime) {
                    popupWindow.dismiss()
                } else {
                    animateViewBackToInitialPosition(v)
                }
            }

            private fun animateViewBackToInitialPosition(v: View) {
                val animatorX = ObjectAnimator.ofFloat(v, "x", v.x, initialX)
                val animatorY = ObjectAnimator.ofFloat(v, "y", v.y, initialY)
                animatorX.duration = 300
                animatorY.duration = 300
                animatorX.interpolator = DecelerateInterpolator()
                animatorY.interpolator = DecelerateInterpolator()
                animatorX.start()
                animatorY.start()
            }
        })

        popupWindow.setOnDismissListener {
            onDismiss()
            if(showOverlay) {
                ActivityDisplay(app).changeBackgroundDim(false)
            }
        }

        return popupWindow
    }
}