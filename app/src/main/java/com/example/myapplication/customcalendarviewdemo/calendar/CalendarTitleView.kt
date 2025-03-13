package com.example.myapplication.customcalendarviewdemo.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

class CalendarTitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    //日历属性
    var calendarAttrs: CalendarViewAttrs? = null

    //标题文本
    private var title: String = ""

    //绘制工具
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arrowPath = Path()

    //按钮区域
    private val prevButtonRect = RectF()
    private val nextButtonRect = RectF()

    //点击回调
    var onPreviousMonthClickListener: (() -> Unit)? = null
    var onNextMonthClickListener: (() -> Unit)? = null

    //手势检测器
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                when {
                    prevButtonRect.contains(e.x, e.y) -> {
                        onPreviousMonthClickListener?.invoke()
                        return true
                    }

                    nextButtonRect.contains(e.x, e.y) -> {
                        onNextMonthClickListener?.invoke()
                        return true
                    }
                }
                return false
            }
        })

    init {
        //初始化绘制工具
        titlePaint.textAlign = Paint.Align.CENTER
        buttonPaint.style = Paint.Style.STROKE
        buttonPaint.strokeWidth = 2f
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String) {
        this.title = title
        invalidate()
    }

    @JvmName("setOnPreviousMonthClickListenerJava")
    fun setOnPreviousMonthClickListener(listener: () -> Unit) {
        onPreviousMonthClickListener = listener
    }

    @JvmName("setOnNextMonthClickListenerJava")
    fun setOnNextMonthClickListener(listener: () -> Unit) {
        onNextMonthClickListener = listener
    }

    /**
     * 处理触摸事件
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) or super.onTouchEvent(event)
    }

    /**
     * 绘制视图
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        calendarAttrs?.let { attrs->
            //设置标题文本样式
            titlePaint.textSize = attrs.titleTextSize/* * resources.displayMetrics.density*/
            titlePaint.color = attrs.titleTextColor

            //设置按钮样式
            buttonPaint.color = attrs.titleTextColor

            //计算标题位置
            val centerX = width / 2f
            val centerY =
                height / 2f + (titlePaint.descent() - titlePaint.ascent()) / 2f - titlePaint.descent()

            //绘制标题
            canvas.drawText(title, centerX, centerY, titlePaint)

            //计算并绘制上个月按钮
            val buttonSize = height * 0.4f
            prevButtonRect.set(
                height/2f - buttonSize/2f,
                height/2f - buttonSize/2f,
                height/2f + buttonSize/2f,
                height/2f + buttonSize/2f,
            )

            //绘制上个月箭头
            arrowPath.reset()
            arrowPath.moveTo(prevButtonRect.centerX()+buttonSize/4f, prevButtonRect.top)
            arrowPath.lineTo(prevButtonRect.centerX()-buttonSize/4f,prevButtonRect.centerY())
            arrowPath.lineTo(prevButtonRect.centerX()+buttonSize/4f,prevButtonRect.bottom)
            canvas.drawPath(arrowPath,buttonPaint)

            //计算并绘制下个月按钮
            nextButtonRect.set(
                width - height / 2f - buttonSize / 2f,
                height / 2f - buttonSize / 2f,
                width - height / 2f + buttonSize / 2f,
                height / 2f + buttonSize / 2f
            )

            //绘制下个月箭头
            arrowPath.reset()
            arrowPath.moveTo(nextButtonRect.centerX() - buttonSize / 4f, nextButtonRect.top)
            arrowPath.lineTo(nextButtonRect.centerX() + buttonSize / 4f, nextButtonRect.centerY())
            arrowPath.lineTo(nextButtonRect.centerX() - buttonSize / 4f, nextButtonRect.bottom)
            canvas.drawPath(arrowPath,buttonPaint)

        }
    }

}