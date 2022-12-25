package com.example.myapplication;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;


import static android.content.Context.MODE_PRIVATE;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ScoresTableFragment extends Fragment {

    @SuppressLint("SetTextI18n")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_scores_table, container, false);

        TextView[] nameArray = new TextView[10];
        TextView[] scoreArray = new TextView[10];

        nameArray[0] = (view.findViewById(R.id.first_name));
        nameArray[1] = (view.findViewById(R.id.second_name));
        nameArray[2] = (view.findViewById(R.id.third_name));
        nameArray[3] = (view.findViewById(R.id.fourth_name));
        nameArray[4] = (view.findViewById(R.id.fifth_name));
        nameArray[5] = (view.findViewById(R.id.sixth_name));
        nameArray[6] = (view.findViewById(R.id.seventh_name));
        nameArray[7] = (view.findViewById(R.id.eighth_name));
        nameArray[8] = (view.findViewById(R.id.nine_name));
        nameArray[9] = (view.findViewById(R.id.ten_name));

        scoreArray[0] = (view.findViewById(R.id.first_score));
        scoreArray[1] = (view.findViewById(R.id.second_score));
        scoreArray[2] = (view.findViewById(R.id.third_score));
        scoreArray[3] = (view.findViewById(R.id.fourth_score));
        scoreArray[4] = (view.findViewById(R.id.fifth_score));
        scoreArray[5] = (view.findViewById(R.id.sixth_score));
        scoreArray[6] = (view.findViewById(R.id.seventh_score));
        scoreArray[7] = (view.findViewById(R.id.eighth_score));
        scoreArray[8] = (view.findViewById(R.id.nine_score));
        scoreArray[9] = (view.findViewById(R.id.ten_score));


        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("shared_preferences", MODE_PRIVATE);

        String jsonString = sharedPreferences.getString("list", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Competitor>>() {}.getType();
        ArrayList<Competitor> listFromGson;
        listFromGson = gson.fromJson(jsonString, type);
        if(listFromGson == null){
            listFromGson = new ArrayList<>();
        }

        Collections.sort(listFromGson);

        for(int i=0; i < listFromGson.size() && i < 10; i++){
            nameArray[i].setText(listFromGson.get(i).getName());
            scoreArray[i].setText(Integer.toString(listFromGson.get(i).getScore()));
        }

        return view;
    }

}
