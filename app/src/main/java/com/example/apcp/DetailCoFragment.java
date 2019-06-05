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
import android.widget.ImageView;
import android.widget.TextView;

public class DetailCoFragment extends Fragment {

    ImageView imageView_Condition;
    TextView textView_Condition;
    TextView textView_Score_Co;

    public DetailCoFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_co,container,false);


        imageView_Condition = (ImageView) view.findViewById(R.id.image_condition);
        textView_Condition = (TextView)view.findViewById(R.id.text_condition);
        textView_Score_Co = (TextView)view.findViewById(R.id.text_score_co);
        viewData();
        return  view;
    }

    void viewData() {

        //수정해야함
        DBHelper dbHelper = new DBHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tb_senser where _id = 1 limit 1", null);

        while (cursor.moveToNext()) {
            textView_Score_Co.setText(cursor.getString(4));
            db.close();
        }
        imgviewCondition(textView_Score_Co.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewData();

    }

    void imgviewCondition(String textScore){

        double score = Double.parseDouble(textScore);

        if(score<100){
            textView_Condition.setText("양호");
            imageView_Condition.setImageResource(R.drawable.icon_good);
            return;
        }else if(score < 200){
            textView_Condition.setText("안좋음");
            imageView_Condition.setImageResource(R.drawable.icon_nomal);
            return;
        } else if(score < 400){
            textView_Condition.setText("나쁨");
            imageView_Condition.setImageResource(R.drawable.icon_bad);
            return;
        }else{
            textView_Condition.setText("매우 나쁨");
            imageView_Condition.setImageResource(R.drawable.icon_verybad);
            return;
        }
    }
}
