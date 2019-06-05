package com.example.apcp;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class Maps extends Fragment implements OnMapReadyCallback
{
    GoogleMap googlemap;

    //위도 경도

    final static double mLatitude = 35.1576822;   //위도
    final static double mLongitude = 128.1002765;  //경도

    public Maps(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_maps, container, false);

        SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        return v;

    }

    //구글맵 생성 콜백
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googlemap = googleMap;

        getData(googlemap);

        LatLng GNU = new LatLng(mLatitude, mLongitude);

        googlemap.moveCamera(CameraUpdateFactory.newLatLng(GNU)); // 카메라를 지정한 경도, 위도로 이동
        googlemap.animateCamera(CameraUpdateFactory.zoomTo(15));
        /*
        //지도타입 - 일반
        this.googlemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //기본위치(63빌딩)
        LatLng position = new LatLng(mLatitude , mLongitude);

        //화면중앙의 위치와 카메라 줌비율
        this.googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));

        onAddMarker();
        */
    }

    //마커 , 원추가
    public void onAddMarker(){
        LatLng position = new LatLng(mLatitude , mLongitude);

        //나의위치 마커
        MarkerOptions mymarker = new MarkerOptions()
                .position(position);   //마커위치

        // 반경 1KM원
        CircleOptions circle1KM = new CircleOptions().center(position) //원점
                .radius(1000)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#880000ff")); //배경색
               // .fillColor(Color.parseColor("#880000ff")); //배경색

        //마커추가
        this.googlemap.addMarker(mymarker);

        //원추가
        this.googlemap.addCircle(circle1KM);
    }

    protected void getData(final GoogleMap map) {

        class GetJSONTask extends AsyncTask<Void, Void, ArrayList<DataFromServer>> {

            BufferedReader bufferedReader = null;

            @Override
            protected ArrayList<DataFromServer> doInBackground(Void... voids) {

                try {
                    URL url = new URL(ServerCommunication.IP + "/getJSON.php");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    JSONObject jsonObject = new JSONObject(sb.toString().trim()); // trim() 은 공백문자 제거하는 메소드
                    JSONArray jsonArray = jsonObject.getJSONArray(ServerCommunication.TAG_RESULTS); // TAG_RESULTS 로 구분하여 JSON 객체들을 저장, ArrayList 처럼

                    ArrayList<DataFromServer> result = new ArrayList<DataFromServer>(); // 서버에서 값을 저장하여 반환할 ArrayList

                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject j = jsonArray.getJSONObject(i);

                        // ID 값에 따른 VALUE 를 구분지어 저장
                        String tempString = j.getString(ServerCommunication.TAG_TEMP);
                        String humiString = j.getString(ServerCommunication.TAG_HUMI);
                        String dustString = j.getString(ServerCommunication.TAG_DUST);
                        String coString = j.getString(ServerCommunication.TAG_CO);
                        String latitudeString = j.getString(ServerCommunication.TAG_LATITUDE);
                        String longitudeString = j.getString(ServerCommunication.TAG_LONGITUDE);

                        // double 형으로 형변환
                        double temp = Double.parseDouble(tempString);
                        double humi = Double.parseDouble(humiString);
                        double dust = Double.parseDouble(dustString);
                        double co = Double.parseDouble(coString);
                        double latitude = Double.parseDouble(latitudeString);
                        double longitude = Double.parseDouble(longitudeString);

                        DataFromServer data = new DataFromServer(temp, humi, dust, co, latitude, longitude); // 값을 저장할 단위 객체
                        result.add(data); // 5개의 값들을 DataFromServer 객체 형태로 저장 (ArrayList)

                    }

                    return result;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return null; // 실패하면 null 값 반환
            }

            @Override
            protected void onPostExecute(ArrayList<DataFromServer> param) {
                super.onPostExecute(param); // param 은 서버에서 받아온 값들을 저장해 놓은 ArrayList

                if (param.size() != 0) { // 서버에 데이터가 하나라도 있을 때때

                    MarkerOptions[] markers = new MarkerOptions[param.size()]; // 마커들
                    LatLng[] lls = new LatLng[param.size()]; // 위도, 경도

                    for (int i = 0; i < param.size(); i++){
                        markers[i] = new MarkerOptions();
                        lls[i] = new LatLng(param.get(i).getLatitude(), param.get(i).getLongitude()); // param 에서 위도, 경도 꺼내서 저장

                        markers[i].position(lls[i]);

                        if (param.get(i).getDust() > 151) {
                            markers[i].title("매우 나쁨");
                        }
                        else if (param.get(i).getDust() <= 150 && param.get(i).getDust() >=81) {
                            markers[i].title("나쁨");
                        }
                        else if (param.get(i).getDust() <= 80 && param.get(i).getDust() >= 31) {
                            markers[i].title("보통");
                        }
                        else {
                            markers[i].title("좋음");
                        }

                        map.addMarker(markers[i]);

                    }
                }
            }
        }

        GetJSONTask getJSONTask = new GetJSONTask();
        getJSONTask.execute();
    }
}








