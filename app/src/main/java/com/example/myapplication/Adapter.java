package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Holder> {

    Context c;
    ArrayList<Model> vehicleData;


    public Adapter(Context c, ArrayList<Model> arrayList) {
        this.c = c;
        this.vehicleData = arrayList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row,null);
        return new Holder(view,Holder.listener);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.tvVehicleId.setText(vehicleData.get(position).getVehicleId());
        holder.tvVehicleType.setText(vehicleData.get(position).getVehicleType());
    }

    @Override
    public int getItemCount() {
        return vehicleData.size();
    }
}
