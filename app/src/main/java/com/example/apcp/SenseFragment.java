package com.example.apcp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SenseFragment extends Fragment {

    SenserModel pastModel = new SenserModel();
    SenserModel nowModel = new SenserModel();

    TextView textView_temp;
    TextView textView_wet;
    TextView textView_dust;
    TextView textView_co;

    LinearLayout linearLayout_temp;
    LinearLayout linearLayout_hum;
    LinearLayout linearLayout_dust;
    LinearLayout linearLayout_co;

    TextView textView_dust_level;
    public SenseFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sense,container,false);

        textView_temp = view.findViewById(R.id.text_tem);
        textView_wet = view.findViewById(R.id.text_wet);
        textView_dust = view.findViewById(R.id.text_dust);
        textView_co = view.findViewById(R.id.text_co);

        linearLayout_temp = view.findViewById(R.id.layout_temp);
        linearLayout_hum = view.findViewById(R.id.layout_humi);
        linearLayout_dust = view.findViewById(R.id.layout_dust);
        linearLayout_co = view.findViewById(R.id.layout_CO);

        textView_dust_level = view.findViewById(R.id.text_dust);

        // saveNowData(); BluetoothService 의 receiveData 메소드 안으로 옮기자@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // savePastData();
        viewData(); // TextView 에 DB에 저장된 값들을 출력해주는 코드

        linearLayout_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("name", 1);
                startActivity(intent);
            }
        });

        linearLayout_hum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("name", 2);
                startActivity(intent);
            }
        });

        linearLayout_dust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("name", 3);
                startActivity(intent);
            }
        });

        linearLayout_co.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("name", 4);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewData();

    }


    void viewData(){
        DBHelper dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_senser where _id = 1 limit 1",null);

        while (cursor.moveToNext()){
            textView_temp.setText(cursor.getString(1));
            textView_wet.setText(cursor.getString(2));
            textView_dust.setText(cursor.getString(3));
            textView_co.setText(cursor.getString(4));
        }
        db.close();
    }

}
