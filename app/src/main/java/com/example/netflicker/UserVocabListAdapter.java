package com.example.netflicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class UserVocabListAdapter extends BaseAdapter {

    public String[] vocabs = {"apple", "pear", "grape"};
    Context context;

    public UserVocabListAdapter(String[] vocabs, Context context) {
        this.vocabs = new String[]{"apple", "pear"}; // TODO: load user vocab(.json)
        this.context = context;
    }

    public UserVocabListAdapter(Context context) {
        this.vocabs = new String[]{"apple", "pear"}; // TODO: load user vocab(.json)
        this.context = context;
    }

    @Override
    public int getCount() {
        return vocabs.length;
    }

    @Override
    public Object getItem(int i) {
        return vocabs[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView setView = new TextView(context);
        setView.setText(vocabs[i]);
        return setView;
    }
}
