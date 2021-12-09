package com.cantika.jalores.admin;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.LoginModel;
import com.cantika.jalores.R;
import com.cantika.jalores.helper.SweetLoading;
import com.cantika.jalores.kasir.KasirActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KasirAdapter extends RecyclerView.Adapter<KasirAdapter.ViewHolder> {

    List<LoginModel> data, tmp;
    Context context;
    SweetLoading loading;
    Edit listener;

    public KasirAdapter(List<LoginModel> data, Context context, Edit listener) {
        this.data = data;
        this.context = context;
        this.listener = listener;

        loading = new SweetLoading(context);

        for (int i=0; i<data.size(); i++) {
            if (data.get(i).getTitle().equals("Admin")) {
                data.remove(data.get(i));
            }
        }

        tmp = new ArrayList<>(data);
    }

    @NonNull
    @Override
    public KasirAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_kasir, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KasirAdapter.ViewHolder holder, int position) {
        holder.nama.setText(data.get(position).getNama());
        holder.email.setText(data.get(position).getEmail());
        holder.password.setText(data.get(position).getPassword());

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_tambah_kasir);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                EditText email = dialog.findViewById(R.id.email);
                EditText password = dialog.findViewById(R.id.password);
                EditText nama = dialog.findViewById(R.id.nama);
                TextView tambah = dialog.findViewById(R.id.tambah);
                nama.setEnabled(false);
                email.setEnabled(false);

                nama.setText(data.get(position).getNama());
                email.setText(data.get(position).getEmail());
                password.setText(data.get(position).getPassword());
                tambah.setText("Update Kasir");

                tambah.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                            Toast.makeText(context, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.dismiss();
                            listener.editKasir(data.get(position), loading, password.getText().toString());
                        }
                    }
                });

                dialog.show();
            }
        });

        holder.hapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.hapusKasir(data.get(position), loading);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nama, email, password, edit, hapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nama = itemView.findViewById(R.id.nama);
            email = itemView.findViewById(R.id.email);
            password = itemView.findViewById(R.id.password);
            edit = itemView.findViewById(R.id.edit);
            hapus = itemView.findViewById(R.id.hapus);
        }
    }

    public void cari(String value) {
        value = value.toLowerCase(Locale.getDefault());

        data.clear();
        if (value.isEmpty()) {
            data.addAll(tmp);
        } else {
            for (LoginModel m : tmp) {
                if (m.getNama().toLowerCase(Locale.getDefault()).contains(value) ||
                        m.getEmail().toLowerCase(Locale.getDefault()).contains(value)) {
                    data.add(m);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface Edit {
        void editKasir(LoginModel data, SweetLoading loading, String password);
        void hapusKasir(LoginModel data, SweetLoading loading);
    }
}
