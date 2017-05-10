package com.hyunseok.android.parking;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hyunseok.android.parking.domain.Park;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Remote.Callback {

    private GoogleMap mMap;
    // Open API url
    private String hangeulParam = "";
    private String APIurl = "http://openapi.seoul.go.kr:8088/575446554e6c6f7236306247414f6a/json/SearchParkingInfo/1/1000/";
    ProgressDialog progressDialog;
    // TODO SPINNER 추가해서 '구'단위별로 보이게 해보기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 공영 주차장 마커 전체를 화면에 출력
        // 한글 인코딩
        try {
            hangeulParam = URLEncoder.encode("중구", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        APIurl = APIurl + hangeulParam;
        progressDialog = new ProgressDialog(this);
        Remote remote = new Remote();
        remote.getData(this);

        // 중심점을 서울로 이동
        LatLng seoul = new LatLng(37.566696, 126.977942); // 서울시청
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 13));
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public String getUrl() {
        return APIurl;
    }

    @Override
    public void call(String jsonString) {
        // MainActivity의 화면에 뭔가를 세팅해주면, Remote에서 이 함수를 호출해준다.
        try {
            // json 데이터는 Object 와 Array 이 두개로만 이루어짐.
            // 1. json String 전체를 JSONObject 로 변환한다.
            JSONObject jsonObject = new JSONObject(jsonString);
            // 2. JSONObject 중에 최상위의 Object 를 꺼낸다.
            JSONObject rootObject = jsonObject.getJSONObject("SearchParkingInfo");
            // 3. 사용하려는 정보들을 JSONArray 로 꺼낸다.
            //    이 데이터는 rootObject 바로 아래에 실제 정보가 있지만 계층구조상 더 아래에 존재할 수도 있음.
            JSONArray rows = rootObject.getJSONArray("row");
            int arrayLength = rows.length();

            List<String> parkCodes = new ArrayList<>();
            for (int i = 0; i < arrayLength; i++) {
                JSONObject park = rows.getJSONObject(i);

                // 주차장 코드 중복검사
                String code = park.getString("PARKING_CODE");
                if(parkCodes.contains(code)){
                    continue; // 여기서 아래 로직을 실행하지 않고 for문 상단으로 이동
                }
                parkCodes.add(code);

                double lat = getDouble(park, "LAT");
                double lng = getDouble(park, "LNG");
                LatLng parkinglot = new LatLng(lat, lng);

                int capacity = getInt(park, "CAPACITY");
                int current = getInt(park, "CUR_PARKING");
                int space = capacity - current;

                mMap.addMarker(new MarkerOptions().position(parkinglot).title(space + " / " + capacity)).showInfoWindow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    @Override
    public ProgressDialog getProgress(){
        return progressDialog;
    }

    // 값이 없을 경우 0으로 세팅 해준다.
    private double getDouble(JSONObject obj, String key) {
        double result = 0;
        try {
            result = obj.getDouble(key);
        } catch (Exception e) {

        }
        return result;
    }

    private int getInt(JSONObject obj, String key) {
        int result = 0;
        try {
            result = obj.getInt(key);
        } catch (Exception e) {

        }
        return result;
    }
}
