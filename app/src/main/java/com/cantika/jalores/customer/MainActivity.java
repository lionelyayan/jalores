package com.cantika.jalores.customer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.R;
import com.cantika.jalores.customer.adapter.CartAdapter;
import com.cantika.jalores.customer.adapter.MakananAdapter;
import com.cantika.jalores.customer.adapter.MinumanAdapter;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.customer.model.PesananModel;
import com.cantika.jalores.helper.SharedPref;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MinumanAdapter.PesanMinum,
        MakananAdapter.PesanMakan, CartAdapter.Delete, MenuAdapter.Edit{

    SweetLoading loading;

    EditText cari;
    RecyclerView recycler_makanan, recycler_minuman, recycler_cart;
    DatabaseReference myRef;
    List<MenuModel> data = new ArrayList<>();
    MenuAdapter adapter;
    SharedPref sharedPref;
    SwipeRefreshLayout refresh;
    LinearLayout makanan, minuman;
    ImageView cart, tambah, kurang;
    TextView line_makanan, line_minuman, jumlah, oke, pesan, back, status, note, keterangan, id, subtotal;

    Animation in_to_left, in_to_right, out_to_left, out_to_right;

    MakananAdapter makananAdapter;
    MinumanAdapter minumanAdapter;
    List<MenuModel> listMakanan = new ArrayList<>();
    List<MenuModel> listMinuman = new ArrayList<>();
    List<PesananModel> pesananModel = new ArrayList<>();
    List<String> pesananMenu = new ArrayList<>();
    CartAdapter cartAdapter;

    Dialog dialogTambah, dialogCart;
    boolean bRefresh=false;
    int iTotal=0;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deklarasi();
        eventListener();
        getData();
    }

    private void deklarasi() {
        loading = new SweetLoading(this);
        sharedPref = new SharedPref(this);
        myRef = FirebaseDatabase.getInstance().getReference();

        status = findViewById(R.id.status);
        note = findViewById(R.id.note);
        refresh = findViewById(R.id.refresh);
        cari = findViewById(R.id.cari);
        makanan = findViewById(R.id.makanan);
        minuman = findViewById(R.id.minuman);
        line_makanan = findViewById(R.id.line_makanan);
        line_minuman = findViewById(R.id.line_minuman);
        cart = findViewById(R.id.cart);
        recycler_makanan = findViewById(R.id.recycler_makanan);
        recycler_makanan.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recycler_makanan.setHasFixedSize(true);
        recycler_minuman = findViewById(R.id.recycler_minuman);
        recycler_minuman.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recycler_minuman.setHasFixedSize(true);

        line_makanan.setVisibility(View.VISIBLE);
        line_minuman.setVisibility(View.GONE);
        recycler_makanan.setVisibility(View.VISIBLE);
        recycler_minuman.setVisibility(View.GONE);

        in_to_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right_to_left);
        in_to_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_left_to_right);
        out_to_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_from_right_to_left);
        out_to_right = AnimationUtils.loadAnimation(this, R.anim.slide_out_from_left_to_right);

        dialogTambah = new Dialog(this);
        dialogTambah.setContentView(R.layout.dialog_jumlah);
        dialogTambah.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        tambah = dialogTambah.findViewById(R.id.tambah);
        kurang = dialogTambah.findViewById(R.id.kurang);
        jumlah = dialogTambah.findViewById(R.id.jumlah);
        oke = dialogTambah.findViewById(R.id.oke);

        dialogCart = new Dialog(this);
        dialogCart.setContentView(R.layout.dialog_cart);
        dialogCart.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        recycler_cart = dialogCart.findViewById(R.id.recycler_pesanan);
        recycler_cart.setHasFixedSize(true);
        pesan = dialogCart.findViewById(R.id.pesan);
        back = dialogCart.findViewById(R.id.back);
        keterangan = dialogCart.findViewById(R.id.keterangan);
        id = dialogCart.findViewById(R.id.id);
        subtotal = dialogCart.findViewById(R.id.subtotal);
    }

    private void eventListener() {
        cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!bRefresh) {
                    if (recycler_makanan.isShown()) {
                        if (makananAdapter!=null) {
                            makananAdapter.cari(cari.getText().toString());
                        }
                    } else {
                        if (minumanAdapter!=null) {
                            minumanAdapter.cari(cari.getText().toString());
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        makanan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (line_minuman.isShown()) {
                    line_makanan.startAnimation(in_to_left);
                    line_makanan.setVisibility(View.VISIBLE);
                    recycler_makanan.startAnimation(in_to_left);
                    recycler_makanan.setVisibility(View.VISIBLE);

                    line_minuman.startAnimation(out_to_left);
                    line_minuman.setVisibility(View.GONE);
                    recycler_minuman.startAnimation(out_to_left);
                    recycler_minuman.setVisibility(View.GONE);
                }
            }
        });

        minuman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (line_makanan.isShown()) {
                    line_makanan.startAnimation(out_to_right);
                    line_makanan.setVisibility(View.GONE);
                    recycler_makanan.startAnimation(out_to_right);
                    recycler_makanan.setVisibility(View.GONE);

                    line_minuman.startAnimation(in_to_right);
                    line_minuman.setVisibility(View.VISIBLE);
                    recycler_minuman.startAnimation(in_to_right);
                    recycler_minuman.setVisibility(View.VISIBLE);
                }
            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pesananMenu.size()>0) {
                    tampilCart(pesananMenu);
                } else {
                    Toast.makeText(MainActivity.this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getData() {
        bRefresh=true;
        cari.setText("");
        refresh.setRefreshing(true);
        myRef.child("menu").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bRefresh=false;
                refresh.setRefreshing(false);
                listMakanan.clear();
                listMinuman.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MenuModel model = ds.getValue(MenuModel.class);
                    if (model.getKategori().equals("makanan")) {
                        listMakanan.add(model);
                    } else {
                        listMinuman.add(model);
                    }
                }
                makananAdapter = new MakananAdapter(listMakanan, MainActivity.this, MainActivity.this);
                recycler_makanan.setAdapter(makananAdapter);
                minumanAdapter = new MinumanAdapter(listMinuman, MainActivity.this, MainActivity.this);
                recycler_minuman.setAdapter(minumanAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                refresh.setRefreshing(false);
                bRefresh=false;
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        myRef.child("transaksi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pesananModel.clear();
                pesananMenu.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PesananModel model = ds.getValue(PesananModel.class);
                    pesananModel.add(model);
                }

                setCart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCart() {
        if (pesananModel.size()>0) {
            for (int i=0; i<pesananModel.size(); i++) {
                //tambah pesanan dari kasir
                if (getIntent().getStringExtra("tambah")!=null) {
                    if (pesananModel.get(i).getId().equals(getIntent().getStringExtra("id"))
                            && !pesananModel.get(i).getStatus().equals("selesai")) {
                        String sPesan = pesananModel.get(i).getPesanan().substring(1, pesananModel.get(i).getPesanan().length() - 1);
                        String[] sArr = sPesan.split(", ");
                        pesananMenu.addAll(Arrays.asList(sArr));
                        tambah.setEnabled(true);
                        kurang.setEnabled(true);
                        oke.setEnabled(true);
                        back.setVisibility(View.VISIBLE);
                        pesan.setVisibility(View.VISIBLE);
                        id.setText(pesananModel.get(i).getId());
                        subtotal.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(pesananModel.get(i).getTotal()))));
                        status.setText("ID : "+pesananModel.get(i).getId());
                        note.setText("*Silahkan pilih menu tambahan");
                        keterangan.setText("Mau nambah menu lagi..?");
                        break;
                    }
                }
                //set cart customer
                else {
                    if (sharedPref.getID()!=null) {
                        if (pesananModel.get(i).getId().equals(sharedPref.getID())) {
                            id.setText("Pesanan Anda");
                            if (!pesananModel.get(i).getStatus().equals("selesai")) {
                                String sPesan = pesananModel.get(i).getPesanan().substring(1, pesananModel.get(i).getPesanan().length() - 1);
                                String[] sArr = sPesan.split(", ");
                                pesananMenu.addAll(Arrays.asList(sArr));
                                tambah.setEnabled(false);
                                kurang.setEnabled(false);
                                oke.setEnabled(false);
                                back.setVisibility(View.GONE);
                                pesan.setVisibility(View.GONE);
                                subtotal.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(pesananModel.get(i).getTotal()))));
                                if (pesananModel.get(i).getStatus().equals("menunggu")) {
                                    status.setText("Status : Menunggu diproses");
                                    note.setText("*Hubungi kasir untuk menambah pesanan");
                                    keterangan.setText("Menunggu diproses kasir");
                                } else {
                                    status.setText("Status : Selamat menikmati");
                                    note.setText("*Selamat menyantap hidangan");
                                    keterangan.setText("Selamat menikmati");
                                }
                            }
                            else {
                                status.setText("Status : Belum pesan");
                                note.setText("*Pilih menu kesukaan kamu");
                                back.setVisibility(View.VISIBLE);
                                pesan.setVisibility(View.VISIBLE);
                                keterangan.setText("Mau nambah menu lagi..?");
                                pesananMenu.clear();
                                back.setVisibility(View.VISIBLE);
                                pesan.setVisibility(View.VISIBLE);
                                tambah.setEnabled(true);
                                kurang.setEnabled(true);
                                oke.setEnabled(true);
                                sharedPref.prefClear();
                            }
                            break;
                        }
                    }
                }

                if (i==pesananModel.size()-1) {
                    id.setText("Pesanan Anda");
                    subtotal.setText(loading.formatRupiah(Double.parseDouble("0")));
                    status.setText("Status : Belum pesan");
                    note.setText("*Pilih menu kesukaan kamu");
                    back.setVisibility(View.VISIBLE);
                    pesan.setVisibility(View.VISIBLE);
                    keterangan.setText("Mau nambah menu lagi..?");
                    pesananMenu.clear();
                    back.setVisibility(View.VISIBLE);
                    pesan.setVisibility(View.VISIBLE);
                    tambah.setEnabled(true);
                    kurang.setEnabled(true);
                    oke.setEnabled(true);
                }
            }
        }
    }

    private void tampilCart(List<String> menu) {
        cartAdapter = new CartAdapter(MainActivity.this, menu, MainActivity.this, sharedPref,"customer");
        recycler_cart.setAdapter(cartAdapter);
        dialogCart.show();

        iTotal=0;
        for (int i = 0; i<pesananMenu.size(); i++) {
            String[] sArr = pesananMenu.get(i).split("\\|");
            iTotal = iTotal + (Integer.parseInt(sArr[1]) * Integer.parseInt(sArr[2]));
        }
        subtotal.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(iTotal))));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCart.dismiss();
            }
        });

        pesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();

                Calendar todayDate = Calendar.getInstance();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat formatID = new SimpleDateFormat("yyyyMMddHHmmss");
                String tgl = formatter.format(todayDate.getTime());
                String sId = formatID.format(todayDate.getTime());

                PesananModel pesananModel = new PesananModel();
                if (getIntent().getStringExtra("tambah")==null) {
                    pesananModel.setId(sId);
                } else {
                    pesananModel.setId(id.getText().toString());
                }
                pesananModel.setTanggal(tgl);
                pesananModel.setPesanan(pesananMenu.toString());
                pesananModel.setTotal(iTotal);
                pesananModel.setStatus("menunggu");
                sharedPref.setString(sharedPref.ID, pesananModel.getId());
                sharedPref.setString(sharedPref.STATUS, pesananModel.getStatus());

                myRef.child("transaksi").child(pesananModel.getId()).setValue(pesananModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                loading.dismiss();
                                if (task.isSuccessful()) {
                                    if (getIntent().getStringExtra("tambah")==null) {
                                        dialogCart.dismiss();
                                        pesananMenu.clear();
                                        status.setText("Status : Menunggu diproses");
                                        note.setText("*Hubungi kasir untuk menambah pesanan");
                                        getData();
                                    } else {
                                        finish();
                                    }
                                    Toast.makeText(MainActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void tambahMakan(MenuModel model) {
        tambahPesanan(model);
    }

    @Override
    public void tambahMinum(MenuModel model) {
        tambahPesanan(model);
    }

    private void tambahPesanan(MenuModel model) {
        if (!status.getText().toString().equals("Status : Belum pesan") && getIntent().getStringExtra("tambah")==null) {
            Toast.makeText(this, "Pesanan sedang berlangsung", Toast.LENGTH_SHORT).show();
        } else {
            dialogTambah.show();

            jumlah.setText("0");
            if (pesananMenu.size()>0) {
                for (int i=0; i<pesananMenu.size(); i++) {
                    String[] sArr = pesananMenu.get(i).split("\\|");
                    if (sArr[0].equals(model.getNama())) {
                        jumlah.setText(sArr[1]);
                    }
                }
            }

            tambah.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int iJml = Integer.parseInt(jumlah.getText().toString());
                    if (iJml<=1000) {
                        iJml = iJml +1;
                    }
                    jumlah.setText(String.valueOf(iJml));
                }
            });

            kurang.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int iJml = Integer.parseInt(jumlah.getText().toString());
                    if (iJml>0) {
                        iJml = iJml -1;
                    }
                    jumlah.setText(String.valueOf(iJml));
                }
            });

            oke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pesananMenu.size()>0) {
                        for (int i=0; i<pesananMenu.size(); i++) {
                            String[] sArr = pesananMenu.get(i).split("\\|");
                            if (sArr[0].equals(model.getNama())) {
                                if (jumlah.getText().toString().equals("0")) {
                                    pesananMenu.remove(pesananMenu.get(i));
                                } else {
                                    pesananMenu.set(i, model.getNama()+"|"+jumlah.getText().toString()+"|"+model.getHarga());
                                }
                                break;
                            }

                            if (i==pesananMenu.size()-1) {
                                pesananMenu.add(model.getNama()+"|"+jumlah.getText().toString()+"|"+model.getHarga());
                            }
                        }
                    } else {
                        pesananMenu.add(model.getNama()+"|"+jumlah.getText().toString()+"|"+model.getHarga());
                    }

                    Toast.makeText(MainActivity.this, "Berhasil update pesanan", Toast.LENGTH_SHORT).show();
                    dialogTambah.dismiss();
                }
            });
        }
    }

    @Override
    public void deleteCart(String s) {
        pesananMenu.remove(s);
        String[] sArr = s.split("\\|");
        iTotal = iTotal - (Integer.parseInt(sArr[1]) * Integer.parseInt(sArr[2]));
        subtotal.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(iTotal))));

        if (pesananMenu.size()==0) {
            dialogCart.dismiss();
        }
        cartAdapter.notifyDataSetChanged();
    }

    @Override
    public void editMenu(MenuModel menuModel, SweetLoading loading, String harga) {
        loading.show();
        for (int i=0; i<data.size(); i++) {
            if (data.get(i).getNama().equals(menuModel.getNama())) {
                data.get(i).setHarga(Integer.parseInt(harga));
                adapter.notifyDataSetChanged();
                break;
            }
        }
        myRef.child("menu").child(menuModel.getNama())
                .child("harga").setValue(Integer.parseInt(harga))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Berhasil edit kasir", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void hapusMenu(MenuModel menuModel, SweetLoading loading) {
        loading.show();
        data.remove(menuModel);
        adapter.notifyDataSetChanged();
        myRef.child("menu").child(menuModel.getNama()).setValue(null)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Berhasil hapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                        }
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