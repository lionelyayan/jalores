package com.cantika.jalores.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.R;
import com.cantika.jalores.helper.SharedPref;
import com.cantika.jalores.helper.SweetLoading;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<String> data;
    Delete delete;
    SharedPref sharedPref;
    String tipe;
    SweetLoading loading;

    public CartAdapter(Context context, List<String> data, Delete delete, SharedPref sharedPref, String tipe) {
        this.data = data;
        this.delete = delete;
        this.sharedPref = sharedPref;
        this.tipe = tipe;
        this.context = context;

        loading = new SweetLoading(context);
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        String[] sArr = data.get(position).split("\\|");
        holder.nama.setText(sArr[0]);
        holder.jumlah.setText(sArr[1]);
        holder.harga.setText(loading.formatRupiah(Double.parseDouble(sArr[2])));

        holder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                dialog.setCancelable(false);
                dialog.setTitleText("Yaah.. yakin nih mau hapus menu ini..?");
                dialog.setConfirmButton("Hapus", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        delete.deleteCart(data.get(position));
                        dialog.dismiss();
                    }
                });
                dialog.setCancelButton("Batal", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView jumlah, nama, harga;
        LinearLayout item_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            jumlah = itemView.findViewById(R.id.jumlah);
            nama = itemView.findViewById(R.id.nama);
            harga = itemView.findViewById(R.id.harga);
            item_layout = itemView.findViewById(R.id.item_layout);

            if (sharedPref.getSTATUS()!=null) {
                item_layout.setEnabled(false);
            } else {
                if (tipe.equals("customer")) {
                    item_layout.setEnabled(true);
                } else {
                    item_layout.setEnabled(false);
                }
            }
        }
    }

    public interface Delete {
        void deleteCart(String s);
    }
}
