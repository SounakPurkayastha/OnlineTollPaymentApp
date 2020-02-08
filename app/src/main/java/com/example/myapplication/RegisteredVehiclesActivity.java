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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RegisteredVehiclesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    //private ArrayList<Model> vehicleData;
    private Adapter adapter;
    private Button btnAddVehicle;
    String userId;

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_vehicles);

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new Adapter(RegisteredVehiclesActivity.this,HomeActivity.vehicles);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(RegisteredVehiclesActivity.this, R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        btnAddVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisteredVehiclesActivity.this,NewVehicleActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisteredVehiclesActivity.this,HomeActivity.class);
        startActivity(intent);
    }

}
