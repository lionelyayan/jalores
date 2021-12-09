package com.cantika.jalores.admin.model;

public class KasirModel {

    String id, email, nama, password, title;

    public KasirModel() {
    }

    public KasirModel(String id, String email, String nama, String password, String title) {
        this.id = id;
        this.email = email;
        this.nama = nama;
        this.password = password;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
