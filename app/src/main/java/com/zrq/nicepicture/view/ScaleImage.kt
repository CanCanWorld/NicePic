package com.zrq.nicepicture.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.google.android.material.imageview.ShapeableImageView
import com.zrq.nicepicture.R

class ScaleImage : ShapeableImageView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        if (context != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleImage)
            startScale = typedArray.getFloat(R.styleable.ScaleImage_startScale, 0f)
            duration = typedArray.getInteger(R.styleable.ScaleImage_duration, 400)
            typedArray.recycle()
        }
    }

    private var num = Int.MAX_VALUE
    var startScale = 0f
    var duration = 400

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val count = duration / 10
        if (num <= count) {
            scaleX = num * 1f / count
            scaleY = num * 1f / count
            num++
            invalidate()
        }
    }

    fun startAnim() {
        if (startScale > 1 || startScale < 0) startScale = 0f
        num = (duration * startScale / 10).toInt()
    }

}