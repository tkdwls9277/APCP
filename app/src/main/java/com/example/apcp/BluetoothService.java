package com.example.apcp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.Context.NOTIFICATION_SERVICE;


public class BluetoothService {

    public static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태

    SenserModel nowModel = new SenserModel();
    SenserModel pastModel = new SenserModel();

    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;

    private Thread workerThread = null; // 문자열 수신에 사용될 쓰레드

    private Activity targetActivity;
    private Context context;

    private AlertDialog alertDialog;

    public BluetoothService(Context context) {
        this.context = context;
    }

    public BluetoothService(Activity activity) {
        targetActivity = activity;
    }


    public boolean checkBluetooth() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();

        if (ba == null) {
            // 장치가 블루투스를 지원하지 않는 경우
            if(targetActivity != null) {
                Toast.makeText(targetActivity, "블루투스를 지원하지 않는 장치입니다.", Toast.LENGTH_LONG).show();
                targetActivity.finish();
            }
            return false;

        } else {
            // 장치가 블루투스를 지원하는 경우
            if (!ba.isEnabled()) {
                // 블루투스가 비활성 상태인 경우
                return false;
            } else {
                // 블루투스가 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택
                Log.e("블루투스", "활성");
                return true;

            }
        }
    }

    public void selectBluetooth() {
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        final Set<BluetoothDevice> devices = ba.getBondedDevices(); // Set은 순서를 고려하지 않고 저장하는 집합

        // 이미 페어링된 블루투스 기기를 찾음
        final int pairedDevicesCount = devices.size();
        // 페어링된 디바이스의 크기(크기가 아니고 개수(?)인거같다.)를 저장

        if (pairedDevicesCount == 0) { // 페어링되어있는 장치가 없는 경우
            // 페어링하기 위한 함수 호출
        } else { // 페어링되어있는 장치가 있는 경우
            AlertDialog.Builder builder = new AlertDialog.Builder(targetActivity); // 디바이스를 선택하기위한 다이얼로그
            builder.setTitle("페어링된 디바이스 목록"); // 다이얼로그 타이틀 설정

            List<String> list = new ArrayList<>(); // 페어링된 디바이스의 이름을 저장할 리스트
            for (BluetoothDevice bd : devices) { // 반복이 돌 때 마다 bluetoothDevice에 devices의 값들 중 하나를 저장한다.
                list.add(bd.getName()); // 그 값을 리스트에 저장
            }
            list.add("취소");

            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            // CharSequence배열을 list.size 크기로 만들고 list의 값들을 저장함
            // toArray는 리스트를 배열로 변경하는 메소드

            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == pairedDevicesCount) { // 취소를 누른 경우
                        Toast.makeText(targetActivity, "디바이스를 먼저 페어링 해주세요!", Toast.LENGTH_LONG);
                    } else {
                        Log.e("BluetoothSocket connect", "실행 전");

                        for (BluetoothDevice tempDevice : devices) { // 페어링된 디바이스들을 탐색
                            if (charSequences[which].toString().equals(tempDevice.getName())) { // 사용자가 선택한 이름과 같은 디바이스를 찾았을 때
                                bluetoothDevice = tempDevice;
                                break;
                            }
                        }

                        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성

                        try {
                            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                            bluetoothSocket.connect();

                            Log.e("Bluetooth", "Connected");

                            DBHelper dbHelper = new DBHelper(targetActivity.getApplicationContext());
                            dbHelper.saveDeviceName(charSequences[which].toString());

                            if(targetActivity != null) { // Splash 에서 부를 때
                                connectDevice(charSequences[which].toString());
                            }

                            targetActivity.startActivity(new Intent(targetActivity, MainActivity.class));
                            targetActivity.finish();

                        } catch (Exception e) {

                            e.printStackTrace();
                            Log.e("Exception", "" + e);
                            try {
                                if (bluetoothSocket.isConnected()) bluetoothSocket.close();
                            } catch (IOException ioE) {
                                Log.e("BluetoothSocket Close", "Failed");
                            }
                        }

                    }
                }
            });

            builder.setCancelable(false); // 뒤로가기 버튼 막음

            alertDialog = builder.create(); // 다이얼로그 생성
            alertDialog.show(); // 다이얼로그 출력
        }
    }
    public void connectDevice(String string) {

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = ba.getBondedDevices();
        this.context = context;
        DBHelper dbHelper;
        if(context != null) dbHelper = new DBHelper(context);

        InputStream inputStream;

        for (BluetoothDevice tempDevice : devices) { // 페어링된 디바이스들을 탐색
            if (string.equals(tempDevice.getName())) { // 사용자가 선택한 이름과 같은 디바이스를 찾았을 때
                Log.e("string", string);
                bluetoothDevice = tempDevice;
                break;
            }
        }

        if(bluetoothDevice == null) Log.e("bd ", "is null");

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성

        try {
            if(targetActivity == null) { // 호출시점이 GpsTracker 일 때

                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();

                inputStream = bluetoothSocket.getInputStream();
                receiveData(inputStream);
            }
            else { // 호출 시점이 Splash 일 때
                inputStream = bluetoothSocket.getInputStream();
                receiveData(inputStream);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "" + e);

            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) bluetoothSocket.close();
            } catch (IOException ioE) {
                Log.e("BluetoothSocket Close", "Failed");
            }
        }

    }

    public void connectDevice(String string, Context context) {

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = ba.getBondedDevices();
        this.context = context;
        DBHelper dbHelper;
        if(context != null) dbHelper = new DBHelper(context);

        InputStream inputStream;

        for (BluetoothDevice tempDevice : devices) { // 페어링된 디바이스들을 탐색
            if (string.equals(tempDevice.getName())) { // 사용자가 선택한 이름과 같은 디바이스를 찾았을 때
                Log.e("string", string);
                bluetoothDevice = tempDevice;
                break;
            }
        }

        if(bluetoothDevice == null) Log.e("bd ", "is null");

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성

        try {
            if(targetActivity == null) { // 호출시점이 GpsTracker 일 때

                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();

                inputStream = bluetoothSocket.getInputStream();
                receiveData(inputStream);
            }
            else { // 호출 시점이 Splash 일 때
                inputStream = bluetoothSocket.getInputStream();
                receiveData(inputStream);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "" + e);

            try {
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) bluetoothSocket.close();
            } catch (IOException ioE) {
                Log.e("BluetoothSocket Close", "Failed");
            }
        }

    }



    public void receiveData(final InputStream inputStream){

        Log.e("receiveData", "in");

        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성

        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()) { // 현재 스레드에 인터럽트 발생 시 중단
                    try {

                        int readBufferPosition = 0; // 버퍼 내 문자 저장 위치
                        byte[] readBuffer = new byte[1024]; // 수신된 문자열을 저장하기 위한 버퍼

                        // 데이터를 수신했는지 확인합니다.
                        int byteAvailable = inputStream.available();
                        Log.e("","receiveCheck");
                        // 데이터가 수신 된 경우
                        if(byteAvailable > 0) {
                            Log.e("","receiveClear");
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                                for(int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                // 개행문자를 기준으로 받음(한줄)
                                if(tempByte == '\n') {
                                    // readBuffer 배열을 encodedBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // 인코딩 된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            Log.e("run","run");
                                            String[] fintext = text.split(",");

                                            savePastData();
                                            saveNowData(fintext);
                                            showNotification();

                                            try {
                                                bluetoothSocket.close();
                                                Log.e("workerThread 내부", "소켓 닫음");
                                            } catch (IOException e) {
                                                Log.e("workerThread 내부", "소켓 닫기 실패");
                                            }

                                            workerThread.interrupt(); // 한번만 실행되게 인터럽트를 걸어주는 코드
                                        }
                                    });
                                 } // 개행 문자가 아닐 경우
                                else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        // 1초마다 받아옴
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        workerThread.start();
    }

    public boolean checkOncePaired() {
        Log.e("BluetoothService", "checkOncePaired");
        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = ba.getBondedDevices();

        DBHelper dbHelper = new DBHelper(targetActivity.getApplicationContext());

        for(BluetoothDevice tempDevice : devices) {
            if (tempDevice.getName().equals(dbHelper.getDeviceName())) { // 등록된 장치 중 DB에 들어간 이름과 같은 장치가 있다면
                bluetoothDevice = tempDevice;


                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // UUID 생성

                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();


                    InputStream inputStream = bluetoothSocket.getInputStream();
                    receiveData(inputStream);

                    targetActivity.startActivity(new Intent(targetActivity, MainActivity.class));
                    targetActivity.finish();
                    Log.e("Bluetooth", "Connected");

                    return true;

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IOException", "" + e);

                    try {
                        bluetoothSocket.close();
                    } catch (IOException ioE) {
                        Log.e("checkOncePaired 내부", "소켓 닫기 실패");
                    }
                    return false;
                }
            }
        }

        return false;

    }

    void savePastData(){

        if(nowModel.dust != "") {
            int dataCount = checkDataRows();

            pastModel.dust = nowModel.dust;
            pastModel.temperature = nowModel.temperature;
            pastModel.humidity = nowModel.humidity;
            pastModel.co = nowModel.co;


            if (dataCount == 1) {
                DBHelper dbHelper;
                if (context != null) dbHelper = new DBHelper(context);
                else dbHelper = new DBHelper(targetActivity.getApplicationContext());

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("insert into tb_senser (tem, wet, dust, co) values (?,?,?,?)",
                        new String[]{ pastModel.temperature, pastModel.humidity, pastModel.dust, pastModel.co});
                db.close();
            } else if (dataCount > 1) {
                DBHelper dbHelper;
                if (context != null) dbHelper = new DBHelper(context);
                else dbHelper = new DBHelper(targetActivity.getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.execSQL("update tb_senser set tem = ?,  wet = ?, dust = ?, co = ? where _id = '2'",
                        new String[]{ pastModel.temperature, pastModel.humidity,pastModel.dust, pastModel.co});
                db.close();
            }
        }
    }

    int checkDataRows(){

        DBHelper dbHelper;
        if (targetActivity == null) dbHelper = new DBHelper(context);
        else dbHelper = new DBHelper(targetActivity.getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_senser",null);

        int count = 0;
        while (cursor.moveToNext()){
            count++;
        }
        db.close();

        return count;
    }

    void saveNowData(String[] fintext){
        if(fintext.length>=3) {

            nowModel.temperature = fintext[0];
            nowModel.humidity = fintext[1];
            nowModel.dust = fintext[2];
            nowModel.co = fintext[3];


            DBHelper dbHelper;
            if (context != null) dbHelper = new DBHelper(context);
            else dbHelper = new DBHelper(targetActivity.getApplicationContext());

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int dataCount = checkDataRows();

            if (dataCount == 0) {
                db.execSQL("insert into tb_senser (tem, wet, dust,  co) values (?,?,?,?)",
                        new String[]{ nowModel.temperature, nowModel.humidity, nowModel.dust, nowModel.co});
                db.close();
            } else {
                db.execSQL("update tb_senser set tem = ?, wet = ?,  dust = ?, co = ? where _id = '1'",
                        new String[]{nowModel.temperature, nowModel.humidity, nowModel.dust, nowModel.co});
                db.close();
            }
        }
    }

    void notiCondition(){
        if(pastModel.dust !="" ){
            if(Double.parseDouble(nowModel.dust) < 30 && Double.parseDouble(pastModel.dust) > 30){
                showNotification();
                return;
            }else if (Double.parseDouble(nowModel.dust) < 60){
                if( Double.parseDouble(pastModel.dust) > 60 || Double.parseDouble(pastModel.dust) < 30)
                    showNotification();
                return;
            }else if(Double.parseDouble(nowModel.dust) < 90){
                if (Double.parseDouble(pastModel.dust) > 90 || Double.parseDouble(pastModel.dust) < 60)
                    showNotification();
                return;
            }else if(Double.parseDouble(nowModel.dust) >= 90){
                if (Double.parseDouble(pastModel.dust) < 90)

                    showNotification();
                return;
            }
        }
    }

    void showNotification(){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(targetActivity);

        RemoteViews collapsedView = new RemoteViews(targetActivity.getPackageName(),
                R.layout.notification_main);


        Intent intent1 = new Intent(targetActivity, Splash.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(targetActivity,
                0,
                intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if(nowModel.dust != "") {
           String notiText = setNotiText();
            collapsedView.setTextViewText(R.id.text_Noti_Content, notiText);
        }
       collapsedView.setOnClickPendingIntent(R.id.imageView_refresh,pendingIntent1);

        Notification notification = new NotificationCompat.Builder(targetActivity, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomBigContentView(collapsedView)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;


        NotificationManager manager = (NotificationManager)targetActivity.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("default", "기본",NotificationManager.IMPORTANCE_LOW));
        }

        notificationManager.notify(1, notification);
    }

    String setNotiText(){
        String notiText = "미세먼지 : ";

        if (Double.parseDouble(nowModel.dust) < 30){
            notiText += "좋음 ";
        }else if (Double.parseDouble(nowModel.dust) >= 30 && Double.parseDouble(nowModel.dust) < 60 ){
            notiText += "보통 ";
        }else if(Double.parseDouble(nowModel.dust) >= 60 && Double.parseDouble(nowModel.dust) < 90){
            notiText += "나쁨 ";
        }else{
            notiText += "매우 나쁨 ";
        }

        notiText += "온도 : " + nowModel.temperature + " 습도 : "+nowModel.humidity;

        return notiText;
    }

    public void socketClose() {
        Log.e("socketClose", "yes");
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            Log.e("socketClose 메소드 내부", "소켓 닫기 실패");
        }
    }
}