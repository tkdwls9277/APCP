package com.example.apcp;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ActivityForGoogleAPITest extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        onCreate 메소드에서 getMapAsync() 메소드를 호출하여 GoogleMap 객체가 준비될 때 실행될 콜백을 등록합니다.
        그러기 위해서는 레이아웃에 추가했던 프래그먼트(com.google.android.gms.maps.MapFragment)의 핸들을 가져와야 합니다.
        FragmentManager.findFragmentById 메소드를 사용하여 지정한 ID를 갖는 프래그먼트의 핸들을 가져옵니다
        getMapAsync() 메소드가 메인 쓰레드에서 호출되어야 메인스레드에서 onMapReady 콜백이 실행됩니다.
        */
    }

    @Override
    public void onMapReady(final GoogleMap map) { // 맵이 사용할 준비가 되었을 때(=NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때) 호출되어지는 메소드

        getData(map);

        LatLng GNU = new LatLng(35.153391, 128.099468);

        /*
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(GNU); // 마커가 표시될 위치(position),
        markerOptions.title("진주"); // 마커에 표시될 타이틀(title),
        markerOptions.snippet("국립 경상대학교"); // 마커 클릭시 보여주는 간단한 설명(snippet)를 설정하고
        map.addMarker(markerOptions); // addMarker 메소드로 GoogleMap 객체에 추가해주면 지도에 표시
        */

        map.moveCamera(CameraUpdateFactory.newLatLng(GNU)); // 카메라를 지정한 경도, 위도로 이동
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        // 지정한 단계로 카메라 줌을 조정
        // 1 단계로 지정하면 세계지도 수준으로 보이고 숫자가 커질수록 상세지도
    }

    protected void getData(final GoogleMap map) { // 지도 액티비티에서 이 메소드 사용할 듯

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
