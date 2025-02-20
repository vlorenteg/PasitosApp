package es.studium.pasitosapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mapa;
    double lat;
    double lng;
    int btr;
    String coordenadas = "";
    private BDController bdController;
    private TextView textoDatos;
    DecimalFormat dformat = new DecimalFormat("####.########");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializamos el TextView
        textoDatos = findViewById(R.id.textoDatos);

        // Creamos el controlador de la BD
        bdController = new BDController(MainActivity.this);

        // Cargar datos en el TextView
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cargarDatos(); // Actualiza los datos cada 5 minutos
                handler.postDelayed(this, 300000); // 300000 ms = 5 minutos
            }
        };
        handler.postDelayed(runnable, 300000);

        // Obtenemos el mapa de forma asíncrona (notificará cuando esté listo)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapa);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Obtenemos los permisos necesarios
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            return;
        } else {
            locationStart();
        }

        // Batería
        registerReceiver(bateria, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    // Obtenemos el nivel de la batería
    private BroadcastReceiver bateria = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btr = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    // Obtenemos la última posición conocida cada 5 minutos
    private void locationStart() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 0, (LocationListener) Local);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 0, (LocationListener) Local);
    }

    // Comprueba los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
            }
        }
    }

    // Método para convertir VectorDrawable a Bitmap
    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = getResources().getDrawable(drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable) drawable;
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("Unsupported drawable type");
        }
    }

    // Método para enviar la localización
    public void setLocation(Location loc) {
        // Obtenemos la posición actual
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    // Marcador
                    LatLng posicion = new LatLng(loc.getLatitude(), loc.getLongitude());

                    // Convertir el recurso a Bitmap usando el método nuevo
                    Bitmap bitmap = getBitmapFromVectorDrawable(R.drawable.baseline_location_on_24);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

                    mapa.addMarker(new MarkerOptions()
                            .position(posicion)
                            .title("Lat: " + dformat.format(loc.getLatitude()) + " - Long: " + dformat.format(loc.getLongitude()))
                            .snippet("Batería: " + btr + "%")
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)) // Se usa el Bitmap en lugar del recurso directo
                            .anchor(0.19f, 0.72f)
                    );
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 18));

                    // Insertamos los datos en la BD
                    PasitosSQLite nuevoRegistro = new PasitosSQLite(loc.getLatitude(), loc.getLongitude(), btr);
                    bdController.insertarDatos(nuevoRegistro);

                    // Actualizamos el TextView con los nuevos datos
                    cargarDatos();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Método utilizado si cambia el estado del GPS
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            lat = loc.getLatitude();
            lng = loc.getLongitude();
            coordenadas = loc.getLatitude() + "," + loc.getLongitude();
            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("debug", "GPS Activado");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("debug", "GPS Desactivado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case 0:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case 1:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
                case 2:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
            }
        }
    }

    private void cargarDatos() {
        List<PasitosSQLite> listaDatos = bdController.obtenerDatos();
        StringBuilder datosTexto = new StringBuilder();

        for (PasitosSQLite datos : listaDatos) {
            datosTexto.append("Lat: ").append(datos.getLatitud())
                    .append(", Long: ").append(datos.getLongitud())
                    .append(", Batería: ").append(datos.getBateria()).append("%\n");
        }

        if (datosTexto.length() == 0) {
            textoDatos.setText("No hay datos guardados");
        } else {
            textoDatos.setText(datosTexto.toString());
        }
    }


    // Mostramos el mapa
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
    }
}


