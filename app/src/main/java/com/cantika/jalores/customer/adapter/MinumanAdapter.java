package com.cantika.jalores.customer.adapter;

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

public class MinumanAdapter extends RecyclerView.Adapter<MinumanAdapter.ViewHolder> {

    List<MenuModel> data, tmp;
    Context context;
    SweetLoading sweetLoading;
    PesanMinum pesanan;

    public MinumanAdapter(List<MenuModel> data, Context context, PesanMinum pesanan) {
        this.data = data;
        this.context = context;
        this.pesanan = pesanan;

        sweetLoading = new SweetLoading(context);
        tmp = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public MinumanAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MinumanAdapter.ViewHolder holder, int position) {
        MenuModel model = data.get(position);

        holder.nama.setText(model.getNama());
        holder.deskripsi.setText(model.getDeskripsi());
        holder.harga.setText(sweetLoading.formatRupiah(Double.parseDouble(String.valueOf(model.getHarga()))));
        holder.kategori.setImageResource(R.drawable.ic_drink);

        byte[] decodedString = Base64.decode(model.getFoto(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.foto.setImageBitmap(decodedByte);

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesanan.tambahMinum(model);
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
        TextView nama, harga, deskripsi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_layout = itemView.findViewById(R.id.item_layout);
            foto = itemView.findViewById(R.id.foto);
            kategori = itemView.findViewById(R.id.kategori);
            nama = itemView.findViewById(R.id.nama);
            harga = itemView.findViewById(R.id.harga);
            deskripsi = itemView.findViewById(R.id.deskripsi);
        }
    }

    public void cari (String value) {
        value = value.toLowerCase(Locale.getDefault());

        data.clear();
        if (value.isEmpty()) {
            data.addAll(tmp);
        } else {
            for (MenuModel m : tmp) {
                if (m.getNama().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface PesanMinum {
        void tambahMinum(MenuModel model);
    }
}
