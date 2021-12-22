package com.example.netflicker;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UserVocabActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_vocab_list);

        ListView listView = findViewById(R.id.user_vocab);
        UserVocabListAdapter listAdapter = new UserVocabListAdapter(this);
        listView.setAdapter(listAdapter);
    }
}
