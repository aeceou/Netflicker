package com.example.netflicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class UserVocabAdapter extends ArrayAdapter<HashMap<String, String>> {
    private List<String> ens;
    private List<String> kos;
    Context context;

    public UserVocabAdapter(@NonNull Context context, int resource, List<String> ens, List<String> kos) {
        super(context, resource);
        this.ens = ens;
        this.kos = kos;
        this.context = context;
    }

    @Override
    public int getCount() {
        return ens.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.activity_vocab_listview, null, true);

        TextView en_word = convertView.findViewById(R.id.vocab_view);
        en_word.setText(ens.get(position).toString());
        TextView kor_meaning = convertView.findViewById(R.id.meaning_view);
        kor_meaning.setText(kos.get(position).toString());

        return convertView;

    }
}
