package com.cantika.jalores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.admin.AdminActivity;
import com.cantika.jalores.customer.MainActivity;
import com.cantika.jalores.customer.TransaksiModel;
import com.cantika.jalores.helper.SharedPref;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.kasir.KasirActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    TextView customer, kasir, admin, title, login;
    EditText email, password;
    CardView card_login;
    ImageView close;

    SweetLoading loading;
    SharedPref sharedPref;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loading = new SweetLoading(this);
        sharedPref = new SharedPref(this);

        if (sharedPref.getUSER()!=null) {
            if (!sharedPref.getUSER().equals("")) {
                if (sharedPref.getUSER().equals("Kasir")) {
                    startActivity(new Intent(LoginActivity.this, KasirActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                }
                finish();
            }
        }

        customer = findViewById(R.id.customer);
        kasir = findViewById(R.id.kasir);
        admin = findViewById(R.id.admin);
        title = findViewById(R.id.title);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        card_login = findViewById(R.id.card_login);
        close = findViewById(R.id.close);

        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });

        kasir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card_login.setVisibility(View.VISIBLE);
                close.setVisibility(View.VISIBLE);
                title.setText("Login Kasir");
                email.setText("");
                password.setText("");
            }
        });

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card_login.setVisibility(View.VISIBLE);
                close.setVisibility(View.VISIBLE);
                title.setText("Login Admin");
                email.setText("");
                password.setText("");
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Lengkapi email dan password", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();
                    String[] log = title.getText().toString().split(" ");

                    FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            loading.dismiss();
                            List<LoginModel> loginModels = new ArrayList<>();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                LoginModel model = ds.getValue(LoginModel.class);
                                loginModels.add(model);
                            }

                            for (int i=0; i<loginModels.size(); i++) {
                                if (loginModels.get(i).getTitle().equals(log[1])
                                        && loginModels.get(i).getEmail().equals(email.getText().toString())
                                        && loginModels.get(i).getPassword().equals(password.getText().toString())) {
                                    if (log[1].equals("Admin")) {
                                        sharedPref.setString(sharedPref.USER, "Admin");
                                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                    } else {
                                        sharedPref.setString(sharedPref.USER, "Kasir");
                                        startActivity(new Intent(LoginActivity.this, KasirActivity.class));
                                    }
                                    card_login.setVisibility(View.GONE);
                                    close.setVisibility(View.GONE);
                                    finish();
                                    break;
                                }
                                if (i==loginModels.size()-1) {
                                    Toast.makeText(LoginActivity.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loading.dismiss();
                        }
                    });
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card_login.setVisibility(View.GONE);
                close.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}