package com.example.myapplication.customcalendarviewdemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.customcalendarviewdemo.calendar.CustomCalendarView
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var calendarView: CustomCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        calendarView = findViewById<CustomCalendarView?>(R.id.calendar_view).apply {
            onDateChangedListener = { year, month, day ->

            }
            onDateSelectedListener = { year, month, day ->
                val monthName = getMonthName(month)
                Toast.makeText(
                    this@MainActivity,
                    "$year 年 $monthName 月 $day 日",
                    Toast.LENGTH_SHORT
                ).show()
            }

            //设置默认选中今天
            val today = Calendar.getInstance()
            setDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        }

    }

    //获取月份名称
    private fun getMonthName(month: Int): String {
        return when (month) {
            0 -> "一月"
            1 -> "二月"
            2 -> "三月"
            3 -> "四月"
            4 -> "五月"
            5 -> "六月"
            6 -> "七月"
            7 -> "八月"
            8 -> "九月"
            9 -> "十月"
            10 -> "十一月"
            11 -> "十二月"
            else -> "${month + 1}月"
        }
    }
}