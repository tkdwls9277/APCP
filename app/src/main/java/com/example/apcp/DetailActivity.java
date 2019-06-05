package com.example.apcp;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.apcp.DetailCoFragment;
import com.example.apcp.DetailDustFragment;
import com.example.apcp.DetailHumFragment;
import com.example.apcp.DetailTemFragment;

public class DetailActivity extends AppCompatActivity {


    Intent intent;
    int name;

    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;

        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.background1, options);
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        relativeLayout.setBackground(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.background1)));

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.detailactivity_bottomnav);

        intent = getIntent();
        name = intent.getIntExtra("name", 0);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_tem:
                        FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                        DetailTemFragment detailTemFragment = new DetailTemFragment();
                        transaction1.replace(R.id.detailactivity_frame, detailTemFragment);
                        transaction1.commit();
                        return true;

                    case R.id.action_hum:
                        FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                        DetailHumFragment detailHumFragment = new DetailHumFragment();
                        transaction2.replace(R.id.detailactivity_frame, detailHumFragment);
                        transaction2.commit();
                        return true;

                    case R.id.action_dust:

                        FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                        DetailDustFragment detailDustFragment = new DetailDustFragment();
                        transaction3.replace(R.id.detailactivity_frame, detailDustFragment);
                        transaction3.commit();

                        return true;
                    case R.id.action_co:
                        FragmentTransaction transaction4 = getSupportFragmentManager().beginTransaction();
                        DetailCoFragment detailCoFragment= new DetailCoFragment();
                        transaction4.replace(R.id.detailactivity_frame, detailCoFragment);
                        transaction4.commit();
                        return true;
                }
                return false;
            }
        });

        switch (name){
            case 1:
                FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
                DetailTemFragment detailTemFragment = new DetailTemFragment();
                transaction1.replace(R.id.detailactivity_frame, detailTemFragment);
                transaction1.commit();
                MenuItem item1 = bottomNavigationView.getMenu().findItem(R.id.action_tem).setChecked(true);
                return;

            case 2:
                FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                DetailHumFragment detailHumFragment = new DetailHumFragment();
                transaction2.replace(R.id.detailactivity_frame, detailHumFragment);
                transaction2.commit();
                MenuItem item2 = bottomNavigationView.getMenu().findItem(R.id.action_hum).setChecked(true);
                return;
            case 3:
                FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                DetailDustFragment detailDustFragment = new DetailDustFragment();
                transaction3.replace(R.id.detailactivity_frame, detailDustFragment);
                transaction3.commit();
                MenuItem item3 = bottomNavigationView.getMenu().findItem(R.id.action_dust).setChecked(true);
                return;
            case 4:
                FragmentTransaction transaction4 = getSupportFragmentManager().beginTransaction();
                DetailCoFragment detailCoFragment= new DetailCoFragment();
                transaction4.replace(R.id.detailactivity_frame, detailCoFragment);
                transaction4.commit();
                MenuItem item4 = bottomNavigationView.getMenu().findItem(R.id.action_co).setChecked(true);
                return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.relativeLayout));
    }

    private void recycleView(View view) {
        if(view != null) {
            Drawable bg = view.getBackground();
            if(bg != null) {
                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackground(null);
            }
        }
    }


}