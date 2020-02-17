package com.example.myapplication;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Holder extends RecyclerView.ViewHolder {

    TextView tvVehicleId, tvVehicleType;
    ImageView delete;
    static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public Holder(@NonNull View itemView, final OnItemClickListener listener) {
        super(itemView);
        this.tvVehicleId = itemView.findViewById(R.id.tvVehicleRegistration);
        this.tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
        this.delete = itemView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            }
        });
    }
}
