package com.coolweather.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Music;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.location;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private LinearLayout layout_tq;
    private LinearLayout layout_news;
    private LinearLayout layout_map;
    private BottomNavigationView bottomNavigation;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button day_button;
    private Button btn_tjgq;
    private String weatherId;
    private String cityName;
    private String degree;
    private WebView map_webView;
    private WebView news_webView;
    private String musicurl;
    private Intent intent;


    private  String channelId="1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decoorView = getWindow().getDecorView();
            decoorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        //初始化各组件
        bingPicImg = findViewById(R.id.bing_pic_img);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);   // 设置下拉刷新进度条的颜
        //初始化各组件
        news_webView = findViewById(R.id.news_webview);
        map_webView = findViewById(R.id.map_webview);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(this);
        day_button=findViewById(R.id.day_button);
        day_button.setOnClickListener(this);
        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        layout_tq = findViewById(R.id.layout_tq);
        layout_news = findViewById(R.id.layout_news);
        layout_map = findViewById(R.id.layout_map);
        btn_tjgq = findViewById(R.id.btn_tjgq);
        btn_tjgq.setOnClickListener(this);
/*        btn_tjgq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WeatherActivity.this, MusicActivity.class);
                startActivity(intent);
            }
        });*/

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.main_wallpaper:
                        layout_tq.setVisibility(View.VISIBLE);
                        layout_news.setVisibility(View.GONE);
                        layout_map.setVisibility(View.GONE);
                        break;
                    case R.id.main_news:
                        layout_tq.setVisibility(View.GONE);
                        layout_news.setVisibility(View.VISIBLE);
                        layout_map.setVisibility(View.GONE);
                        break;
                    case R.id.main_map:
                        layout_tq.setVisibility(View.GONE);
                        layout_news.setVisibility(View.GONE);
                        layout_map.setVisibility(View.VISIBLE);
                        map_webView.reload();
                        break;
                }
                return true;
            }
        });


        /**
         * 加载新闻webview网页
         */
        //让WebView支持JavaScript脚本
        news_webView.getSettings().setJavaScriptEnabled(true);
        //自适应屏幕
        news_webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        news_webView.getSettings().setLoadWithOverviewMode(true);

        news_webView.loadUrl("https://i.ifeng.com/?ch=ifengweb_2014");
        news_webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        /**
         * 加载疫情webview网页
         */
        map_webView.getSettings().setJavaScriptEnabled(true);//让WebView支持JavaScript脚本
        //自适应屏幕
        map_webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        map_webView.getSettings().setLoadWithOverviewMode(true);
        map_webView.loadUrl("https://news.qq.com/zt2020/page/feiyan.htm#/area?pool=bj");  //后面更改
        map_webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //使用webview加载网页
                view.loadUrl(url);
                return true;
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询数据
            String weatherId = getIntent().getStringExtra("weather_id");
            //String cityName=getIntent().getStringExtra("cityName");

            weatherLayout.setVisibility(View.INVISIBLE);    // 暂时将ScrollView设为不可见
            requestWeather(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {   // 设置下拉刷新监听器
                requestWeather(weatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

    }
/*
    private void getTuiJianMusic(String wd) {
        String path = "http://weather.qkeke.com/?wd=" + wd;
        HttpUtil.sendOkHttpRequest(path, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                final Music music = new Gson().fromJson(responseText, Music.class);

                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        musicurl = "https://music.163.com/#/song?id=" + music.getId();
                        TextView te = findViewById(R.id.musictext);
                        te.setText(music.getName() + " - " + music.getArtist());
                    }
                });
            }
        });
    }*/

    /**
     * 根据天气Id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {

       String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=8e7ec84410bd4be8b5f6130a319103d0"; // 这里的key设置为第一个实训中获取到的API Key
       // String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        // 组装地址并发出请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);   // 将返回数据转换为Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //缓存有效的weather对象(实际上缓存的是字符串)
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            weather.basic.weatherId = weatherId;
                            showWeatherInfo(weather);   // 显示内容
                        } else {
                            //Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);    // 表示刷新事件结束并隐藏刷新进度条
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }


    /**
     * 处理并展示Weather实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        // 从Weather对象中获取数据
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1]; //按24小时计时的时间
        String degree = weather.now.temperature + "°C";
        this.degree = weather.now.temperature;
      /*  getTuiJianMusic(this.degree);*/
        String weatherInfo = weather.now.more.info;
        // 将数据显示到对应控件上
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {    // 循环处理每天的天气信息
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            // 加载布局
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            // 设置数据
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            // 添加到父布局
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: " + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);  // 将天气信息设置为可见
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_button:
                drawerLayout.openDrawer(GravityCompat.START);
                //获取NotificationManager实例
                Intent intent =new Intent(this,WeatherActivity.class);
                PendingIntent pi=PendingIntent.getActivity(this,0,intent,0);
                NotificationManager manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, "name", NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(notificationChannel);

                }
                Notification notification = new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("2020-12-25 天气")
                        .setContentText("今天有雨，记得出门带伞噢")
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ww)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ww))
                        .setAutoCancel(true)
                        .build();
                manager.notify(1, notification);
                notification.defaults = Notification.DEFAULT_ALL;
                break;


            case R.id.day_button:
                 intent=new Intent(WeatherActivity.this, DayActivity.class);
                startActivity(intent);
                break;


            case R.id.btn_tjgq:
                Intent intent1 = new Intent();
                intent1.setClass(WeatherActivity.this, MusicActivity.class);
                startActivity(intent1);
                //从当前的首页跳转到列表页
                break;
            default:
                break;
        }
    }
}
