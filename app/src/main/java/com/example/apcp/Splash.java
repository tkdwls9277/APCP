package com.example.apcp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class Splash extends Activity {

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private BluetoothService bs;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bs = new BluetoothService(this);
        dbHelper = new DBHelper(this);

        if(!checkLocationServiceStatus()) { // 위치 권한 확인
            Log.e("1", "위치 권한 확인");
            showDialogForLocationServiceSetting();
            Log.e("권한확인", "" + checkLocationServiceStatus());
        }

        else if((dbHelper.getDeviceName() != "")&&bs.checkOncePaired()) {
             Log.e("2", "페어링된 적 있는 지 확인");
            // 특별한 동작 필요 x
        }
        else if(bs.checkBluetooth()) { // 블루투스 확인
            Log.e("3", "블루투스 활성상태 확인");
            bs.selectBluetooth();
        }
        else {
            Log.e("4", "블루투스 활성 상태 요청");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, bs.REQUEST_ENABLE_BT);
            // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요청
        }
             /*
       // MainActivity.class 자리에 다음에 넘어갈 액티비티를 넣어주기
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("state", "launch");
        startActivity(intent);*/
    }

    public boolean checkLocationServiceStatus() { // 위치서비스를 사용가능한지 확인하는 메소드
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || // GPS를 통해 현재 위치 확인이 가능한지
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER); // 네트워크를 통해 현재 위치 확인이 가능한지
    }

    private void showDialogForLocationServiceSetting() { // 다이얼로그 창을 띄우는 메소드
        AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this); // 다이얼로그 창
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
            case BluetoothService.REQUEST_ENABLE_BT:{
                if(resultCode == RESULT_OK){
                    // 블루투스가 활성 상태가 됨
                    bs.selectBluetooth();

                    recreate();
                }
                else if(resultCode == RESULT_CANCELED){
                    // 블루투스가 비활성 상태임
                    finish();
                }
                break;
            }
        }
    }

    public void checkRunTimePermission() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.ACCESS_FINE_LOCATION);
        // ACCESS_FINE_LOCATION은 Wi-Fi를 통한 위치 확인 (GPS_PROVIDER, NETWORK_PROVIDER)
        int coarseLocationPermission = ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        // ACCESS_COARSE_LOCATION은 기지국을 통한 위치 확인 (NETWORK_PROVIDER)

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 이미 퍼미션을 가지고 있다면
            // 위치 값을 가져올 수 있음
            finish(); // 액티비티를 닫은 후 다시 열 수 있을까?@@??@?@@?@?@??@@@?@??@?@?@?@?@?@??@?@?@@@@@@@@@@@@@@@@@@@
            startActivity(new Intent(this, Splash.class)); // 스플래시 다시 열기
            // Splash.this.finish();
            // finish() 대신 이 메소드를 써야 할 지도@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@222
        }
        else { // 퍼미션 요청을 허용하지 않은 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(Splash.this, REQUIRED_PERMISSIONS[0])) {
                // 사용자가 퍼미션 요청을 거부한 적이 있을 경우
                Toast.makeText(Splash.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(Splash.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                // 사용자에게 퍼미션 요청을 하는 코드, 요청 결과는 onRequestPermissionResult에서 수신
                // startActivityForResult 같은 느낌인듯
            }
            else {
                // 사용자가 퍼미션 요청을 거부한 적이 없는 경우
                ActivityCompat.requestPermissions(Splash.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
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


                finish();
                startActivity(new Intent(this, Splash.class)); // 여기도 다시 닫고 여는 과정@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                // Splash.this.finish();
                // finish() 대신 이 메소드를 써야 할 지도@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@222

                // Handler handler = new Handler();
                // handler.postDelayed(new SplashHandler(), 2000); // 이 위치가 맞는 지 모른다.@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@22

            }

            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료. 2 가지 경우

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(Splash.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(Splash.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        // 뒤로가기 버튼 막음
    }

}
