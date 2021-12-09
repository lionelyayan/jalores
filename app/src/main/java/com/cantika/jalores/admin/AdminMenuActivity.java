package com.cantika.jalores.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.R;
import com.cantika.jalores.admin.adapter.AdminKasirAdapter;
import com.cantika.jalores.admin.adapter.AdminMenuAdapter;
import com.cantika.jalores.admin.model.KasirModel;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.helper.TakePicture;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AdminMenuActivity extends AppCompatActivity implements AdminMenuAdapter.MenuListener, EasyPermissions.PermissionCallbacks {

    LinearLayout empty;
    RecyclerView recyclerView;
    SwipeRefreshLayout refresh;
    DatabaseReference myRef;
    SweetLoading loading;
    ImageView tambah_menu, foto;
    EditText cari, id, nama, harga, deskripsi;
    boolean bRefresh=false;
    AdminMenuAdapter adapter;
    List<MenuModel> menuModel = new ArrayList<>();

    Dialog dialog;
    CardView card_foto;
    RadioButton makanan, minuman;
    TextView tambah;
    String kategori="";
    MenuModel model = new MenuModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        deklarasi();
        eventListener();
        getData();
    }

    private void deklarasi() {
        myRef = FirebaseDatabase.getInstance().getReference();
        loading = new SweetLoading(this);

        empty = findViewById(R.id.empty);
        cari = findViewById(R.id.cari);
        tambah_menu = findViewById(R.id.tambah_menu);
        refresh = findViewById(R.id.refresh);
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_menu_tambah);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        foto = dialog.findViewById(R.id.foto);
        id = dialog.findViewById(R.id.id);
        nama = dialog.findViewById(R.id.nama);
        harga = dialog.findViewById(R.id.harga);
        deskripsi = dialog.findViewById(R.id.deskripsi);
        card_foto = dialog.findViewById(R.id.card_foto);
        makanan = dialog.findViewById(R.id.makanan);
        minuman = dialog.findViewById(R.id.minuman);
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

        tambah_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tambahMenu();
            }
        });
    }

    private void getData() {
        bRefresh=true;
        cari.setText("");
        refresh.setRefreshing(true);
        model.setId("");
        model.setNama("");
        model.setDeskripsi("");
        model.setHarga(0);
        model.setKategori("");
        model.setFoto("");

        myRef.child("menu").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bRefresh=false;
                refresh.setRefreshing(false);
                menuModel.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MenuModel model = ds.getValue(MenuModel.class);
                    menuModel.add(model);
                }
                adapter = new AdminMenuAdapter(AdminMenuActivity.this, menuModel, AdminMenuActivity.this);
                recyclerView.setAdapter(adapter);

                if (menuModel.size() == 0) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                refresh.setRefreshing(false);
                bRefresh=false;
                Toast.makeText(AdminMenuActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void tambahMenu() {
        Calendar todayDate = Calendar.getInstance();
        SimpleDateFormat formatID = new SimpleDateFormat("yyyyMMddHHmmss");
        String sId = formatID.format(todayDate.getTime());

        id.setText(sId);
        nama.setText("");
        harga.setText("");
        deskripsi.setText("");
        makanan.setChecked(true);
        foto.setImageBitmap(null);
        kategori="";
        tambah.setText("Tambah menu");

        card_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekPermission();
            }
        });

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().isEmpty() || nama.getText().toString().isEmpty() ||
                        harga.getText().toString().isEmpty() || deskripsi.getText().toString().isEmpty()) {
                    Toast.makeText(AdminMenuActivity.this, "Lengkapi formulir", Toast.LENGTH_SHORT).show();
                } else {
                    if (model.getFoto()==null || model.getFoto().equals("")) {
                        Toast.makeText(AdminMenuActivity.this, "Tambah foto menu dahulu", Toast.LENGTH_SHORT).show();
                    } else {
                        loading.show();
                        if (makanan.isChecked())
                            kategori="makanan";
                        else
                            kategori="minuman";

                        model.setId(id.getText().toString());
                        model.setNama(nama.getText().toString());
                        model.setDeskripsi(deskripsi.getText().toString());
                        model.setHarga(Integer.parseInt(harga.getText().toString()));
                        model.setKategori(kategori);

                        myRef.child("menu").child(id.getText().toString()).setValue(model)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        loading.dismiss();
                                        dialog.dismiss();
                                        if (task.isSuccessful()) {
                                            getData();
                                            Toast.makeText(AdminMenuActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AdminMenuActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }
        });

        dialog.show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Bitmap bitmap = (Bitmap) intent.getParcelableExtra("valueBitmap");
            if (bitmap!=null) {
                foto.setImageBitmap(bitmap);
                model.setFoto(bitmapToBase64(bitmap));
            } else {
                foto.setImageBitmap(null);
                model.setFoto("");
            }
        }
    };

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @AfterPermissionGranted(123)
    private void cekPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE};
        String[] perms1 = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms) && EasyPermissions.hasPermissions(this, perms1)) {
//            LocalBroadcastManager.getInstance(AdminMenuActivity.this)
//                    .registerReceiver(mMessageReceiver, new IntentFilter("bitmap"));
//            startActivity(new Intent(AdminMenuActivity.this, TakePicture.class));
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 777);
        } else {
            EasyPermissions.requestPermissions(this, "Dibutuhkan izin aplikasi", 123, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                bitmap= getResizedBitmap(bitmap, 700);
                model.setFoto(bitmapToBase64(bitmap));
                foto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void editMenu(MenuModel tmp) {
        id.setText(tmp.getId());
        nama.setText(tmp.getNama());
        deskripsi.setText(tmp.getDeskripsi());
        harga.setText(String.valueOf(tmp.getHarga()));
        tambah.setText("Update menu");

        byte[] decodedString = Base64.decode(tmp.getFoto(), Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        foto.setImageBitmap(bmp);
        model.setFoto(tmp.getFoto());

        if (tmp.getKategori().equals("minuman")) {
            kategori="minuman";
            minuman.setChecked(true);
        } else {
            kategori="makanan";
            makanan.setChecked(true);
        }

        card_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekPermission();
            }
        });

        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (id.getText().toString().isEmpty() || nama.getText().toString().isEmpty() ||
                        deskripsi.getText().toString().isEmpty() || harga.getText().toString().isEmpty()) {
                    Toast.makeText(AdminMenuActivity.this, "Lengkapi formulir", Toast.LENGTH_SHORT).show();
                } else {
                    loading.show();
                    if (makanan.isChecked())
                        kategori="makanan";
                    else
                        kategori="minuman";

                    model.setId(id.getText().toString());
                    model.setNama(nama.getText().toString());
                    model.setDeskripsi(deskripsi.getText().toString());
                    model.setHarga(Integer.parseInt(harga.getText().toString()));
                    model.setKategori(kategori);

                    myRef.child("menu").child(id.getText().toString()).setValue(model)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loading.dismiss();
                                    dialog.dismiss();
                                    if (task.isSuccessful()) {
                                        getData();
                                        Toast.makeText(AdminMenuActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AdminMenuActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        dialog.show();
    }

    @Override
    public void hapusMenu(MenuModel tmp, Dialog d) {
        loading.show();
        menuModel.remove(tmp);
        myRef.child("menu").child(tmp.getId()).setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();
                        d.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminMenuActivity.this, "Berhasil hapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminMenuActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        adapter.notifyDataSetChanged();
    }
}