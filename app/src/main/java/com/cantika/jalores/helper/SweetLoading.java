package com.cantika.jalores.helper;

import android.content.Context;

import java.text.NumberFormat;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SweetLoading {

    private Context context;
    private SweetAlertDialog sweetAlertDialog;

    public SweetLoading(Context context) {
        this.context = context;

        sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
    }

    public void show() {
        sweetAlertDialog.show();
    }

    public void dismiss() {
        sweetAlertDialog.dismiss();
    }

    public String formatRupiah(Double number){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(number);
    }
}
