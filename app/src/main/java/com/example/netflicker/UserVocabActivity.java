package com.example.netflicker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.JsonReader;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserVocabActivity extends AppCompatActivity {
    public List<String> vocabs = new ArrayList<>();
    public List<String> meanings = new ArrayList<>();
    //public HashMap<String, String> word_map = new HashMap<>();
    boolean needUpdate = false;

    private void doTranslation() {
        StringBuilder output = new StringBuilder();
        String clientId = "PaU7hlgJXAjTRZsUY0i1";
        String clientSecret = "RcNrCDDwX5";
        String translationText;
        String result = "";
        JsonParser parser = new JsonParser();
        JsonElement element;

        for (int i = 0; i < vocabs.size(); i++) {
            translationText = vocabs.get(i);
            Log.v("20202989", "translationText: "+translationText);
            if (android.os.Build.VERSION.SDK_INT > 9)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            try {
                String text = URLEncoder.encode(translationText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                String postParams = "source=en&target=ko&text=" + text;
                //Log.v("20202989", "[1]");
                con.setDoOutput(true);
                Log.v("20202989", "[2]");
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                Log.v("20202989", "[3]");
                wr.writeBytes(postParams);
                Log.v("20202989", "[4]");
                wr.flush();
                Log.v("20202989", "[5]");
                wr.close();
                Log.v("20202989", "[6]");

                int responseCode = con.getResponseCode();
                Log.v("20202989", "responseCode: "+ String.valueOf(responseCode));
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                result = response.toString();
                Log.d("20202989", "Papago result: " + result);
            } catch (Exception ex) {
                Log.e("20202989 SampleHTTP", "Exception in processing response", ex);
                ex.printStackTrace();
            }


            element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null) {
                Log.e("20202989", "errorCode: " + element.getAsJsonObject().get("errorCode").getAsString());
                meanings.add("");
            } else if(element.getAsJsonObject().get("message") != null) {
                meanings.add(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("20202989", "UserVocabActivity onCreate");
        setContentView(R.layout.activity_vocab_list);

        File dir = new File(this.getFilesDir(), "mydir");
        if(!dir.exists()){
            dir.mkdir();
        }

        //String json_str = readJSON("user_vocab.json");
        Gson gson = new Gson();
        //this.vocabs = gson.fromJson(json_str, this.vocabs.getClass());

        try {
            File _file = new File(dir, "user_vocab.json");
            Log.v("20202989", _file.getAbsolutePath().toString());
            Reader reader = new FileReader(_file);
            this.vocabs = gson.fromJson(reader, this.vocabs.getClass());

            _file = new File(dir, "user_meaning.json");
            Log.v("20202989", _file.getAbsolutePath().toString());
            reader = new FileReader(_file);
            this.meanings = gson.fromJson(reader, this.meanings.getClass());
        } catch (FileNotFoundException e) {
            needUpdate = true;
        }
        if (this.vocabs.size() != this.meanings.size()) {
            needUpdate = true;
        }

        if (needUpdate) {
            doTranslation();
            gson = new GsonBuilder().setPrettyPrinting().create();
            try {
                File _file = new File(dir, "user_meaning.json");
                FileWriter writer = new FileWriter(_file);
                gson.toJson(meanings, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*
        for (int i = 0; i < vocabs.size(); i++) {
            word_map.put(vocabs.get(i), meanings.get(i));
        }

         */

        ListView listView = findViewById(R.id.user_vocab_list_view);
        //ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_vocab_listview, R.id.textView, vocabs);
        UserVocabAdapter adapter = new UserVocabAdapter(this, 0, vocabs, meanings);
        listView.setAdapter(adapter);
    }


    public String readJSON(String fileName) {
        String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = this.getAssets().open(fileName);
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
    /*
    public String writeJSON(String fileName, List<String> data) {
        String json = new Gson().toJson(data);
        try {
            // Opening data.json file
            FileWriter file = new FileWriter("E:/output.json");
            InputStream inputStream = this.getAssets().open(fileName);
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

     */


    /*
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... arg0) {
            StringBuilder output = new StringBuilder();
            String clientId = "PaU7hlgJXAjTRZsUY0i1";
            String clientSecret = "RcNrCDDwX5";
            String translationText = meanings[i];
            String result;
            try {
                String text = URLEncoder.encode(translationText, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                String postParams = "source=en&target=ko&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    output.append(inputLine);
                }
                br.close();
            } catch (Exception ex) {
                Log.e("SampleHTTP", "Exception in processing response.", ex);
                ex.printStackTrace();
            }
            result = output.toString();
            return null;
        }

        protected void onPostExecute() {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null) {
                Log.e("Naver Translation Fault", "errorCode: " + element.getAsJsonObject().get("errorCode").getAsString());
            } else if(element.getAsJsonObject().get("message") != null) {
                kor_words.add(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());

            }

        }
        protected void onPreExecute() {

        }
    }

     */
}
