package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChooseVehicleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Model> vehicleData;
    private ChooseVehicleActivityAdapter adapter;
    String userId;
    static String vehicleType;
    static String vehicleId;

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_vehicle);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        vehicleData = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference().child(userId).child("Vehicle");
        dbRef.addListenerForSingleValueEvent(valueEventListener);
        Toast.makeText(ChooseVehicleActivity.this,"Please choose current vehicle",Toast.LENGTH_SHORT).show();
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                Model uData = dataSnapshot1.getValue(Model.class);
                vehicleData.add(uData);
            }
            adapter = new ChooseVehicleActivityAdapter(vehicleData);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new ChooseVehicleActivityAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    Model m = vehicleData.get(position);
                    vehicleType = m.getVehicleType();
                    vehicleId = m.getVehicleId();
                    startActivity(new Intent(ChooseVehicleActivity.this,PaymentActivity.class));
                }
            });
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(ContextCompat.getDrawable(ChooseVehicleActivity.this, R.drawable.divider));
            recyclerView.addItemDecoration(dividerItemDecoration);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChooseVehicleActivity.this,HomeActivity.class);
        startActivity(intent);
    }
}
