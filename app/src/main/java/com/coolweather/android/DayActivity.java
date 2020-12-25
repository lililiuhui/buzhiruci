package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

public class DayActivity extends AppCompatActivity {
    CalendarView calendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        calendarView = (CalendarView) findViewById(R.id.calenderView);
        //calendarView 监听事件
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange( CalendarView view, int year, int month, int dayOfMonth) {
                //显示用户选择的日期
                Toast.makeText(DayActivity.this,year + "年" + month + "月" + dayOfMonth + "日",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
