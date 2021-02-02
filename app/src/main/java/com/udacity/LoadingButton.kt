package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonLabel = resources.getString(R.string.button_label)

    private var drawProgressWidth: Float = 0F
    private var drawFilledCircleAngle: Float = 0F

    private var buttonRect = Rect()
    private var progressAnimator = ValueAnimator()
    private var filledCircleAnimator = ValueAnimator()

    private val animationDuration = 2000L
    private val animationDurationFastEnd = 300L



    init {
        isClickable = true
    }


    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Initial) { p, old, new ->
        when(new) {
            ButtonState.Initial -> {
                buttonLabel = resources.getString(R.string.button_label)
            }
            ButtonState.Clicked -> {

            }
            ButtonState.Loading -> {
                buttonLabel = resources.getString(R.string.button_loading)
                progressAnimator = ValueAnimator.ofFloat(0F, measuredWidth.toFloat()).apply {
                    duration = animationDuration
                    addUpdateListener { animation ->
                        drawProgressWidth = animation.animatedValue as Float
                        animation.repeatCount = 0
                        invalidate()
                    }
                    start()
                }
                filledCircleAnimator = ValueAnimator.ofFloat(0F, 360F).apply {
                    duration = animationDuration
                    addUpdateListener { animation ->
                        drawFilledCircleAngle = animation.animatedValue as Float
                        animation.repeatCount = 0
                        invalidate()
                    }
                    start()
                }
            }

            ButtonState.Completed -> {
                progressAnimator.cancel()
                progressAnimator = ValueAnimator.ofFloat(drawProgressWidth, measuredWidth.toFloat()).apply {
                    duration = animationDurationFastEnd
                    addUpdateListener { animation ->
                        drawProgressWidth = animation.animatedValue as Float
                        animation.repeatCount = 0
                        invalidate()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            buttonState = ButtonState.Initial
                        }
                    })
                    start()
                }

                filledCircleAnimator.cancel()
                filledCircleAnimator = ValueAnimator.ofFloat(drawFilledCircleAngle, 360F).apply {
                    duration = animationDurationFastEnd
                    addUpdateListener { animation ->
                        drawFilledCircleAngle = animation.animatedValue as Float
                        animation.repeatCount = 0
                        invalidate()
                    }
                    start()
                }
            }
        }
    }


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // draw button
        paint.color = getColor(context, R.color.colorPrimary)

        buttonRect = Rect(0, 0, measuredWidth, measuredHeight)
        canvas?.drawRect(buttonRect, paint)

        // draw progress value
        when (buttonState) {
            ButtonState.Initial -> {

            }
            ButtonState.Loading -> {
                // draw progress
                paint.color = getColor(context, R.color.colorPrimaryDark)
                canvas?.drawRect(0f, 0f, drawProgressWidth, measuredHeight.toFloat(), paint)

                // draw progress circle
                paint.color = getColor(context, R.color.colorAccent)
                val circleX = (measuredWidth / 2f) + (paint.measureText(buttonLabel) / 2f)
                val circleY = (measuredHeight / 2f)
                canvas?.drawArc(
                        circleX,
                        circleY - 35f,
                        (circleX + 60f),
                        circleY + 30f,
                        0F,
                        drawFilledCircleAngle,
                        true,
                        paint)
            }
            ButtonState.Completed -> {
                // draw progress
                paint.color = getColor(context, R.color.colorPrimaryDark)
                canvas?.drawRect(0f, 0f, drawProgressWidth, measuredHeight.toFloat(), paint)

                // draw progress circle
                paint.color = getColor(context, R.color.colorAccent)
                val circleX = (measuredWidth / 2f) + (paint.measureText(buttonLabel) / 2f)
                val circleY = (measuredHeight / 2f)
                canvas?.drawArc(
                        circleX,
                        circleY - 35f,
                        (circleX + 60f),
                        circleY + 30f,
                        0F,
                        drawFilledCircleAngle,
                        true,
                        paint)
            }
        }

        // draw button label
        paint.color = Color.WHITE
        paint.getTextBounds(buttonLabel, 0, buttonLabel.length, buttonRect)
        val x: Float = measuredWidth / 2f
        val y: Float = measuredHeight / 2f + buttonRect.height() / 2f - buttonRect.bottom
        canvas?.drawText(buttonLabel, x, y, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
                MeasureSpec.getSize(w),
                heightMeasureSpec,
                0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun setState(newState: ButtonState) {
        buttonState = newState
    }

}