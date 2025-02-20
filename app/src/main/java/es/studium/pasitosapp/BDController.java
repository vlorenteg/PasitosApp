package es.studium.pasitosapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class BDController {
    private BaseDatos baseDatos;
    private String NOMBRE_TABLA = "SQLiteDatos";

    // Constructor
    public BDController(Context context) {
        baseDatos = new BaseDatos(context);
    }

    // Insertar datos
    public long insertarDatos(PasitosSQLite datos) {
        SQLiteDatabase db = null;
        try {
            db = baseDatos.getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("latitud", datos.getLatitud());
            valores.put("longitud", datos.getLongitud());
            valores.put("bateria", datos.getBateria());
            return db.insert(NOMBRE_TABLA, null, valores);
        } finally {
            if (db != null) db.close();
        }
    }

    // Obtener todos los datos guardados
    public List<PasitosSQLite> obtenerDatos() {
        List<PasitosSQLite> listaDatos = new ArrayList<>();
        SQLiteDatabase db = baseDatos.getReadableDatabase();

        // Consulta modificada para ordenar por el campo id en orden descendente (últimos datos primero)
        Cursor cursor = db.rawQuery("SELECT * FROM " + NOMBRE_TABLA + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                double latitud = cursor.getDouble(1);
                double longitud = cursor.getDouble(2);
                int bateria = cursor.getInt(3);
                long id = cursor.getLong(0);

                listaDatos.add(new PasitosSQLite(latitud, longitud, bateria, id));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaDatos;
    }

}
