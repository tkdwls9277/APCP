package com.example.apcp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GpsTracker extends Service implements LocationListener {
    private Location location;
    private LocationManager locationManager;
    private Context context;

    private double latitude;
    private double longitude;

    private String deviceName;

    public GpsTracker () { // default constructor
    }
    public GpsTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public Location getLocation() {
        Log.e("getLocation", "yes");
        try {
            // 수정 전 locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) { // 위치 정보 제공자 확인
            } else {
                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                Log.e("FINE_LOCATION", "" + hasFineLocationPermission);
                Log.e("COARSE_LOCATION", "" + hasCoarseLocationPermission);

                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                        hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { // 권한 확인
                } else {

                    return null;
                }

                if (isNetworkEnabled) {
                    Log.e("GpsTracker", "isNetworkEnabled");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 , 0, this);

                    if (locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null)
                        {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e("getLocation", "latitude : " + latitude + ", longitude : " + longitude);
                        }
                    }
                }

                if (isGPSEnabled) // GPS 이용
                {
                    Log.e("GpsTracker", "isGPSEnabled");
                    if (location == null)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 , 0, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 최근 위치 확인
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.e("getLocation", "latitude : " + latitude + ", longitude : " + longitude);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e("Exception" , "" + e.toString());
        }

        // locationManager.removeUpdates(GpsTracker.this); // 위치 정보 갱신 종료

        return location;
    }

    @Override
    public void onCreate() { // 서비스가 생성될 때 실행(한 번만)
        super.onCreate();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 수정 후
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        String id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); // 단말기 고유 번호
        // 값이 -1일 경우 값 전달이 안된 것임

        DataFromServer data = getDataFromDb();

        double temp = data.getTemp();
        double humi = data.getHumi();
        double dust = data.getDust();
        double co = data.getCo();

        getLocation();

        sendData(id, temp, humi, dust, co, latitude, longitude);

        // Looper.myLooper() == Looper.getMainLooper() 현재 쓰레드가 메인쓰레드인지 확인하는 코드, true 일 시 메인쓰레드
        boolean thread = Looper.myLooper() == Looper.getMainLooper();
        Log.e("onStartCommand", "" + thread);

        // ServerCommunication sc = new ServerCommunication();  잠깐 보류
        // sc.sendData(dust, co, co2, latitude, longitude);

        return START_STICKY;
    }
    @Override
    public void onLocationChanged(Location location) {
        class LocationTask extends AsyncTask<Location, Void, Void> {
            private Context context;

            public LocationTask(Context context) {
                this.context = context;
            }

            @Override
            protected Void doInBackground(Location... params) {

                String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID); // 에러 날 수도..

                DataFromServer data = getDataFromDb();
                double temp = data.getTemp();
                double humi = data.getHumi();
                double dust = data.getDust();
                double co = data.getCo();
                latitude = params[0].getLatitude();
                longitude = params[0].getLongitude();

                Log.e("DataFromDb", "temp : " + temp + ", humi : " + humi + ", dust : " + dust + ", co : " + co);
                sendData(id, temp, humi, dust, co, latitude, longitude);

                Log.e("latitude", "" + latitude);
                Log.e("longitude", "" + longitude);
                return null;
            }
        }
        DBHelper dbHelper = new DBHelper(this);
        BluetoothService bs = new BluetoothService(context);
        bs.connectDevice(dbHelper.getDeviceName(), this);

        LocationTask lTask = new LocationTask(this); // 매개변수로 dust, co, co2 값들을 AsyncTask로 넘겨주자
        lTask.execute(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public double getLatitude() {
        if(location != null)
        {
            latitude = location.getLatitude();
        }
        Log.e("latitude", "" + latitude);
        return latitude;
    }

    public double getLongitude()
    {
        if(location != null)
        {
            longitude = location.getLongitude();
        }
        Log.e("longitude", "" + longitude);
        return longitude;
    }

    public void stopUsingGPS() // 위치 정보 갱신 종료하는 메소드
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(GpsTracker.this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(GpsTracker.this);
        Toast.makeText(this, "서비스 종료", Toast.LENGTH_LONG).show();
        Log.e("GpsTracker", "onDestroy");
    }

    protected void sendData(final String id, final double temp, final double humi, final  double dust,
                            final double co, final double latitude, final double longitude) { // GpsTracker 서비스에서 이 메소드를 사용할 듯
        Log.e("ServerCommunication", "sendData");
        class SendDataTask extends AsyncTask <Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String param = "id="+id+"&temp="+temp+"&humi="+humi+"&dust="+dust+"&co="+co+"&latitude="+latitude+"&longitude="+longitude;
                    Log.e("param", param);

                    URL url = new URL( ServerCommunication.IP + "/insert2.php");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(5000); // 5초
                    httpURLConnection.setConnectTimeout(5000); // 웹과 접속을 하고 답변을 받는 데 응답이이 없으면 5초 뒤에 끊는 다는 것
                    httpURLConnection.setRequestMethod("POST"); // POST 방식으로
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.connect();

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(param.getBytes("UTF-8"));
                    outputStream.flush();
                    outputStream.close();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    }
                    else{
                        inputStream = httpURLConnection.getErrorStream();
                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    while((line = bufferedReader.readLine()) != null){
                        sb.append(line);
                        Log.e("sendData", line);
                    }


                    bufferedReader.close();

                    httpURLConnection.disconnect();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("sendDataException", "" + e);
                }
                return null;
            }
        }
        SendDataTask sendDataTask = new SendDataTask();
        sendDataTask.execute();
    }

    public DataFromServer getDataFromDb () {
        DataFromServer result = new DataFromServer();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_senser",null);
        cursor.moveToFirst();

        Log.e("cursor", "" + cursor.getPosition());
        /*
        result.setTemp(Double.parseDouble(cursor.getString(1)));
        result.setHumi(Double.parseDouble(cursor.getString(2)));
        result.setDust(Double.parseDouble(cursor.getString(3)));
        result.setCo(Double.parseDouble(cursor.getString(4)));
*/

        while (cursor.moveToNext()){
            Log.e("getDataFromDb", "" + cursor.getInt(0) + ", " + cursor.getString(1) + ", " + cursor.getString(2)
                    + ", " + cursor.getString(3) + ", " + cursor.getString(4));
        }
        db.close();

        return result;
    }

}
