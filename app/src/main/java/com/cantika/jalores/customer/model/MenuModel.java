package com.cantika.jalores.customer.model;

public class MenuModel {

    String nama, deskripsi, kategori, id, foto;
    int harga;

    public MenuModel() {
    }

    public MenuModel(String nama, String deskripsi, String kategori, String id, int harga, String foto) {
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.id = id;
        this.harga = harga;
        this.foto = foto;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
