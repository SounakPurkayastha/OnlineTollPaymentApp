package com.example.myapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Holder extends RecyclerView.ViewHolder {

    TextView tvVehicleId, tvVehicleType;

    public Holder(@NonNull View itemView) {
        super(itemView);
        this.tvVehicleId = itemView.findViewById(R.id.tvVehicleRegistration);
        this.tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
    }
}
