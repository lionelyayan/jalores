package com.cantika.jalores.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.R;
import com.cantika.jalores.admin.adapter.AdminKasirAdapter;
import com.cantika.jalores.admin.model.KasirModel;
import com.cantika.jalores.customer.MainActivity;
import com.cantika.jalores.customer.adapter.MakananAdapter;
import com.cantika.jalores.customer.adapter.MinumanAdapter;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.helper.SweetLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdminKasirActivity extends AppCompatActivity implements AdminKasirAdapter.ListenerKasir {

    LinearLayout empty;
    RecyclerView recyclerView;
    SwipeRefreshLayout refresh;
    DatabaseReference myRef;
    SweetLoading loading;
    ImageView tambah_kasir;
    EditText cari, id, nama, email, password;
    boolean bRefresh=false;

    AdminKasirAdapter adapter;
    List<KasirModel> kasirModel = new ArrayList<>();
    KasirModel model = new KasirModel();

    Dialog dialog;
    TextView tambah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_kasir);

        deklarasi();
        eventListener();
        getData();
    }

    private void deklarasi() {
        myRef = FirebaseDatabase.getInstance().getReference();
        loading = new SweetLoading(this);

        empty = findViewById(R.id.empty);
        cari = findViewById(R.id.cari);
        tambah_kasir = findViewById(R.id.tambah_kasir);
        refresh = findViewById(R.id.refresh);
        recyclerView = findViewById(R.id.recycler_kasir);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_kasir_tambah);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        id = dialog.findViewById(R.id.id);
        nama = dialog.findViewById(R.id.nama);
        email = dialog.findViewById(R.id.email);
        password = dialog.findViewById(R.id.password);
        tambah = dialog.findViewById(R.id.tambah);
    }

    private void eventListener() {
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!bRefresh) {
                    if (adapter!=null) {
                        adapter.cari(cari.getText().toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        tambah_kasir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tambahKasir();
            }
        });
    }

    private void getData() {
        bRefresh=true;
        cari.setText("");
        refresh.setRefreshing(true);

        myRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bRefresh=false;
                refresh.setRefreshing(false);
                kasirModel.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    KasirModel model = ds.getValue(KasirModel.class);
                    if (model.getTitle().equals("Kasir")) {
                        kasirModel.add(model);
                    }
                }
                adapter = new AdminKasirAdapter(AdminKasirActivity.this, kasirModel, AdminKasirActivity.this);
                recyclerView.setAdapter(adapter);

                if (kasirModel.size() == 0) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                refresh.setRefreshing(false);
                bRefresh=false;
                Toast.makeText(AdminKasirActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tambahKasir() {
        Calendar todayDate = Calendar.getInstance();
        SimpleDateFormat formatID = new SimpleDateFormat("yyyyMMddHHmmss");
        String sId = formatID.format(todayDate.getTime());

        id.setText(sId);
        nama.setText("");
        email.setText("");
        password.setText("");
        tambah.setText("Tambah kasir");

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().isEmpty() || nama.getText().toString().isEmpty() ||
                        email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    Toast.makeText(AdminKasirActivity.this, "Lengkapi formulir", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();

                    model.setId(id.getText().toString());
                    model.setNama(nama.getText().toString());
                    model.setEmail(email.getText().toString());
                    model.setPassword(password.getText().toString());
                    model.setTitle("Kasir");

                    myRef.child("users").child(id.getText().toString()).setValue(model)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loading.dismiss();
                                    dialog.dismiss();
                                    if (task.isSuccessful()) {
                                        getData();
                                        Toast.makeText(AdminKasirActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminKasirActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        dialog.show();
    }

    @Override
    public void editKasir(KasirModel tmp) {
        id.setText(tmp.getId());
        nama.setText(tmp.getNama());
        email.setText(tmp.getEmail());
        password.setText(tmp.getPassword());
        tambah.setText("Update kasir");

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().isEmpty() || nama.getText().toString().isEmpty() ||
                        email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                    Toast.makeText(AdminKasirActivity.this, "Lengkapi formulir", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();

                    KasirModel kasirModel = new KasirModel();
                    kasirModel.setId(id.getText().toString());
                    kasirModel.setNama(nama.getText().toString());
                    kasirModel.setEmail(email.getText().toString());
                    kasirModel.setPassword(password.getText().toString());
                    kasirModel.setTitle("Kasir");

                    myRef.child("users").child(id.getText().toString()).setValue(kasirModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loading.dismiss();
                                    dialog.dismiss();
                                    if (task.isSuccessful()) {
                                        getData();
                                        Toast.makeText(AdminKasirActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminKasirActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        dialog.show();
    }

    @Override
    public void hapusKasir(KasirModel tmp, Dialog d) {
        loading.show();
        kasirModel.remove(tmp);
        myRef.child("users").child(tmp.getId()).setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();
                        d.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminKasirActivity.this, "Berhasil hapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminKasirActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        adapter.notifyDataSetChanged();
    }
}