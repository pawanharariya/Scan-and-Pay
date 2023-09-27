package com.psh.scanpay.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.psh.scanpay.MainActivity;
import com.psh.scanpay.R;
import com.psh.scanpay.models.SliderImages;
import com.psh.scanpay.models.Store;
import com.psh.scanpay.utils.Constants;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, MainActivity.MapUpdateListener,
        GoogleMap.OnMarkerClickListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted = false;
    private LatLng currentLocation, oldLocation;
    private List<Store> storeList;
    private SupportMapFragment mapFragment;
    private String zipcode;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Marker currentMarker;
    private ImageSlider imageSlider;

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        ((MainActivity) requireActivity()).initializeMapListener(this);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        imageSlider = root.findViewById(R.id.image_slider);
        setImageSlider();
        sharedPreferences = requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE);
        storeList = ((MainActivity) requireActivity()).getAllStores();
        zipcode = sharedPreferences.getString(Constants.USER_ZIPCODE, "263139");
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
        }
        double lastLatitude = Double.parseDouble(sharedPreferences.getString(Constants.LAST_LATITUDE, "28.7041"));
        double lastLongitude = Double.parseDouble(sharedPreferences.getString(Constants.LAST_LONGITUDE, "77.1025"));
        oldLocation = new LatLng(lastLatitude, lastLongitude);
        Log.e("oldLocation", "location" + lastLatitude + "  " + lastLongitude);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        currentLocation = new LatLng(lat, lng);
                        saveLastLocation(lat, lng);
                        mapFragment.getMapAsync(this);
                    } else requestLocation();
                });
        mapFragment.getMapAsync(this);
        return root;
    }

    private void setImageSlider() {
        List<SlideModel> imageList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings
                .Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection(Constants.BANNER_IMAGES_BASE_URL)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        SliderImages sliderImages = (documentSnapshot.toObject(SliderImages.class));
                        if (sliderImages != null) {
                            List<SlideModel> images = sliderImages.getImages();
                            if (images != null)
                                imageList.addAll(sliderImages.getImages());
                        }
                    }
                    imageSlider.setImageList(imageList);
                });
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        map = googleMap;
        if (currentLocation == null)
            currentLocation = oldLocation;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
        if (currentMarker == null)
            currentMarker = map.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Current Location"));
        else
            currentMarker.setPosition(currentLocation);
        if (storeList == null)
            storeList = new ArrayList<>();
        for (Store store : storeList) {
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(store.getLatLng())
                    .title(store.getStoreName()));
            assert marker != null;
            marker.setTag(store);
        }
    }

    private void requestLocation() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        currentLocation = new LatLng(lat, lng);
                        saveLastLocation(lat, lng);
                        mapFragment.getMapAsync(HomeFragment.this);
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            getLocationPermission();
        LocationServices.getFusedLocationProviderClient(requireActivity())
                .requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void saveLastLocation(double lat, double lng) {
        editor = sharedPreferences.edit();
        editor.putString(Constants.LAST_LATITUDE, String.valueOf(lat));
        editor.putString(Constants.LAST_LONGITUDE, String.valueOf(lng));
        editor.apply();
        Log.e("saveLastLocation", "lastLocations" + lat + "  " + lng);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            } else
                Toast.makeText(requireContext(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateHomeFragmentUI() {
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onMarkerClick(@NotNull final Marker marker) {

        return false;
    }
}