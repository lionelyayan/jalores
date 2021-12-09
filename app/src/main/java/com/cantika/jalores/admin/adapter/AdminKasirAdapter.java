package com.cantika.jalores.admin.adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.R;
import com.cantika.jalores.admin.model.KasirModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdminKasirAdapter extends RecyclerView.Adapter<AdminKasirAdapter.ViewHolder> {

    Context context;
    List<KasirModel> data, arr;
    ListenerKasir listenerKasir;

    public AdminKasirAdapter(Context context, List<KasirModel> data, ListenerKasir listenerKasir) {
        this.context = context;
        this.data = data;
        this.listenerKasir = listenerKasir;

        arr = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public AdminKasirAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_kasir, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminKasirAdapter.ViewHolder holder, int position) {
        KasirModel tmp = data.get(position);

        holder.id.setText(tmp.getId());
        holder.nama.setText(tmp.getNama());
        holder.email.setText(tmp.getEmail());

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_detail_kasir);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView id, nama, email, password, edit, hapus;
                id = dialog.findViewById(R.id.id);
                nama = dialog.findViewById(R.id.nama);
                email = dialog.findViewById(R.id.email);
                password = dialog.findViewById(R.id.password);
                edit = dialog.findViewById(R.id.edit);
                hapus = dialog.findViewById(R.id.hapus);

                id.setText(tmp.getId());
                nama.setText(tmp.getNama());
                email.setText(tmp.getEmail());

                password.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (password.getText().toString().equals("**********")) {
                            password.setText(tmp.getPassword());
                        } else {
                            password.setText("**********");
                        }
                    }
                });

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listenerKasir.editKasir(tmp);
                        dialog.dismiss();
                    }
                });

                hapus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialog.setTitle("Kamu yakin mau menghapus kasir "+tmp.getNama()+"..?");
                        sweetAlertDialog.setConfirmButton("Hapus", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                listenerKasir.hapusKasir(tmp, dialog);
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
        TextView id, nama, email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_layout = itemView.findViewById(R.id.item_layout);
            id = itemView.findViewById(R.id.id);
            nama = itemView.findViewById(R.id.nama);
            email = itemView.findViewById(R.id.email);
        }
    }

    public void cari(String value) {
        value = value.toLowerCase(Locale.getDefault());
        data.clear();

        if (value.isEmpty()) {
            data.addAll(arr);
        } else {
            for (KasirModel m : arr) {
                if (m.getNama().toLowerCase(Locale.getDefault()).contains(value) ||
                        m.getEmail().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface ListenerKasir {
        void editKasir(KasirModel tmp);
        void hapusKasir(KasirModel tmp, Dialog dialog);
    }
}
