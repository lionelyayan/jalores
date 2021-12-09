package com.cantika.jalores.customer;

public class TransaksiModel {

    int total;
    String id, pesanan, status, tanggal;

    public TransaksiModel() {
    }

    public TransaksiModel(String id, int total, String pesanan, String status, String tanggal) {
        this.id = id;
        this.total = total;
        this.pesanan = pesanan;
        this.status = status;
        this.tanggal = tanggal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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
}
