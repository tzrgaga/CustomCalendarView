package com.example.myapplication.customcalendarviewdemo.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import java.util.Calendar

/**
 * 日历月视图
 */
class CalendarMonthView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    // 日历属性
    var calendarAttrs: CalendarViewAttrs? = null
    
    // 当前年月
    private var year: Int = 0
    private var month: Int = 0
    
    // 选中的日期
    private var selectedDay: Int = -1
    
    // 今天的日期
    private val today = Calendar.getInstance()
    
    // 日历数据
    private val calendar = Calendar.getInstance()
    private val daysInMonth = mutableListOf<DayInfo>()
    
    // 日期单元格视图
    private val dayCells = mutableListOf<CalendarDayView>()
    
    // 星期文本绘制工具
    private val weekdayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // 单元格大小
    private var cellWidth: Float = 0f
    private var cellHeight: Float = 0f
    
    // 星期标题
    private val weekdays = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    
    // 日期点击回调
    private var onDayClickListener: ((year: Int, month: Int, day: Int) -> Unit)? = null
    
    // 手势检测器
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // 忽略星期行
            if (e.y < (calendarAttrs?.weekdayRowHeight ?: 0f)) return false
            
            // 计算点击的行列
            val row = ((e.y - (calendarAttrs?.weekdayRowHeight ?: 0f)) / cellHeight).toInt()
            val col = (e.x / cellWidth).toInt()
            
            // 计算点击的日期索引
            val index = row * 7 + col
            if (index >= 0 && index < daysInMonth.size) {
                val dayInfo = daysInMonth[index]
                if (dayInfo.isCurrentMonth) {  // 只处理当前月的日期
                    selectedDay = dayInfo.day
                    updateDayCellsState()
                    onDayClickListener?.invoke(year, month, selectedDay)
                    return true
                }
            }
            return false
        }
    })
    
    /**
     * 日期信息类
     */
    data class DayInfo(
        val year: Int,
        val month: Int,
        val day: Int,
        val isCurrentMonth: Boolean
    )
    
    init {
        setWillNotDraw(false)
        // 初始化绘制工具
        weekdayPaint.textAlign = Paint.Align.CENTER
    }
    
    /**
     * 设置月份数据
     */
    fun setMonthData(year: Int, month: Int) {
        this.year = year
        this.month = month

        // 重置选中状态，让调用者通过setSelectedDay设置
        selectedDay = -1
        
        // 生成日历数据
        generateCalendarData()
        
        // 创建或更新日期单元格视图
        updateDayCells()
        
        // 刷新视图
        requestLayout()
        invalidate()
    }
    
    /**
     * 设置选中的日期
     */
    fun setSelectedDay(day: Int) {
        if (selectedDay != day) {
            selectedDay = day
            updateDayCellsState()
        }
    }

    fun clearSelectedDay() {
        if (selectedDay != -1) {
            selectedDay = -1
            updateDayCellsState()
        }
    }
    
    /**
     * 设置日期点击监听器
     */
    fun setOnDayClickListener(listener: (year: Int, month: Int, day: Int) -> Unit) {
        onDayClickListener = listener
    }
    
    /**
     * 处理触摸事件
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
    
    /**
     * 生成日历数据
     */
    private fun generateCalendarData() {
        daysInMonth.clear()
        
        // 设置日历为当前月的第一天
        calendar.set(year, month, 1)
        
        // 获取当月第一天是星期几(0=周日,1=周一,...,6=周六)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        
        // 获取当月的天数
        val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // 为上个月的日期创建空白单元格
        calendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.add(Calendar.MONTH, 1) // 恢复到当前月
        
        for (i in 0 until firstDayOfWeek) {
            val prevDay = daysInPrevMonth - firstDayOfWeek + i + 1
            daysInMonth.add(DayInfo(year, month - 1, prevDay, false))
        }
        
        // 添加当月的日期
        for (i in 1..daysInCurrentMonth) {
            daysInMonth.add(DayInfo(year, month, i, true))
        }
        
        // 填充下个月的空白单元格
        val totalCells = 6 * 7 // 6行，每行7天
        val nextMonthDays = totalCells - daysInMonth.size
        for (i in 1..nextMonthDays) {
            daysInMonth.add(DayInfo(year, month + 1, i, false))
        }
    }
    
    /**
     * 创建或更新日期单元格视图
     */
    private fun updateDayCells() {
        // 确保有足够的日期单元格视图
        while (dayCells.size < daysInMonth.size) {
            val dayCell = CalendarDayView(context).apply {
                calendarAttrs = this@CalendarMonthView.calendarAttrs
            }
            dayCells.add(dayCell)
            addView(dayCell)
        }
        
        // 更新每个日期单元格的信息
        for (i in daysInMonth.indices) {
            val dayCell = dayCells[i]
            val dayInfo = daysInMonth[i]
            
            dayCell.dayInfo = dayInfo
            dayCell.isSelected = dayInfo.day == selectedDay && dayInfo.isCurrentMonth
            dayCell.isToday = dayInfo.year == today.get(Calendar.YEAR) && 
                               dayInfo.month == today.get(Calendar.MONTH) && 
                               dayInfo.day == today.get(Calendar.DAY_OF_MONTH)
        }
    }
    
    /**
     * 更新日期单元格状态
     */
    private fun updateDayCellsState() {
        for (i in daysInMonth.indices) {
            val dayInfo = daysInMonth[i]
            val dayCell = dayCells[i]
            
            dayCell.isSelected = dayInfo.day == selectedDay && dayInfo.isCurrentMonth
            dayCell.invalidate()
        }
    }
    
    /**
     * 视图大小变化时重新计算单元格大小
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // 计算单元格大小
        cellWidth = w / 7f
        cellHeight = (h - (calendarAttrs?.weekdayRowHeight ?: 0f)) / 6f
        
        requestLayout()
    }
    
    /**
     * 测量视图
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        
        // 计算单元格大小
        cellWidth = width / 7f
        cellHeight = (height - (calendarAttrs?.weekdayRowHeight ?: 0f)) / 6f
        
        // 测量日期单元格
        val cellWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth.toInt(), MeasureSpec.EXACTLY)
        val cellHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight.toInt(), MeasureSpec.EXACTLY)
        
        for (dayCell in dayCells) {
            dayCell.measure(cellWidthSpec, cellHeightSpec)
        }
        
        setMeasuredDimension(width, height)
    }
    
    /**
     * 布局子视图
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局日期单元格
        for (i in dayCells.indices) {
            if (i >= daysInMonth.size) break
            
            val row = i / 7
            val col = i % 7
            
            val left = (col * cellWidth).toInt()
            val top = ((calendarAttrs?.weekdayRowHeight ?: 0f) + row * cellHeight).toInt()
            val right = ((col + 1) * cellWidth).toInt()
            val bottom = ((calendarAttrs?.weekdayRowHeight ?: 0f) + (row + 1) * cellHeight).toInt()
            
            dayCells[i].layout(left, top, right, bottom)
        }
    }
    
    /**
     * 绘制视图
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        calendarAttrs?.let { attrs ->
            // 设置星期文本样式
            weekdayPaint.textSize = attrs.weekdayTextSize /** resources.displayMetrics.density*/
            weekdayPaint.color = attrs.weekdayTextColor
            
            // 绘制星期行
            val weekdayY = attrs.weekdayRowHeight / 2f + (weekdayPaint.descent() - weekdayPaint.ascent()) / 2f - weekdayPaint.descent()
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
