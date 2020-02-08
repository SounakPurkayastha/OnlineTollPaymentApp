package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity  {

    EditText etUsername, etPassword;
    Button button;
    TextView tv1, login, map;
    boolean isSignup = true;
    long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null && user.isEmailVerified()) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        button = findViewById(R.id.button);
        tv1 = findViewById(R.id.tv1);
        login = findViewById(R.id.login);
        map = findViewById(R.id.map);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSignup) {
                    button.setText("LOGIN");
                    tv1.setText("Don't have an account? ");
                    login.setText("Signup");
                    isSignup = false;
                }
                else {
                    button.setText("SIGNUP");
                    tv1.setText("Already have an account? ");
                    login.setText("Login");
                    isSignup = true;
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkConnection()) {
                    Toast.makeText(MainActivity.this,"No internet connection",Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = etUsername.getText().toString(),
                        password = etPassword.getText().toString();
                if(username.equals("")) {
                    Toast.makeText(MainActivity.this,"Usernmae needed",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.equals("")) {
                    Toast.makeText(MainActivity.this,"Password needed",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isSignup) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this,"Signup successful",Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(MainActivity.this,SuccessActivity.class));
                                        }
                                        else  {
                                            Toast.makeText(MainActivity.this,"Something went wrong",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }
                            else
                                Toast.makeText(MainActivity.this,"Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(MainActivity.this,"Please verify email",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                                Toast.makeText(MainActivity.this,"Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    @Override
    public void onBackPressed() {
        if (backPressedTime + 1000 > System.currentTimeMillis()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
        else {
            Toast.makeText(MainActivity.this,"Press back again to exit",Toast.LENGTH_SHORT).show();
        }

        backPressedTime = System.currentTimeMillis();
    }

}
