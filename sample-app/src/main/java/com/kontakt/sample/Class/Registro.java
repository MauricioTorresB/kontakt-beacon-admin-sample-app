package com.kontakt.sample.Class;

public class Registro {
    private String sim;
    private String deviceId;
    private int major;
    private int minor;
    private String latitud;
    private String longitud;
    private String dato1;
    private String dato2;
    private String dato3;

    public Registro() {
    }

    public Registro(String sim, String deviceId, int major, int minor, String latitud, String longitud, String dato1, String dato2, String dato3) {
        this.sim = sim;
        this.deviceId = deviceId;
        this.major = major;
        this.minor = minor;
        this.latitud = latitud;
        this.longitud = longitud;
        this.dato1 = dato1;
        this.dato2 = dato2;
        this.dato3 = dato3;
    }

    public String getSim() {
        return sim;
    }

    public void setSim(String sim) {
        this.sim = sim;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getDato1() {
        return dato1;
    }

    public void setDato1(String dato1) {
        this.dato1 = dato1;
    }

    public String getDato2() {
        return dato2;
    }

    public void setDato2(String dato2) {
        this.dato2 = dato2;
    }

    public String getDato3() {
        return dato3;
    }

    public void setDato3(String dato3) {
        this.dato3 = dato3;
    }
}
