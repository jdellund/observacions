package com.edumet.observacions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static java.lang.String.valueOf;

public class Captura extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    //The fastest rate for active location updates. Exact. Updates will never be more frequent than this value.
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private static final int CONTENT_REQUEST = 1337; // fotos
    private static final String EXTRA_FILENAME = "com.edumet.observacions.EXTRA_FILENAME";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    // UI Widgets.
    private ImageButton Foto;
    private ImageButton Girar;
    private ImageButton Envia;
    private ImageButton Desa;
    private ImageButton Pendents;
    private ImageButton Mapa;
    public ImageView imatge;
    private EditText observacio;
    private ProgressBar mProgressBar;
    private Spinner spinner;

    private String timeStamp;
    private String pathEnvia;
    private String mCurrentPhotoPath;
    static private boolean mRequestingLocationUpdates;
    private File output = null;
    private File outputMiniatura = null;
    private int midaEnvia = 800;
    public Bitmap bitmap;
    private int num_fenomen = 1;
    public int angle_foto;
    private boolean jaLocalitzat = false;
    private String dia;
    private String hora;
    private int AppID;

    DadesHelper mDbHelper;

    String[] nomFenomen;

    String usuari;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.captura, container, false);
        setHasOptionsMenu(true);

        Foto = (ImageButton) v.findViewById(R.id.btnFoto);
        Girar = (ImageButton) v.findViewById(R.id.btnGirar);
        Envia = (ImageButton) v.findViewById(R.id.btnEnvia);
        Desa = (ImageButton) v.findViewById(R.id.btnDesa);
        Pendents = (ImageButton) v.findViewById(R.id.btnPendents);
        Mapa = (ImageButton) v.findViewById(R.id.btnMapa);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        imatge = (ImageView) v.findViewById(R.id.imgFoto);
        observacio = (EditText) v.findViewById(R.id.txtObservacions);
        spinner = (Spinner) v.findViewById(R.id.spinner);

        mDbHelper = new DadesHelper(getContext());

        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                "com.edumet.observacions", Context.MODE_PRIVATE);
        usuari = sharedPref.getString("usuari", "");

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();

        //ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);

        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Foto.setEnabled(false);
        Foto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fesFoto();
            }
        });
        Girar.setEnabled(false);
        Girar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                angle_foto += 90;
                if (angle_foto >= 360) {
                    angle_foto = 0;
                }
                Log.i("ANGLE", valueOf(angle_foto));
                bitmap = rotateViaMatrix(bitmap, 90);
                imatge.setImageBitmap(bitmap);
            }
        });
        Envia.setEnabled(false);
        Envia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desa();
                sendPost();
            }
        });
        Desa.setEnabled(false);
        Desa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                desa();
                Snackbar snackbar = Snackbar
                        .make(getActivity().findViewById(android.R.id.content), "Observació desada. Vols enviar-la ?", Snackbar.LENGTH_LONG)
                        .setAction("SÍ", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendPost();
                            }
                        });
                snackbar.show();
            }
        });
        Pendents.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).observacionsFetes();
            }
        });
        Mapa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapa();
            }
        });

        Resources res = getResources();
        nomFenomen = res.getStringArray(R.array.nomFenomen);

        List<String> categories = new ArrayList<String>();
        for (int i = 1; i < nomFenomen.length; i++) {
            categories.add(nomFenomen[i]);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                num_fenomen = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        updateValuesFromBundle(savedInstanceState);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.captura_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putSerializable(EXTRA_FILENAME, output);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }
            updateLocationUI();
        }
    }

    private boolean sortir = true;

    @Override
    public void onResume() {
        super.onResume();
        Log.i("ONRESUME", String.valueOf(mRequestingLocationUpdates));
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }
        updateLocationUI();
        if (output != null) {
            imatge.setImageBitmap(bitmap);
            enableBotons();
        }

        if (getView() == null) {
            return;
        }

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // handle back button's click listener
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(android.R.id.content), "Vols sortir de l'App ?", Snackbar.LENGTH_LONG)
                            .setAction("SÍ", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getActivity().finish();
                                }
                            });
                    snackbar.show();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        Log.i("ONPAUSE", String.valueOf(mRequestingLocationUpdates));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        imatge.setImageBitmap(bitmap);
    }

    //
    // LOCALITZACIÓ
    //

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.i("FRAGonActResult", "User agreed to make required location settings changes.");
                        mRequestingLocationUpdates = true;
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("FRAGonActResult", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                updateLocationUI();
                Log.i("FRAGonActResult", String.valueOf(mRequestingLocationUpdates));
                break;
            case CONTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    angle_foto = 0;
                    setPic(midaEnvia, midaEnvia);
                    imatge.setImageBitmap(bitmap);
                    galleryAddPic();
                    enableBotons();
                    ((MainActivity) getActivity()).hihaFoto();
                    Log.i("onActivityResult", "Foto");
                }
                break;
        }
    }

    private void enableBotons() {
        Girar.setImageResource(R.mipmap.ic_rotate_edumet);
        Girar.setEnabled(true);
        Envia.setImageResource(R.mipmap.ic_send_edumet);
        Envia.setEnabled(true);
        Desa.setImageResource(R.mipmap.ic_save_edumet);
        Desa.setEnabled(true);
    }

    private void startLocationUpdates() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mRequestingLocationUpdates = true;
                        updateLocationUI();

                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("startUpdates", "Location settings are not satisfied. Attempting to upgrade location settings.");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("startUpdates", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                                Log.i("startUpdates", errorMessage);
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }
                        updateLocationUI();
                    }
                });
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            if (!jaLocalitzat) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "S'ha localitzat la teva ubicació", Snackbar.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).ubicacio(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                jaLocalitzat = true;
            }
            Foto.setImageResource(R.mipmap.ic_camera_edumet);
            Foto.setEnabled(true);
            Mapa.setImageResource(R.mipmap.ic_location_edumet);
            Mapa.setEnabled(true);
        }
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.i("stopUpdates", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (shouldProvideRationale) {
            Log.i("requestPermissions", "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i("requestPermissions", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("FRAGonReqPermResult", "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates) {
                    Log.i("FRAGonReqPermResult", "Permission granted, updates requested, starting location updates");
                }
            } else {
                // Permission denied.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    //
    // MAPA
    //

    public void mapa() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        }
        if (mCurrentLocation != null) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            intent.putExtra(MainActivity.EXTRA_LATITUD, String.valueOf(mCurrentLocation.getLatitude()));
            intent.putExtra(MainActivity.EXTRA_LONGITUD, String.valueOf(mCurrentLocation.getLongitude()));
            startActivity(intent);
        }
    }
    //
    // FOTOGRAFIA
    //

    public void fesFoto() {
        if (mCurrentLocation != null) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getActivity().getPackageManager()) != null) {
                try {
                    output = createImageFile();
                } catch (IOException ex) {
                    Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                if (output != null) {
                    Uri outputUri = FileProvider.getUriForFile(getContext(), "com.edumet.observacions", output);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = ClipData.newUri(getActivity().getContentResolver(), "Una foto", outputUri);
                        i.setClipData(clip);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            getActivity().grantUriPermission(packageName, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                    try {
                        startActivityForResult(i, CONTENT_REQUEST);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(super.getContext(), R.string.msg_no_camera, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    }
                }
            }

        } else {
            Toast.makeText(super.getContext(), R.string.encara_sense_lloc, Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(timeStamp, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

  public void fesMiniatura(int x) {
        try {
            String minTimeStamp = String.valueOf(x) + "_" + timeStamp;
            File storageDir = getActivity().getFilesDir();
            outputMiniatura = File.createTempFile(minTimeStamp, ".jpg", storageDir);
        } catch (IOException ex) {
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        try {
            FileOutputStream out = new FileOutputStream(outputMiniatura);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(super.getContext(), R.string.fitxer_error, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void setPic(int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

    static public Bitmap rotateViaMatrix(Bitmap original, int angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return (Bitmap.createBitmap(original, 0, 0, original.getWidth(),
                original.getHeight(), matrix, true));
    }

    //
    // ENVIA AL SERVIDOR EDUMET
    //

    public void sendPost() {

        ByteArrayOutputStream baosEnv = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosEnv);
        byte[] fotografia = baosEnv.toByteArray();
        String encodedFoto = Base64.encodeToString(fotografia, Base64.DEFAULT);

        ((MainActivity) getActivity()).enviaObservacio(
                AppID,
                encodedFoto,
                usuari,
                dia,
                hora,
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                num_fenomen,
                observacio.getText().toString()
        );
    }

    //
    // DESA
    //

    public void desa() {

        fesMiniatura(midaEnvia);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat shf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        dia = sdf.format(Calendar.getInstance().getTime());
        hora = shf.format(Calendar.getInstance().getTime());

        AppID=((MainActivity) getActivity()).desaObservacio(
                dia,
                hora,
                mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude(),
                num_fenomen,
                observacio.getText().toString(),
                mCurrentPhotoPath,
                outputMiniatura.getAbsolutePath()
        );
    }

    //
    // GENERAL
    //

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                getActivity().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE).setAction(getString(actionStringId), listener).show();
    }
}









