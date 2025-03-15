package com.example.myapplication.customcalendarviewdemo.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * 日历单元格视图
 */
class CalendarDayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 日期信息
    var dayInfo: CalendarMonthView.DayInfo? = null
    
    // 是否被选中
    @get:JvmName("isDaySelected")
    @set:JvmName("setDaySelected")
    var isSelected: Boolean = false

    // 是否是今天
    var isToday: Boolean = false
    
    // 日历属性
    var calendarAttrs: CalendarViewAttrs? = null
    
    // 绘制工具
    private val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedDayBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val todayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dayBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        // 初始化绘制工具
        dayTextPaint.textAlign = Paint.Align.CENTER
        selectedDayBgPaint.style = Paint.Style.FILL
        todayPaint.style = Paint.Style.STROKE
        dayBgPaint.style = Paint.Style.FILL
    }
    
    /**
     * 绘制视图
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        dayInfo?.let { info ->
            // 如果不是当前月份，则不绘制任何内容
            if (!info.isCurrentMonth) return
            
            calendarAttrs?.let { attrs ->
                // 设置日期文本样式
                dayTextPaint.textSize = attrs.dayTextSize /** resources.displayMetrics.density*/
                dayTextPaint.color = attrs.dayTextColor
                dayBgPaint.color = attrs.dayBgColor

                // 设置选中日期样式
                selectedDayBgPaint.color = attrs.selectedDayBackgroundColor
                
                // 设置今天样式
                todayPaint.color = attrs.todayTextColor
                todayPaint.strokeWidth = attrs.selectedBorderWidth

                // 计算正方形的大小和位置（居中，并留出一点边距）
                val padding = min(width, height) * 0.05f
                val size = min(width, height) - (2 * padding)
                val left = (width - size) / 2f
                val top = (height - size) / 2f

                // 绘制单元格背景（如果被选中）
                if (isSelected) {
                    canvas.drawRoundRect(
                        left,
                        top,
                        left + size,
                        top + size,
                        size * 0.2f,
                        size * 0.2f,
                        selectedDayBgPaint
                    )
                } else {
                    canvas.drawRoundRect(
                        left,
                        top,
                        left + size,
                        top + size,
                        size * 0.2f,
                        size * 0.2f,
                        dayBgPaint
                    )
                }
                
                // 绘制今天的边框
                if (isToday) {
                    canvas.drawRoundRect(
                        left + attrs.selectedBorderWidth / 2,
                        top + attrs.selectedBorderWidth / 2,
                        left + size - attrs.selectedBorderWidth / 2,
                        top + size - attrs.selectedBorderWidth / 2,
                        size * 0.2f,
                        size * 0.2f,
                        todayPaint
                    )
                }
                
                // 绘制日期文本
                val x = width / 2f
                val y = height / 2f + (dayTextPaint.descent() - dayTextPaint.ascent()) / 2f - dayTextPaint.descent()
                canvas.drawText(info.day.toString(), x, y, dayTextPaint)
            }
        }
    }
}
