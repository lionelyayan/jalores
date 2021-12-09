package com.cantika.jalores.customer;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cantika.jalores.R;
import com.cantika.jalores.customer.model.MenuModel;
import com.cantika.jalores.helper.SharedPref;
import com.cantika.jalores.helper.SweetLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private Context context;
    private List<MenuModel> data, arr;
    SweetLoading sweetLoading;
    int iJml;
    TextView pesan;
    String list;

    SharedPref sharedPref;
    SweetLoading loading;

    Edit edit;

    public MenuAdapter(Context context, List<MenuModel> data, TextView pesan, String list, Edit edit) {
        this.context = context;
        this.data = data;
        this.pesan = pesan;
        this.list = list;
        this.edit = edit;

        loading = new SweetLoading(context);
        arr = new ArrayList<>(data);
        sweetLoading = new SweetLoading(context);
        sharedPref = new SharedPref(context);
    }

    @NonNull
    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.ViewHolder holder, int position) {
        MenuModel tmp = data.get(position);

//        holder.foto.setImageBitmap();
        holder.nama.setText(tmp.getNama());
        holder.harga.setText(sweetLoading.formatRupiah(Double.parseDouble(String.valueOf(tmp.getHarga()))));

        if (!list.equals("")) {
            holder.jml.setVisibility(View.GONE);
            holder.item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialogmenu = new Dialog(context);
                    dialogmenu.setContentView(R.layout.dialog_tambah_menu);
                    dialogmenu.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    EditText menunama = dialogmenu.findViewById(R.id.nama);
                    EditText menuharga = dialogmenu.findViewById(R.id.harga);
                    TextView menutambah = dialogmenu.findViewById(R.id.tambah);
                    menunama.setEnabled(false);

                    menunama.setText(data.get(position).getNama());
                    menuharga.setText(String.valueOf(data.get(position).getHarga()));
                    menutambah.setText("Update harga");

                    menutambah.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (menuharga.getText().toString().isEmpty()) {
                                Toast.makeText(context, "Harga harus diisi", Toast.LENGTH_SHORT).show();
                            } else {
                                dialogmenu.dismiss();
                                edit.editMenu(tmp, loading, menuharga.getText().toString());
                            }
                        }
                    });

                    dialogmenu.show();
                }
            });

            holder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    edit.hapusMenu(tmp, loading);
                    return false;
                }
            });
        }

        else {
            holder.item_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView foto;
        TextView nama, harga, jml;
        CardView item_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

//            jml = itemView.findViewById(R.id.jml);
            foto = itemView.findViewById(R.id.foto);
            nama = itemView.findViewById(R.id.nama);
            harga = itemView.findViewById(R.id.harga);
            item_layout = itemView.findViewById(R.id.item_layout);
        }
    }

    public void search(String value) {
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

    interface Edit{
        void editMenu(MenuModel menuModel, SweetLoading loading, String harga);
        void hapusMenu(MenuModel menuModel, SweetLoading loading);
    }
}
