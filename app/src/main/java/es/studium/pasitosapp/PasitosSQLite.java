package es.studium.pasitosapp;

public class PasitosSQLite {
    private double latitud;
    private double longitud;
    private int bateria;
    private long id;

    // Constructor sin id (para insertar nuevos datos)
    public PasitosSQLite(double latitud, double longitud, int bateria) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.bateria = bateria;
    }

    // Constructor con id (cuando recuperamos datos de la BD)
    public PasitosSQLite(double latitud, double longitud, int bateria, long id) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.bateria = bateria;
        this.id = id;
    }

    // Getter y setter para latitud
    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    // Getter y setter para longitud
    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    // Getter y setter para bateria
    public int getBateria() {
        return bateria;
    }

    public void setBateria(int bateria) {
        this.bateria = bateria;
    }

    // Getter y setter para id
    public long getId() {
        return id;
    }

    public void setId(long id) {

        this.id = id;
    }
}
