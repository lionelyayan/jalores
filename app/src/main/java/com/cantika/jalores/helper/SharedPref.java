package com.cantika.jalores.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    public final String PREF = "pref";
    public final String STATUS = "status";
    public final String ID = "id";
    public final String USER = "user";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public SharedPref(Context context) {
        sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void prefClear() {
        spEditor.clear().commit();
    }

    public void setString(String key, String value) {
        spEditor.putString(key, value);
        spEditor.commit();
    }

    public String getSTATUS() {
        return sp.getString(STATUS, null);
    }

    public String getID() {
        return sp.getString(ID, null);
    }

    public String getUSER() {
        return sp.getString(USER, null);
    }
}
