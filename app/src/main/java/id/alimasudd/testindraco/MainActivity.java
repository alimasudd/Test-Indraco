package id.alimasudd.testindraco;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.ByteArrayOutputStream;

import id.alimasudd.testindraco.databinding.ActivityMainBinding;
import id.alimasudd.testindraco.utils.SessionManager;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private SessionManager sessionManager;

    public static int PERMISSION_ALL = 4765;
    String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9101;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    private Location location;
    private LocationRequest locationRequest;
    protected static final int REQUEST_CHECK_SETTINGS = 10101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

        binding.clickpicture.setOnClickListener(v -> {
            takeCapture();
        });

        binding.buttonData.setOnClickListener(v -> {
            if(sessionManager.getImage() != null){
                if (!hasPermissions(MainActivity.this, PERMISSIONS_LOCATION)) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_LOCATION, PERMISSION_ALL);
                }else{
                    Intent intent=new Intent(this,ResultCamera.class);
                    startActivity(intent);
                }
            }else{

                Toast.makeText(MainActivity.this, "Data masih kosong", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonAPI.setOnClickListener(v -> {
            Intent intent=new Intent(this,openAPI.class);
            startActivity(intent);
        });



    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void takeCapture() {
        if (!hasPermissions(MainActivity.this, PERMISSIONS_CAMERA)) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_CAMERA, 123);
        }
        else{
            Intent camera = new Intent();
            camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera,118);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takeCapture();
            } else {
                Toast.makeText(MainActivity.this, "Berikan izin kamera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==118&& resultCode==RESULT_OK){
            Bitmap photo= (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            sessionManager.setImage(encodedImage);
            Toast.makeText(this, "Data Tersimpan", Toast.LENGTH_SHORT).show();
//            Intent intent=new Intent(this,ResultCamera.class);
//            startActivity(intent);
        }else if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Log.i("GPSAuto", "User agreed to make required location settings changes.");
//                    startLocationUpdates();
                    break;
                case Activity.RESULT_CANCELED:
                    Log.i("GPSAuto", "User chose not to make required location settings changes.");
                    break;
            }
        }
    }



    void checkSP() {

        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationSettingsRequest(MainActivity.this);
        } else {

            if (googleApiClient != null && locationRequest != null) {
                if (!googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("GPSAuto", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("GPSAuto", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setTitle("Informasi");
                        builder.setMessage("Izin lokasi diperlukan untuk mengkases fitur aplikasi.");

                        builder.setPositiveButton("Saya Mengerti", (dialog, which) -> {

                            dialog.dismiss();
                            try {
                                status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                Log.i("GPSAuto", "PendingIntent unable to execute request.");
                            }

                        });

                        AlertDialog alert = builder.create();
                        alert.setCancelable(false);
                        alert.show();
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("GPSAuto", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!hasPermissions(MainActivity.this, PERMISSIONS_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_LOCATION, PERMISSION_ALL);
        }else{
            checkSP();
            checkPlayServices();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(MainActivity.this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(MainActivity.this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            }

            return false;
        }

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


        if (location != null) {

            sessionManager.setLatitude(String.valueOf(location.getLatitude()));
            sessionManager.setLongitude(String.valueOf(location.getLongitude()));

        }

        startLocationUpdates();


    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "GPS belum diijinkan !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location loc) {

        if (loc != null) {
            location = loc;

            sessionManager.setLatitude(String.valueOf(location.getLatitude()));
            sessionManager.setLongitude(String.valueOf(location.getLongitude()));

            Log.d("locationlat", " location" + String.valueOf(location.getLatitude()));
            Log.d("locationlong", " location" + String.valueOf(location.getLongitude()));

            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}