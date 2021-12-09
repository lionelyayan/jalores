package com.cantika.jalores.admin.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.R;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.helper.SweetLoading;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdminMenuAdapter extends RecyclerView.Adapter<AdminMenuAdapter.ViewHolder> {

    Context context;
    List<MenuModel> data, arr;
    SweetLoading loading;
    MenuListener menuListener;
    Bitmap bmp;

    public AdminMenuAdapter(Context context, List<MenuModel> data, MenuListener menuListener) {
        this.context = context;
        this.data = data;
        this.menuListener = menuListener;

        arr = new ArrayList<>(data);
        loading = new SweetLoading(context);
    }

    @NonNull
    @Override
    public AdminMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminMenuAdapter.ViewHolder holder, int position) {
        MenuModel tmp = data.get(position);
        holder.nama.setText(tmp.getNama());
        holder.deskripsi.setText(tmp.getDeskripsi());
        holder.harga.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getHarga()))));

        byte[] decodedString = Base64.decode(tmp.getFoto(), Base64.DEFAULT);
        bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.foto.setImageBitmap(bmp);

        if (tmp.getKategori().equals("minuman")) {
            holder.kategori.setImageResource(R.drawable.ic_drink);
        } else {
            holder.kategori.setImageResource(R.drawable.ic_food);
        }

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_detail_menu);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView nama, deskripsi, harga, edit, hapus;
                ImageView foto, kategori;
                kategori = dialog.findViewById(R.id.kategori);
                foto = dialog.findViewById(R.id.foto);
                nama = dialog.findViewById(R.id.nama);
                deskripsi = dialog.findViewById(R.id.deskripsi);
                harga = dialog.findViewById(R.id.harga);
                edit = dialog.findViewById(R.id.edit);
                hapus = dialog.findViewById(R.id.hapus);

                nama.setText(tmp.getNama());
                deskripsi.setText(tmp.getDeskripsi());
                harga.setText(loading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getHarga()))));

                byte[] decodedString = Base64.decode(tmp.getFoto(), Base64.DEFAULT);
                bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                foto.setImageBitmap(bmp);

                if (tmp.getKategori().equals("minuman")) {
                    kategori.setImageResource(R.drawable.ic_drink);
                } else {
                    kategori.setImageResource(R.drawable.ic_food);
                }

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        menuListener.editMenu(tmp);
                        dialog.dismiss();
                    }
                });

                hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialog.setTitle("Kamu yakin mau menghapus menu "+tmp.getNama()+"..?");
                        sweetAlertDialog.setConfirmButton("Hapus", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                menuListener.hapusMenu(tmp, dialog);
                                sweetAlertDialog.dismiss();
                            }
                        });
                        sweetAlertDialog.setCancelButton("Batal", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        });
                        sweetAlertDialog.show();
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView item_layout;
        ImageView kategori, foto;
        TextView nama, deskripsi, harga;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_layout = itemView.findViewById(R.id.item_layout);
            kategori = itemView.findViewById(R.id.kategori);
            foto = itemView.findViewById(R.id.foto);
            nama = itemView.findViewById(R.id.nama);
            deskripsi = itemView.findViewById(R.id.deskripsi);
            harga = itemView.findViewById(R.id.harga);
        }
    }

    public void cari (String value) {
        value = value.toLowerCase(Locale.getDefault());
        data.clear();

        if (value.isEmpty()) {
            data.addAll(arr);
        } else {
            for (MenuModel m : arr) {
                if (m.getNama().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface MenuListener {
        void editMenu(MenuModel tmp);
        void hapusMenu(MenuModel tmp, Dialog dialog);
    }
}
