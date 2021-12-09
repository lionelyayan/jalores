package com.cantika.jalores.kasir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.R;
import com.cantika.jalores.customer.model.PesananModel;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.kasir.PesananAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    Context context;
    List<PesananModel> data, tmp;
    KasirListener kasirListener;

    public TransaksiAdapter(Context context, List<PesananModel> data, KasirListener kasirListener) {
        this.context = context;
        this.data = data;
        this.kasirListener = kasirListener;

        tmp = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public TransaksiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaksi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaksiAdapter.ViewHolder holder, int position) {
        PesananModel tmp = data.get(position);

        holder.id.setText("ID : "+tmp.getId());
        holder.tanggal.setText(tmp.getTanggal());

        if (tmp.getStatus().equals("menunggu")) {
            holder.tanda.setBackgroundResource(R.color.yellow);
            holder.status.setText("Menunggu diproses");
        } else if (tmp.getStatus().equals("hidangkan")) {
            holder.tanda.setBackgroundResource(R.color.colorPrimary);
            holder.status.setText("Dihidangkan");
        } else {
            holder.tanda.setBackgroundResource(R.color.green);
            holder.status.setText("Selesai");
        }

        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kasirListener.showDetail(tmp);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView item_layout;
        TextView id, tanggal, status, tanda;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item_layout = itemView.findViewById(R.id.item_layout);
            id = itemView.findViewById(R.id.id);
            tanggal = itemView.findViewById(R.id.tanggal);
            status = itemView.findViewById(R.id.status);
            tanda = itemView.findViewById(R.id.tanda);
        }
    }

    public void cari (String value) {
        value = value.toLowerCase(Locale.getDefault());

        data.clear();
        if (value.equals("")) {
            data.addAll(tmp);
        } else {
            for (PesananModel m : tmp) {
                if (m.getId().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface KasirListener {
        void showDetail(PesananModel tmp);
    }
}
