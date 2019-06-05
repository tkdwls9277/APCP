package com.example.apcp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailHumFragment extends Fragment {

    TextView textView_Condition;
    public DetailHumFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_hum,container,false);

        textView_Condition = (TextView)view.findViewById(R.id.text_score_hum);

        viewData();

        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewData();

    }

    void viewData(){

        //수정해야함
        DBHelper dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_senser where _id = 1 limit 1",null);
        while (cursor.moveToNext()){
            textView_Condition.setText(cursor.getString(2));
        }
        db.close();
    }
}
