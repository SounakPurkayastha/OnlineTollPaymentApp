package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResponseActivity extends AppCompatActivity {

    //SharedPreferences sharedPreferences;
    TextView textView;
    JsonPlaceHolderApi jsonPlaceHolderApi;
    ImageView imageView;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
        //sharedPreferences = this.getSharedPreferences("com.example.myapplication",MODE_PRIVATE);
        imageView = findViewById(R.id.qrcode);
        textView = findViewById(R.id.textView);
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://ec2-18-224-227-221.us-east-2.compute.amazonaws.com")
                .addConverterFactory(GsonConverterFactory.create()).build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        /*if(sharedPreferences.getString("qrcode",null) == null) {
            getQRCode();
        }*/
        //reference = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("QRCode");
        reference = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("QRCode");
        if(HomeActivity.qrcode == null) {
            Toast.makeText(ResponseActivity.this,"No QR codes found",Toast.LENGTH_SHORT).show();
        }
        else {
            createQRCode(HomeActivity.qrcode);
        }

        /*else  {
            createQRCode(sharedPreferences.getString("qrcode",null));
            //textView.setText(sharedPreferences.getString("qrcode",null));
        }*/

    }

    public void createQRCode(String code) {
        WindowManager manager = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;
        qrgEncoder = new QRGEncoder(code,null, QRGContents.Type.TEXT,smallerDimension);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                //sharedPreferences.edit().putBoolean("paymentComplete",false).apply();
                //sharedPreferences.edit().remove("qrcode").apply();
                reference.removeValue();
                Call<Void> call = jsonPlaceHolderApi.deleteQRCode(HomeActivity.qrcode);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        startActivity(new Intent(ResponseActivity.this,NoCodeActivity.class));
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.i("Error: ",t.getMessage());
                    }
                });
                //sharedPreferences.edit().remove("vehicleId").apply();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ResponseActivity.this,HomeActivity.class));
    }
}
