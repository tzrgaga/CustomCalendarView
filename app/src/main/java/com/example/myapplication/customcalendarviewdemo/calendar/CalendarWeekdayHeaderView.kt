package com.example.myapplication.customcalendarviewdemo.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.myapplication.customcalendarviewdemo.R

/**
 * 日历星期标题栏
 */
class CalendarWeekdayHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //日历属性
    var calendarAttrs: CalendarViewAttrs? = null

    //星期文本绘制工具
    private val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    //单元格宽度
    private var cellWidth: Float = 0f

    //星期标题
    private val weekdays: Array<String> = context.resources.getStringArray(R.array.weekdays)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //计算单元格宽度
        cellWidth = w / 7f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calendarAttrs?.let { attrs ->
            //设置文本样式
            weekdayPaint.textSize = attrs.weekdayTextSize
            weekdayPaint.color = attrs.weekdayTextColor

            //绘制星期行
            val weekdayY =
                height / 2f + (weekdayPaint.descent() - weekdayPaint.ascent()) / 2f - weekdayPaint.descent()
            for (i in 0 until 7) {
                canvas.drawText(
                    weekdays[i],
                    cellWidth * i + cellWidth / 2f,
                    weekdayY,
                    weekdayPaint
                )
            }
        }
    }

}