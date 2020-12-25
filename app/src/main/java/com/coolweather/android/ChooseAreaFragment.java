package com.coolweather.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.gson.location;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private static final String TAG = "ChooseAreaFragment";
    private String locationId = null;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private TextView where_note;//定位提示
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();


    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;

    private City selectedCity;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        where_note = (TextView) view.findViewById(R.id.textView_where);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        where_note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WeatherActivity.class);
                intent.putExtra("weather_id", locationId);    // 向intent传入WeatherId
                startActivity(intent);
                getActivity().finish();
            }
        });
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 设置 ListView 和 Button 的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof WeatherActivity) {   // 判断碎片的位置
                        //该碎片在WeatherActivity中，只需要刷新该活动
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    } else if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        Log.d(TAG, "onItemClick: 天气ID：" + weatherId);
                        intent.putExtra("weather_id", weatherId);    // 向intent传入WeatherId
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        getLocation();
        queryProvinces();
    }

    //wd 纬度
    public void updateVersion(String wd, String jd) {
        //String path = "http://api.map.baidu.com/geocoder?output=json&location=" + wd + "," + jd + "&key=D84437bf543658642f2285faea183e29";//
        String path = "https://search.heweather.net/find?location=" + jd + "&location=" + wd + "&mode=group=cn&number=1&equal&key=f4b5ef199cff467a82e0298bff93b196";

        Log.d(TAG, "经纬度: " + path);
        HttpUtil.sendOkHttpRequest(path, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgeressDialog();
                        Toast.makeText(getContext(), "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                final location location = new Gson().fromJson(responseText, location.class);


                Log.d(TAG, String.valueOf(responseText));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        where_note.setText("当前定位城市：" + location.getHeWeather6().get(0).getBasic().get(0).getLocation());
                        locationId = location.getHeWeather6().get(0).getBasic().get(0).getCid();

                    }
                });
            }
        });
    }


    public void getLocation() {
        String locationProvider = null;
        boolean isGps = false; //判断GPS定位是否启动
        boolean isNetwork = false; //判断网络定位是否启动
        //获取地理位置管理器
        Context context = getContext();
        LocationManager locationManager = null;
        if (context != null) {
            locationManager
                    = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                //通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
                isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (isGps) {
                    Log.d(TAG, "GPS定位");
                    locationProvider = LocationManager.GPS_PROVIDER;
                } else {
                    Log.d(TAG, "网络定位");
                    //通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）

                    locationProvider = LocationManager.NETWORK_PROVIDER;
                }
            }
            Log.d(TAG, String.valueOf(isGps));
            Log.d(TAG, String.valueOf(isNetwork));
            if (isGps == false && isNetwork == false) {
                where_note.setText("没有开启定位功能，请手动选择");
                Toast.makeText(context, "请打开定位功能", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        //获取Location
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(in
            //   t requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getContext(), "没有位置权限", Toast.LENGTH_SHORT).show();
            where_note.setText("没有定位权限，请手动选择");
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        while (location == null) {
            Log.d(TAG, "数据为空");
            locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
            break;
        }
        if (location != null) {
            //不为空,显示地理位置经纬度
            showLocation(location);
        } else {
            where_note.setText("定位失败，请手动定位");
        }

    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            //如果位置发生变化,重新显示
            showLocation(location);
        }
    };

    /**
     * 显示地理位置经度和纬度信息
     *
     * @param location
     */
    private void showLocation(Location location) {
        String locationStr = "纬度：" + location.getLatitude() + "\n"
                + "经度：" + location.getLongitude();
        updateVersion(location.getLatitude() + "", location.getLongitude() + "");

    }


    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgeressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgeressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void closeProgeressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
