package com.example.myapplication.customcalendarviewdemo.calendar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.customcalendarviewdemo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarViewAttrs(
    var titleTextSize: Float = 18f,
    var titleTextColor: Int = Color.BLACK,
    var weekdayTextSize: Float = 14f,
    var weekdayTextColor: Int = Color.GRAY,
    var dayTextSize: Float = 14f,
    var dayTextColor: Int = Color.BLACK,
    var dayBgColor: Int = Color.WHITE,
    var selectedDayBackgroundColor: Int = Color.rgb(255, 192, 203),//浅粉色
    var todayTextColor: Int = Color.BLACK,
    var selectedBorderWidth: Float = 2f,
    var cellHeight: Float = 45f,
    var headerHeight: Float = 50f,
    var weekdayRowHeight: Float = 32f,
)

/**
 * 自定义日历View - 主容器
 */
class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    //视图属性
    private val attrs = CalendarViewAttrs()

    private val titleView: CalendarTitleView

    private val weekdayHeaderView:CalendarWeekdayHeaderView

    //日历页面
    private val calendarPager: ViewPager2

    //当前显示日期
    private var currentDate: Calendar = Calendar.getInstance()

    //选中的日期
    private var selectedDate: Calendar? = null

    //日期变化回调
    var onDateChangedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

    //日期选择回调
    var onDateSelectedListener: ((year: Int, month: Int, day: Int) -> Unit)? = null

    init {
        //解析XML属性
        parseAttributes(attrs)

        // 初始化标题栏
        titleView = CalendarTitleView(context).apply {
            calendarAttrs = this@CustomCalendarView.attrs
            setOnPreviousMonthClickListener { navigateToPreviousMonth() }
            setOnNextMonthClickListener { navigateToNextMonth() }
        }
        addView(titleView)

        //初始化星期标题
        weekdayHeaderView = CalendarWeekdayHeaderView(context).apply {
            calendarAttrs = this@CustomCalendarView.attrs
        }
        addView(weekdayHeaderView)

        // 初始化ViewPager
        calendarPager = ViewPager2(context).apply {
            adapter = CalendarPagerAdapter().apply {
                setHasStableIds(true)
                //限制预加载页面数量，减少内存使用
                offscreenPageLimit = 1
            }
            // 设置初始页为一个很大的值的中间，实现无限滑动
            val middlePosition = Int.MAX_VALUE / 2
            setCurrentItem(middlePosition, false)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val monthDiff = position - (Int.MAX_VALUE / 2)
                    updateDisplayDate(monthDiff)
                }
            })
            setPageTransformer(MarginPageTransformer(100))
        }
        addView(calendarPager)

        // 更新标题
        updateTitleView()
    }

    /**
     * 解析XML属性
     */
    private fun parseAttributes(attrs: AttributeSet?) {
        if (attrs == null) return
        var typedArray:TypedArray? =null
        try {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomCalendarView)
            with(this.attrs) {
                titleTextSize = typedArray.getDimension(R.styleable.CustomCalendarView_titleTextSize, titleTextSize)
                titleTextColor = typedArray.getColor(R.styleable.CustomCalendarView_titleTextColor, titleTextColor)
                weekdayTextSize = typedArray.getDimension(R.styleable.CustomCalendarView_weekdayTextSize, weekdayTextSize)
                weekdayTextColor = typedArray.getColor(R.styleable.CustomCalendarView_weekdayTextColor, weekdayTextColor)
                dayTextSize = typedArray.getDimension(R.styleable.CustomCalendarView_dayTextSize, dayTextSize)
                dayTextColor = typedArray.getColor(R.styleable.CustomCalendarView_dayTextColor, dayTextColor)
                dayBgColor = typedArray.getColor(R.styleable.CustomCalendarView_dayBgColor, dayBgColor)
                selectedDayBackgroundColor = typedArray.getColor(R.styleable.CustomCalendarView_selectedDayBackgroundColor, selectedDayBackgroundColor)
                todayTextColor = typedArray.getColor(R.styleable.CustomCalendarView_todayTextColor, todayTextColor)
                selectedBorderWidth = typedArray.getDimension(R.styleable.CustomCalendarView_selectedBorderWidth, selectedBorderWidth)
                cellHeight = typedArray.getDimension(R.styleable.CustomCalendarView_cellHeight, cellHeight)
                headerHeight = typedArray.getDimension(R.styleable.CustomCalendarView_headerHeight, headerHeight)
                weekdayRowHeight = typedArray.getDimension(R.styleable.CustomCalendarView_weekdayRowHeight, weekdayRowHeight)
            }
        } finally {
            typedArray?.recycle()
        }
    }

    /**
     * 测量视图大小
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)

        //计算单元格大小为正方形
        val cellSize = width / 7f

        // 计算日历总高度 = 标题高度 + 星期行高度 + 6行日期高度(最多6行)
        val totalHeight = attrs.headerHeight + attrs.weekdayRowHeight + (6 * cellSize)

        // 测量标题栏
        val titleWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val titleHeightSpec = MeasureSpec.makeMeasureSpec(attrs.headerHeight.toInt(), MeasureSpec.EXACTLY)
        titleView.measure(titleWidthSpec, titleHeightSpec)

        //测量星期标题
        val weekdayHeaderWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val weekdayHeaderHeightSpec = MeasureSpec.makeMeasureSpec(attrs.weekdayRowHeight.toInt(),MeasureSpec.EXACTLY)
        weekdayHeaderView.measure(weekdayHeaderWidthSpec,weekdayHeaderHeightSpec)

        // 测量日历页面
        val pagerWidth = width
        // 使用6行正方形单元格的高度
        val pagerHeight = (cellSize * 6).toInt()
        val pagerWidthSpec = MeasureSpec.makeMeasureSpec(pagerWidth, MeasureSpec.EXACTLY)
        val pagerHeightSpec = MeasureSpec.makeMeasureSpec(pagerHeight, MeasureSpec.EXACTLY)
        calendarPager.measure(pagerWidthSpec, pagerHeightSpec)

        setMeasuredDimension(width, totalHeight.toInt())
    }




    /**
     * 布局子视图
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        // 布局标题栏
        titleView.layout(0, 0, titleView.measuredWidth, titleView.measuredHeight)

        //星期标题栏
        weekdayHeaderView.layout(
            0,
            titleView.measuredHeight,
            weekdayHeaderView.measuredWidth,
            titleView.measuredHeight + weekdayHeaderView.measuredHeight
        )

        // 布局日历页面
        calendarPager.layout(
            0,
            titleView.measuredHeight + weekdayHeaderView.measuredHeight,
            calendarPager.measuredWidth,
            titleView.measuredHeight + weekdayHeaderView.measuredHeight + calendarPager.measuredHeight
        )
    }

    /**
     * 更新标题视图
     */
    private fun updateTitleView() {
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)

        // 更新标题：yyyy年MM月
        val dateFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINESE)
        val title = dateFormat.format(currentDate.time)
        titleView.setTitle(title)

        // 通知日期变化
        onDateChangedListener?.invoke(year, month, currentDate.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * 导航到上个月
     */
    private fun navigateToPreviousMonth() {
        calendarPager.currentItem -= 1
    }

    /**
     * 导航到下个月
     */
    private fun navigateToNextMonth() {
        calendarPager.currentItem += 1
    }

    fun navigateToToday() {
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val day = today.get(Calendar.DAY_OF_MONTH)

        setDate(year, month, day)

        calendarPager.setCurrentItem(Int.MAX_VALUE/2,true)

        onDateSelectedListener?.invoke(year, month, day)

    }

    /**
     * 基于当前位置更新显示日期
     */
    private fun updateDisplayDate(monthDiff: Int) {
        currentDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, monthDiff)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        updateTitleView()
    }

    /**
     * 日历适配器类
     */
    private inner class CalendarPagerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CalendarPagerAdapter.CalendarViewHolder>() {

        inner class CalendarViewHolder(val calendarMonthView: CalendarMonthView) : androidx.recyclerview.widget.RecyclerView.ViewHolder(calendarMonthView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
            val calendarMonthView = CalendarMonthView(context).apply {
                calendarAttrs = this@CustomCalendarView.attrs
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )

                setOnDayClickListener { year, month, day ->
                    //更新选中日期
                    selectedDate = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    onDateSelectedListener?.invoke(year, month, day)
                }
            }
            return CalendarViewHolder(calendarMonthView)
        }

        override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
            // 计算相对于今天的月份差异
            val monthDiff = position - (Int.MAX_VALUE / 2)

            // 创建日历对象并调整月份
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthDiff)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            // 设置当前视图月份
            holder.calendarMonthView.setMonthData(
                currentYear,
                currentMonth
            )

            //检查是否有选中日期，以及是否匹配当前月份
            selectedDate?.let { selected ->
                if (selected.get(Calendar.YEAR) == currentYear &&
                    selected.get(Calendar.MONTH) == currentMonth
                ) {
                    holder.calendarMonthView.setSelectedDay(selected.get(Calendar.DAY_OF_MONTH))
                } else {
                    holder.calendarMonthView.clearSelectedDay()
                }
            } ?: holder.calendarMonthView.clearSelectedDay()
        }

        override fun getItemCount(): Int = Int.MAX_VALUE

        /**
         * 添加这个方法防止视图状态丢失
         */
        override fun getItemId(position: Int): Long {
            //计算相对于今天的月份差异
            val monthDiff = position - (Int.MAX_VALUE / 2)
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthDiff)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            //使用年和月份生成唯一ID
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            return (year.toLong() * 100 )+ month
        }
    }

    /**
     * 设置当前日期
     */
    fun setDate(year: Int, month: Int, day: Int) {
        currentDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

        //更新选中日期
        selectedDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }

        // 更新标题
        updateTitleView()

        // 重新加载当前页
        (calendarPager.adapter as CalendarPagerAdapter).notifyDataSetChanged()
    }

}