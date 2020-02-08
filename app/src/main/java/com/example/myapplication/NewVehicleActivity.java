package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class NewVehicleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText etVehicleId;
    private Button button;
    private Spinner spinner;
    String vehicleId, vehicleType;

    DatabaseReference database;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vehicle);
        etVehicleId = findViewById(R.id.etVehicleId);
        spinner = findViewById(R.id.spinner);
        button = findViewById(R.id.button);
        //String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("Vehicle");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vehicleId = etVehicleId.getText().toString();
                if(vehicleId.equals(""))
                    Toast.makeText(NewVehicleActivity.this,"Vehicle Id required",Toast.LENGTH_SHORT).show();
                else if(!Pattern.matches("[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}",vehicleId))
                    Toast.makeText(NewVehicleActivity.this,"Enter Vehicle Id properly",Toast.LENGTH_SHORT).show();
                else if(vehicleType.equals("Vehicle Class"))
                    Toast.makeText(NewVehicleActivity.this,"Select vehicle class",Toast.LENGTH_SHORT).show();
                else {
                    Model data = new Model(vehicleId,vehicleType);
                    database.child(database.push().getKey()).setValue(data);
                    Toast.makeText(NewVehicleActivity.this,"Data inserted successfully",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewVehicleActivity.this, RegisteredVehiclesActivity.class);
                    startActivity(intent);
                }
            }
        });
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        vehicleType = adapterView.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

