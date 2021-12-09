package com.cantika.jalores;

public class LoginModel {
    String nama, title, email, password,alamat;

    public LoginModel() {
    }

    public LoginModel(String nama, String title, String email, String password, String alamat) {
        this.nama = nama;
        this.title = title;
        this.email = email;
        this.password = password;
        this.alamat=alamat;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
