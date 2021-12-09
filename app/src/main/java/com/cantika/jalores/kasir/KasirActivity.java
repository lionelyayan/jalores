package com.cantika.jalores.kasir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cantika.jalores.LoginActivity;
import com.cantika.jalores.LoginModel;
import com.cantika.jalores.R;
import com.cantika.jalores.admin.AdminActivity;
import com.cantika.jalores.admin.AdminMenuActivity;
import com.cantika.jalores.admin.KasirAdapter;
import com.cantika.jalores.customer.MainActivity;
import com.cantika.jalores.customer.TransaksiModel;
import com.cantika.jalores.customer.adapter.CartAdapter;
import com.cantika.jalores.customer.model.PesananModel;
import com.cantika.jalores.helper.SharedPref;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.helper.TakePicture;
import com.cantika.jalores.kasir.adapter.TransaksiAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class KasirActivity extends AppCompatActivity implements TransaksiAdapter.KasirListener, CartAdapter.Delete,
        KasirAdapter.Edit, EasyPermissions.PermissionCallbacks {

    EditText cari;
    RecyclerView recyclerView;
    PesananAdapter adapter;
    KasirAdapter adapter1;
    TextView logout, title;

    List<LoginModel> datakasir = new ArrayList<>();


    ImageView print, printStruk;
    SharedPref sharedPref;
    DatabaseReference myRef;
    SweetLoading loading;
    SwipeRefreshLayout refresh;
    List<PesananModel> pesananModel = new ArrayList<>();
    List<PesananModel> order = new ArrayList<>();
    List<PesananModel> laporan = new ArrayList<>();
    List<String> pesananMenu = new ArrayList<>();
    TransaksiAdapter transaksiAdapter;
    LinearLayout empty;
    Dialog dialogDetail, dialogPrint;
    RecyclerView recycler_detail;
    TextView pesan, back, keterangan, id, subtotal, minggu, bulan;

    boolean doubleBackToExitPressedOnce = false;
    File pdfFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kasir);

        deklarasi();
        eventListener();
        getData();
    }

    private void deklarasi() {
        sharedPref = new SharedPref(this);
        loading = new SweetLoading(this);
        myRef = FirebaseDatabase.getInstance().getReference();

        refresh = findViewById(R.id.refresh);
        empty = findViewById(R.id.empty);
        title = findViewById(R.id.title);
        logout = findViewById(R.id.logout);
        cari = findViewById(R.id.cari);
        print = findViewById(R.id.print);
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        if (getIntent().getStringExtra("admin")!=null) {
            logout.setVisibility(View.GONE);
            if (getIntent().getStringExtra("admin").equals("laporan")) {
                print.setVisibility(View.VISIBLE);
                title.setText("Laporan");
            } else {
                print.setVisibility(View.GONE);
                title.setText("Pesanan");
            }
        } else {
            logout.setVisibility(View.VISIBLE);
            print.setVisibility(View.GONE);
            title.setText("Halaman Kasir");
        }

        dialogDetail = new Dialog(this);
        dialogDetail.setContentView(R.layout.dialog_cart);
        dialogDetail.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        recycler_detail = dialogDetail.findViewById(R.id.recycler_pesanan);
        recycler_detail.setHasFixedSize(true);
        pesan = dialogDetail.findViewById(R.id.pesan);
        back = dialogDetail.findViewById(R.id.back);
        keterangan = dialogDetail.findViewById(R.id.keterangan);
        id = dialogDetail.findViewById(R.id.id);
        subtotal = dialogDetail.findViewById(R.id.subtotal);
        printStruk = dialogDetail.findViewById(R.id.print);
        if (getIntent().getStringExtra("admin")!=null) {
            printStruk.setVisibility(View.GONE);
        } else {
            printStruk.setVisibility(View.VISIBLE);
        }

        dialogPrint = new Dialog(KasirActivity.this);
        dialogPrint.setContentView(R.layout.dialog_pilih_print);
        dialogPrint.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        minggu = dialogPrint.findViewById(R.id.minggu);
        bulan = dialogPrint.findViewById(R.id.bulan);
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
                if (transaksiAdapter!=null) {
                    transaksiAdapter.cari(cari.getText().toString());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPref.setString(sharedPref.USER, "");
                startActivity(new Intent(KasirActivity.this, LoginActivity.class));
                finish();
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minggu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cekPermission(null);
                    }
                });

                bulan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cekPermission(null);
                    }
                });

                dialogPrint.show();
            }
        });
    }

    private void getData() {
        refresh.setRefreshing(true);

        myRef.child("transaksi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                refresh.setRefreshing(false);
                pesananModel.clear();
                pesananMenu.clear();
                order.clear();
                laporan.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PesananModel model = ds.getValue(PesananModel.class);
                    pesananModel.add(model);
                    if (model.getStatus().equals("selesai"))
                        laporan.add(model);
                    if (!model.getStatus().equals("selesai"))
                        order.add(model);
                }

                if (pesananModel.size()>0) {
                    if (getIntent().getStringExtra("admin")!=null) {
                        pesananModel.clear();
                        if (getIntent().getStringExtra("admin").equals("laporan"))
                            pesananModel.addAll(laporan);
                        else
                            pesananModel.addAll(order);
                    }
                    empty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    transaksiAdapter = new TransaksiAdapter(KasirActivity.this, pesananModel, KasirActivity.this);
                    recyclerView.setAdapter(transaksiAdapter);
                } else {
                    empty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                refresh.setRefreshing(false);
            }
        });
    }

    private void createPdf(PesananModel tmp) throws DocumentException {
        loading.show();
        Calendar todayDate = Calendar.getInstance();
        SimpleDateFormat formatID = new SimpleDateFormat("yyyyMMddHHmmss");
        String sId = formatID.format(todayDate.getTime());

        // write the document content
        pdfFile=null;
        if (tmp==null) {
            pdfFile = new File(getExternalFilesDir(null),sId+".pdf");
        } else {
            pdfFile = new File(getExternalFilesDir(null),"/Struk_"+tmp.getId()+".pdf");
        }
        FileOutputStream outputStream =null;
        try {
            outputStream = new FileOutputStream(pdfFile);

            Document document = new Document(PageSize.A4);
            PdfPTable table=null;
            if (tmp==null) {
                table = new PdfPTable(new float[]{3, 3, 3, 3});
            } else {
                table = new PdfPTable(new float[]{3, 3, 3});
            }

            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setFixedHeight(50);
            table.setTotalWidth(PageSize.A4.getWidth());
            table.setWidthPercentage(100);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            if (tmp==null) {
                table.addCell("ID");
                table.addCell("Tanggal");
                table.addCell("Pesanan");
                table.addCell("Total");
            } else {
                table.addCell("Qty");
                table.addCell("Nama Menu");
                table.addCell("Harga");
            }
            table.setHeaderRows(1);

            PdfPCell[] cells = table.getRow(0).getCells();
            for (PdfPCell cell : cells) {
                cell.setBackgroundColor(BaseColor.GRAY);
            }

            int iPendapatan=0;
            if (tmp==null) {
                for (int i=0; i<pesananModel.size(); i++) {
                    StringBuilder pesanan= new StringBuilder();
                    String s = pesananModel.get(i).getPesanan().substring(1, pesananModel.get(i).getPesanan().length() - 1);
                    List<String> myList = new ArrayList<String>(Arrays.asList(s.split(", ")));
                    for (int j=0; j<myList.size(); j++) {
                        String[] sArr = myList.get(j).split("\\|");
                        pesanan.append(sArr[1]).append(" x ").append(sArr[0]).append("\n");
                    }

                    table.addCell(pesananModel.get(i).getId());
                    table.addCell(pesananModel.get(i).getTanggal());
                    table.addCell(pesanan.toString());
                    table.addCell(loading.formatRupiah(Double.parseDouble(String.valueOf(pesananModel.get(i).getTotal()))));

                    iPendapatan = iPendapatan + pesananModel.get(i).getTotal();
                }
            } else {
                String s = tmp.getPesanan().substring(1, tmp.getPesanan().length() - 1);
                List<String> myList = new ArrayList<>(Arrays.asList(s.split(", ")));
                for (int j=0; j<myList.size(); j++) {
                    String[] sArr = myList.get(j).split("\\|");
                    table.addCell(sArr[1]);
                    table.addCell(sArr[0]);
                    table.addCell(loading.formatRupiah(Double.parseDouble(String.valueOf(sArr[2]))));
                }
                iPendapatan = tmp.getTotal();
            }

            PdfWriter.getInstance(document, outputStream);

            document.open();
            Font judul = new Font(Font.FontFamily.TIMES_ROMAN, 30.0f, Font.UNDERLINE, BaseColor.BLUE);
            Font total = new Font(Font.FontFamily.TIMES_ROMAN, 20.0f, Font.NORMAL, BaseColor.BLUE);
            if (tmp==null) {
                document.add(new Paragraph("Laporan Penjualan \n\n", judul));
                document.add(new Paragraph("Total Pendapatan "+
                        loading.formatRupiah(Double.parseDouble(String.valueOf(iPendapatan)))+"\n\n", total));
            } else {
                document.add(new Paragraph("ID Pesanan "+tmp.getId()+"\n\n", judul));
                document.add(new Paragraph("Total "+
                        loading.formatRupiah(Double.parseDouble(String.valueOf(iPendapatan)))+"\n\n", total));
            }
            document.add(table);
            document.close();

            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
            sweetAlertDialog.setCancelable(false);
            sweetAlertDialog.setTitle("Berhasil..");
            sweetAlertDialog.setContentText("Lokasi file : Android/data/com.cantika.jalores/files/");
            sweetAlertDialog.setConfirmButton("OKE", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    loading.dismiss();
                    if (tmp==null) {
                        dialogPrint.dismiss();
                    }
                }
            });

            sweetAlertDialog.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDetail(PesananModel tmp) {
        pesananMenu.clear();
        for (int i=0; i<pesananModel.size(); i++) {
            if (pesananModel.get(i).getId().equals(tmp.getId())) {
                String sPesan = pesananModel.get(i).getPesanan().substring(1, pesananModel.get(i).getPesanan().length() - 1);
                String[] sArr = sPesan.split(", ");
                pesananMenu.addAll(Arrays.asList(sArr));
                break;
            }
        }
        recycler_detail.setAdapter(new CartAdapter(KasirActivity.this, pesananMenu, KasirActivity.this, sharedPref,"kasir"));

        id.setText(tmp.getId());
        subtotal.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getTotal()))));
        if (getIntent().getStringExtra("admin")!=null) {
            keterangan.setVisibility(View.GONE);
            back.setVisibility(View.GONE);
            pesan.setVisibility(View.GONE);
        } else {
            if (!tmp.getStatus().equals("selesai")) {
                pesan.setVisibility(View.VISIBLE);
                keterangan.setVisibility(View.VISIBLE);
                back.setVisibility(View.VISIBLE);
                keterangan.setText("Pilih aksi untuk pesanan ini");
                if (tmp.getStatus().equals("menunggu")) {
                    pesan.setText("Proses");
                    pesan.setBackgroundResource(R.drawable.rounded_yellow_1);
                } else {
                    pesan.setText("Selesai");
                    pesan.setBackgroundResource(R.drawable.rounded_green);
                }
            } else {
                keterangan.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
                pesan.setVisibility(View.GONE);
            }
        }

        pesan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //aksi dari kasir
                if (tmp.getStatus().equals("menunggu")) {
                    myRef.child("transaksi").child(tmp.getId()).child("status").setValue("hidangkan");
                } else {
                    myRef.child("transaksi").child(tmp.getId()).child("status").setValue("selesai");
                }
                dialogDetail.dismiss();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tambah pesanan customer
                Intent i = new Intent(KasirActivity.this, MainActivity.class);
                i.putExtra("tambah", "tambah");
                i.putExtra("id", tmp.getId());
                startActivity(i);
                dialogDetail.dismiss();
            }
        });

        printStruk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cekPermission(tmp);
                dialogDetail.dismiss();
            }
        });

        dialogDetail.show();
    }

    @Override
    public void editKasir(LoginModel data, SweetLoading loading, String password) {
        loading.show();
        for (int i=0; i<datakasir.size(); i++) {
            if (datakasir.get(i).getNama().equals(data.getNama())) {
                datakasir.get(i).setPassword(password);
                adapter1.notifyDataSetChanged();
                break;
            }
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(data.getNama())
                .child("password").setValue(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loading.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(KasirActivity.this, "Berhasil edit kasir", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(KasirActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void hapusKasir(LoginModel data, SweetLoading loading) {
        loading.show();
        datakasir.remove(data);
        adapter1.notifyDataSetChanged();
        FirebaseDatabase.getInstance().getReference().child("users").child(data.getNama()).setValue(null)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loading.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(KasirActivity.this, "Berhasil hapus kasir", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(KasirActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void deleteCart(String s) {
        //
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getStringExtra("admin")==null) {
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
        } else {
            super.onBackPressed();
        }
    }

    @AfterPermissionGranted(123)
    private void cekPermission(PesananModel tmp) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms)) {
            if (pesananModel.size()>0) {
                try {
                    createPdf(tmp);
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Datanya masih kosong", Toast.LENGTH_SHORT).show();
            }
        } else {
            EasyPermissions.requestPermissions(this, "Dibutuhkan izin penyimpanan", 123, perms);
        }
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
}