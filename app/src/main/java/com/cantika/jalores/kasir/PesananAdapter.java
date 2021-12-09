package com.cantika.jalores.kasir;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cantika.jalores.LoginModel;
import com.cantika.jalores.R;
import com.cantika.jalores.customer.TransaksiModel;
import com.cantika.jalores.helper.SweetLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PesananAdapter extends RecyclerView.Adapter<PesananAdapter.ViewHolder> {

    private Context context;
    List<TransaksiModel> data, data1, tmp;
    SweetLoading sweetLoading;
    String tipe;
    LinearLayout empty;

    public PesananAdapter(Context context, List<TransaksiModel> data, String tipe, LinearLayout empty) {
        this.context = context;
        this.data = data;
        this.tipe = tipe;
        this.empty = empty;

        data1 = new ArrayList<>(data);
        if (!tipe.equals("kasir")) {
            data.clear();
            for (int i=0; i<data1.size(); i++) {
                if (tipe.equals("Laporan")) {
                    if (data1.get(i).getStatus().equals("selesai")) {
                        data.add(data1.get(i));
                    }
                } else {
                    if (!data1.get(i).getStatus().equals("selesai")) {
                        data.add(data1.get(i));
                    }
                }
            }
            notifyDataSetChanged();
        }

        if (data.size()<1) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }

        tmp = new ArrayList<>(data);
        sweetLoading = new SweetLoading(context);
    }

    @NonNull
    @Override
    public PesananAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pesanan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PesananAdapter.ViewHolder holder, int position) {
        TransaksiModel tmp = data.get(position);
        holder.id.setText("ID : "+tmp.getId());
        holder.pesanan.setText(tmp.getPesanan());
        holder.total.setText(sweetLoading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getTotal()))));

        holder.proses.setText(tmp.getStatus());
        if (tmp.getStatus().equals("selesai")) {
            holder.proses.setEnabled(false);
            holder.proses.setBackground(context.getResources().getDrawable(R.drawable.rounded_green));
        } else if (tmp.getStatus().equals("proses")) {
            holder.proses.setText("Selesaikan");
        } else {
            holder.proses.setText("proses");
        }

        holder.proses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tmp.getStatus().equals("menunggu")) {
                    FirebaseDatabase.getInstance().getReference().child("transaksi").child(tmp.getId()).child("status")
                            .setValue("proses").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Berhasil memproses", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    FirebaseDatabase.getInstance().getReference().child("transaksi").child(tmp.getId()).child("status")
                            .setValue("selesai").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                holder.proses.setEnabled(false);
                                Toast.makeText(context, "Berhasil memproses", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_struk);
                dialog.setCancelable(true);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                TextView id, pesanan, total, tgl;
                id = dialog.findViewById(R.id.id);
                pesanan = dialog.findViewById(R.id.pesanan);
                total = dialog.findViewById(R.id.total);
                tgl = dialog.findViewById(R.id.tgl);

                id.setText(tmp.getId());
                pesanan.setText(tmp.getPesanan());
                total.setText(sweetLoading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getTotal()))));
                tgl.setText(tmp.getTanggal());

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
        TextView id, pesanan, total, proses;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_layout = itemView.findViewById(R.id.item_layout);
            id = itemView.findViewById(R.id.id);
            pesanan = itemView.findViewById(R.id.pesanan);
            total = itemView.findViewById(R.id.total);
            proses = itemView.findViewById(R.id.proses);

            if (!tipe.equals("kasir"))
                proses.setVisibility(View.GONE);
        }
    }

    public void cari(String value) {
        value = value.toLowerCase(Locale.getDefault());
        data.clear();

        if (value.isEmpty()) {
            data.addAll(tmp);
        } else {
            for (TransaksiModel m : tmp) {
                if (m.getId().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }

        notifyDataSetChanged();
    }
}
