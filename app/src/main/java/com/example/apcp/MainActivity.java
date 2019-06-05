package com.example.apcp;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    BluetoothService bs;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.mainactivity_bottomnav);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        SenseFragment senseFragment = new SenseFragment();
        transaction.replace(R.id.mainactivity_frame, senseFragment);
        transaction.commit();

        Intent intent = new Intent(this, GpsTracker.class);
        startService(intent);

        MenuItem item = bottomNavigationView.getMenu().findItem(R.id.action_senser).setChecked(true);
        // 매번 새로운 프래그먼트 객체가 생성되서 프래그먼트들을 보여준다. 이건 메모해놔야 될 것 같다.
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_explain:
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        Explain explain = new Explain();
                        transaction.replace(R.id.mainactivity_frame, explain);
                        transaction.commit();

                        return true;
                    case R.id.action_senser:
                        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                        SenseFragment senseFragment1 = new SenseFragment();
                        transaction2.replace(R.id.mainactivity_frame, senseFragment1);
                        transaction2.commit();

                        return true;
                    case R.id.action_maps:
                        FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                        Maps maps = new Maps();
                        transaction3.replace(R.id.mainactivity_frame, maps);
                        transaction3.commit();


                        return true;
                }

                return false;
            }
        });
    }

    /*
    @Override
    public void onBackPressed() {
        this.finish();
    }
    */

}
