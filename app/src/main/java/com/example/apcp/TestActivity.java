package com.example.apcp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private double temp;
    private double humi;
    private double dust;
    private double co;

    private EditText editTemp;
    private EditText editHumi;
    private EditText editDust;
    private EditText editCo;
    private Button sendButton;
    private Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        if(!checkLocationServiceStatus()) {
            showDialogForLocationServiceSetting();
            Log.e("권한확인", "" + checkLocationServiceStatus());
        }

        editTemp = findViewById(R.id.temp);
        editHumi = findViewById(R.id.humi);
        editDust = findViewById(R.id.dust);
        editCo = findViewById(R.id.co);
        sendButton = findViewById(R.id.sendButton);
        mapButton = findViewById(R.id.mapButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(editTemp.getText().toString().equals("")||
                        editHumi.getText().toString().equals("")||
                        editDust.getText().toString().equals("")||
                        editCo.getText().toString().equals("")) {
                    Toast.makeText(v.getContext(), "값을 입력하세요", Toast.LENGTH_LONG).show();
                }
                else {
                    temp = Double.parseDouble(editTemp.getText().toString());
                    humi = Double.parseDouble(editHumi.getText().toString());
                    dust = Double.parseDouble(editDust.getText().toString());
                    co = Double.parseDouble(editCo.getText().toString());

                    Intent intent = new Intent(v.getContext(), GpsTracker.class);
                    intent.putExtra("temp", temp);
                    intent.putExtra("humi", humi);
                    intent.putExtra("dust", dust);
                    intent.putExtra("co", co);
                    startService(intent);
                }
            }
        });

        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GpsTracker.class);
                stopService(intent);
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ActivityForGoogleAPITest.class);

                startActivity(intent);
            }
        });
    }

    public boolean checkLocationServiceStatus() { // 위치서비스를 사용가능한지 확인하는 메소드
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || // GPS를 통해 현재 위치 확인이 가능한지
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); // 네트워크를 통해 현재 위치 확인이 가능한지
    }

    private void showDialogForLocationServiceSetting() { // 다이얼로그 창을 띄우는 메소드
        AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this); // 다이얼로그 창
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치서비스가 필요합니다.\n위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 설정 버튼 눌렀을 시
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE); // GPS 설정 화면을 불러옴
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 취소 버튼 눌렀을 시
                dialog.cancel(); // 다이얼로그 창을 닫음
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:{

                // 사용자가 GPS를 활성화 시켰는 지 검사
                if (checkLocationServiceStatus()) {

                    Log.e("onActivityResult", "GPS 활성화 됨");
                    checkRunTimePermission();
                    return; // 이건 왜 쓰지????
                }
                break;
            }
        }
    }

    public void checkRunTimePermission() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(TestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        // ACCESS_FINE_LOCATION은 Wi-Fi를 통한 위치 확인 (GPS_PROVIDER, NETWORK_PROVIDER)
        int coarseLocationPermission = ContextCompat.checkSelfPermission(TestActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        // ACCESS_COARSE_LOCATION은 기지국을 통한 위치 확인 (NETWORK_PROVIDER)

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 이미 퍼미션을 가지고 있다면
            // 위치 값을 가져올 수 있음
        }
        else { // 퍼미션 요청을 허용하지 않은 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(TestActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 사용자가 퍼미션 요청을 거부한 적이 있을 경우
                Toast.makeText(TestActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(TestActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                // 사용자에게 퍼미션 요청을 하는 코드, 요청 결과는 onRequestPermissionResult에서 수신
                // startActivityForResult 같은 느낌인듯
            }
            else {
                // 사용자가 퍼미션 요청을 거부한 적이 없는 경우
                ActivityCompat.requestPermissions(TestActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    @Override // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드이다.(이런게 콜백메소드)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // @NonNull String[] permissions ===> 요청 권한 명칭 ===> REQUIRED_PERMISSIONS 배열
        // @NonNull int[] grantResults ===> 요청 권한 획득 여부
        if ( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            Log.e("onRequestPermissions", "if 문 내부");

            boolean checkResult = true;

            for(int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) { // ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION 이 두 가지 권한이 허용이 됬는 지 확인
                    checkResult = false; // 안됬다면 false
                    break;

                    /* ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION 이 두 가지 권한이 허용 되어야 위치권한이 허용되는 것 같다.
                     * 또는 위치 권한을 허용하면 저 2가지가 PERMISSION_GRANTED 되는 것 같다. */
                }
            }

            if (checkResult) {
                // '위치 값을 가져올 수 있음' 이라고만 되어있다.
                // 여기에 위치 값을 받는 코드를 넣는 건가??
                // 특별한 다른 동작이 필요 없는 거 일지도?
            }

            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료. 2 가지 경우

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(TestActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(TestActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }
}
