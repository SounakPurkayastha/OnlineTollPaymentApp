package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentActivity extends AppCompatActivity {

    TextView textView,textView2,textView3;
    Button button;
    final int UPI_PAYMENT = 0;
    //static boolean paymentComplete = false;
    SharedPreferences sharedPreferences;
    int amount;
    String upiId;
    JSONObject object;
    JSONArray jsonArray;
    DatabaseReference reference;
    //JsonPlaceHolderApi jsonPlaceHolderApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        textView = (TextView)findViewById(R.id.textView);
        textView.setText(HomeActivity.nearestTollBooth.getName());
        textView2 = (TextView)findViewById(R.id.textView2);
        textView2.setText(ChooseVehicleActivity.vehicleId);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView3.setText(ChooseVehicleActivity.vehicleType);
        reference = FirebaseDatabase.getInstance().getReference().child(HomeActivity.userId).child("QRCode");
        jsonArray = HomeActivity.jsonArray;
        //Retrofit retrofit = new Retrofit.Builder().baseUrl("http://ec2-18-224-227-221.us-east-2.compute.amazonaws.com")
        //        .addConverterFactory(GsonConverterFactory.create()).build();
        //jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                if(jsonArray.getJSONObject(i).getString("name").equals(HomeActivity.nearestTollBooth.getName())) {
                    object = jsonArray.getJSONObject(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        button = (Button)findViewById(R.id.button);
        try {
            upiId = object.getString("upi_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            amount = object.getJSONObject("amounts").getInt(ChooseVehicleActivity.vehicleType);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pay();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sharedPreferences = this.getSharedPreferences("com.example.myapplication",Context.MODE_PRIVATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response "+resultCode );

       /*E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612*/

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }
    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(PaymentActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(PaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PaymentActivity.this,LoadingActivity.class).putExtra("Activity",1));
                //paymentComplete = true;
                //getQRCode();
                //sharedPreferences.edit().putBoolean("paymentComplete",true).apply();
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                Toast.makeText(PaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(PaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void pay() {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "Toll Booth")
                .appendQueryParameter("tn", "Toll")
                .appendQueryParameter("am", Integer.toString(amount))
                .appendQueryParameter("cu", "INR")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(PaymentActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    /*private void getQRCode() {
        Call<Post> call = jsonPlaceHolderApi.getQRCode(ChooseVehicleActivity.vehicleId);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if(!response.isSuccessful()) {
                    textView.setText("Code:" + response.code());
                    return;
                }
                HomeActivity.qrcode = response.body().getQrCode();
                //textView.setText(code);
                //createQRCode(code);
                //sharedPreferences.edit().putString("qrcode",code).apply();
                //sharedPreferences.edit().putString("vehicleId",ChooseVehicleActivity.vehicleId).apply();
                reference.child(reference.push().getKey()).setValue(HomeActivity.qrcode);
                startActivity(new Intent(PaymentActivity.this,ResponseActivity.class));
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                //textView.setText(t.getMessage());
            }
        });
    }*/

}
