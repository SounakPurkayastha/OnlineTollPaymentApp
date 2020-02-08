package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChooseVehicleActivityAdapter extends RecyclerView.Adapter<ChooseVehicleActivityAdapter.ChooseVehicleActivityHolder> {

    Context c;
    ArrayList<Model> vehicleData;
    OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public static class ChooseVehicleActivityHolder extends RecyclerView.ViewHolder {

        TextView tvVehicleId, tvVehicleType;

        public ChooseVehicleActivityHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            this.tvVehicleId = itemView.findViewById(R.id.tvVehicleRegistration);
            this.tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public ChooseVehicleActivityAdapter(ArrayList<Model> arrayList) {
        vehicleData = arrayList;
    }

    @NonNull
    @Override
    public ChooseVehicleActivityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row, parent, false);
        ChooseVehicleActivityHolder evh = new ChooseVehicleActivityHolder(v, listener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseVehicleActivityHolder holder, int position) {
        holder.tvVehicleId.setText(vehicleData.get(position).getVehicleId());
        holder.tvVehicleType.setText(vehicleData.get(position).getVehicleType());
    }

    @Override
    public int getItemCount() {
        return vehicleData.size();
    }
}
