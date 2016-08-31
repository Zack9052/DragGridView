package com.example.a19459.mydraggridview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;

    private List<String>list=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView=(GridView)findViewById(R.id.gridView);


        for(int i=1;i<=10;i++){
            list.add("item "+i);
        }

        MyNewAdapter adapter1=new MyNewAdapter(this,list);
        gridView.setAdapter(adapter1);


    }
}
