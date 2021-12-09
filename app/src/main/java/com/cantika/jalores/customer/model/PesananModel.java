package com.cantika.jalores.customer.model;

public class PesananModel {

    String id, pesanan, status, tanggal;
    int total;

    public PesananModel() {
    }

    public PesananModel(String id, String pesanan, String status, String tanggal, int total) {
        this.id = id;
        this.pesanan = pesanan;
        this.status = status;
        this.tanggal = tanggal;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPesanan() {
        return pesanan;
    }

    public void setPesanan(String pesanan) {
        this.pesanan = pesanan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
