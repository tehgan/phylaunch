package com.tehgan.phylaunch.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tehgan.phylaunch.R

const val PAGE_COUNT = 5 // Temporary

/**
 * 'Dot' page-indicator for ViewPager2
 * Hate to say it, but this was adapted from a ChatGPT-provided example.
 */
class Dots(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var dotCount: Int = PAGE_COUNT // Temporary

    // VV ChatGPT-provided variables VV //
    private val dotWidth: Int = 15
    private val dotHeight: Int = 15
    private val dotMargin: Int = 10

    private val dots: List<Drawable> = List(dotCount) {
        ContextCompat.getDrawable(context, R.drawable.dot)!!
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Clip off final margin as it's unnecessary
        val totalWidth = (dotCount * (dotWidth + dotMargin) - dotMargin)
        setMeasuredDimension(totalWidth, dotHeight)
    }

    // Draw dots in a horizontal line (* * * * *)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var dotX = 0
        for (dot in dots) {
            dot.setBounds(dotX, 0, dotX + dotWidth, dotHeight)
            dot.draw(canvas)

            dotX += dotWidth + dotMargin
        }
    }

    // Redraw dot on update
    fun updateDot(position: Int) {
        for (i in 0..<dots.size) {
            dots[i].alpha = 255
            if (i == position) {
                dots[i].alpha = 128
            }
        }
        invalidate()
    }

}