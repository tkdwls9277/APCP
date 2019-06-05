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

public class DetailTemFragment extends Fragment {

    TextView textView_Score;
    public DetailTemFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_tem,container,false);

        textView_Score = (TextView)view.findViewById(R.id.text_score_tem);

        viewData();
        return  view;
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
            textView_Score.setText(cursor.getString(1));

        }
        db.close();

    }

}
