package com.cantika.jalores.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.LoginActivity;
import com.cantika.jalores.LoginModel;
import com.cantika.jalores.R;
import com.cantika.jalores.customer.MainActivity;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.helper.SharedPref;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.kasir.KasirActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminActivity extends AppCompatActivity {

    CardView tambah_kasir, order, tambah_menu, lihat_kasir, lihat_menu;
    SweetLoading loading;
    TextView logout, ksrtambah, menutambah;
    EditText ksrnama, ksremail, ksrpassword, menunama, menuharga,alamat;

    Dialog dialogkasir, dialogmenu;

    FirebaseDatabase database;
    DatabaseReference reference;

    SharedPref sharedPref;
    CardView kasir, menu, laporan, pesanan;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sharedPref = new SharedPref(this);
        logout = findViewById(R.id.logout);
        kasir = findViewById(R.id.kasir);
        menu = findViewById(R.id.menu);
        pesanan = findViewById(R.id.pesanan);
        laporan = findViewById(R.id.laporan);

        kasir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this, AdminKasirActivity.class));
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this, AdminMenuActivity.class));
            }
        });

        pesanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminActivity.this, KasirActivity.class);
                i.putExtra("admin", "order");
                startActivity(i);
            }
        });

        laporan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminActivity.this, KasirActivity.class);
                i.putExtra("admin", "laporan");
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref.setString(sharedPref.USER, "");
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
            }
        });



        loading = new SweetLoading(this);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        tambah_kasir = findViewById(R.id.tambah_kasir);
        order = findViewById(R.id.order);
        tambah_menu = findViewById(R.id.tambah_menu);
        lihat_kasir = findViewById(R.id.lihat_kasir);
        lihat_menu = findViewById(R.id.lihat_menu);

        dialogkasir = new Dialog(this);
        dialogkasir.setContentView(R.layout.dialog_tambah_kasir);
        dialogkasir.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ksrnama = dialogkasir.findViewById(R.id.nama);
        ksremail = dialogkasir.findViewById(R.id.email);
        ksrpassword = dialogkasir.findViewById(R.id.password);
        alamat=dialogkasir.findViewById(R.id.alamat);
        ksrtambah = dialogkasir.findViewById(R.id.tambah);
        ksrtambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ksremail.getText().toString().isEmpty() || ksrpassword.getText().toString().isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();

                    LoginModel model = new LoginModel();
                    model.setEmail(ksremail.getText().toString());
                    model.setPassword(ksrpassword.getText().toString());
                    model.setTitle("Kasir");
                    model.setNama(ksrnama.getText().toString());
                    model.setAlamat(alamat.getText().toString());

                    reference.child("users").child(ksrnama.getText().toString()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loading.dismiss();
                            dialogkasir.dismiss();
                            if (task.isSuccessful()) {
                                finish();
                                Toast.makeText(AdminActivity.this, "Berhasil menambah kasir", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Gagal menambah kasir", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loading.dismiss();
                            Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        dialogmenu = new Dialog(this);
        dialogmenu.setContentView(R.layout.dialog_tambah_menu);
        dialogmenu.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        menunama = dialogmenu.findViewById(R.id.nama);
        menuharga = dialogmenu.findViewById(R.id.harga);
        menutambah = dialogmenu.findViewById(R.id.tambah);
        menutambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menunama.getText().toString().isEmpty() || menuharga.getText().toString().isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Nama dan harga harus diisi", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();

                    MenuModel model = new MenuModel();
                    model.setNama(menunama.getText().toString());
                    model.setHarga(Integer.parseInt(menuharga.getText().toString()));

                    reference.child("menu").child(menunama.getText().toString()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loading.dismiss();
                            if (task.isSuccessful()) {
                                menunama.setText("");
                                menuharga.setText("");
                                Toast.makeText(AdminActivity.this, "Berhasil menambah Menu", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AdminActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loading.dismiss();
                            Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


        tambah_kasir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogkasir.show();
            }
        });

        tambah_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogmenu.show();
            }
        });


        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminActivity.this, KasirActivity.class);
                i.putExtra("laporan", "Order");
                startActivity(i);
            }
        });

        lihat_kasir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminActivity.this, KasirActivity.class);
                i.putExtra("laporan","List Kasir");
                startActivity(i);
            }
        });

        lihat_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminActivity.this, MainActivity.class);
                i.putExtra("list","List");
                startActivity(i);
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