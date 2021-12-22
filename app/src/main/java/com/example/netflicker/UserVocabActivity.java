package com.example.netflicker;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserVocabActivity extends AppCompatActivity {
    public List<String> vocabs = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("20202989", "UserVocabActivity onCreate");
        setContentView(R.layout.activity_vocab_list);

        String json_str = readJSON();
        Gson gson = new Gson();
        this.vocabs = gson.fromJson(json_str, this.vocabs.getClass());
        //Log.v("20202989", String.format("%s", this.vocabs.get(0)));
        //Log.v("20202989", String.format("%s", this.vocabs.get(1)));
        //Log.v("20202989", String.format("%s", this.vocabs.get(2)));

        ListView listView = findViewById(R.id.user_vocab_list_view);
        //UserVocabListAdapter adapter = new UserVocabListAdapter(this);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_vocab_listview, R.id.textView, vocabs);
        listView.setAdapter(adapter);
    }

    public String readJSON() {
        String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = this.getAssets().open("user_vocab.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            inputStream.read(buffer);
            inputStream.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return json;
        }
        return json;
    }
}
