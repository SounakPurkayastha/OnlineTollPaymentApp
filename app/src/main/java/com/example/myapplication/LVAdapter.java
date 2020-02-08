package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class LVAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> titles, descriptions;
    ArrayList<Integer> imgs;

    LVAdapter(Context c, ArrayList<String> titles, ArrayList<String> descriptions, ArrayList<Integer> imgs) {
        super(c, R.layout.row, R.id.tvTitle, titles);
        this.context = c;
        this.titles = titles;
        this.descriptions = descriptions;
        this.imgs = imgs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, null);
        ImageView ivImage = row.findViewById(R.id.ivImage);
        TextView tvTitle = row.findViewById(R.id.tvTitle);
        TextView tvDesc = row.findViewById(R.id.tvDesc);

        ivImage.setImageResource(imgs.get(position));
        tvTitle.setText(titles.get(position));
        tvDesc.setText(descriptions.get(position));

        return row;
    }
}