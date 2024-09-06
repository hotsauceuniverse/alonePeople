package com.seyoung.alonepeople;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraUpdate;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;



public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private KakaoMap kakaoMap;
    //onRequestPermissionsResult에서 권한 요청 결과를 받기 위한 request code
    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 100;
    //요청할 위치 권한 목록
    private final String[] locationPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    // gradle 과 manifest 추가
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng startPosition = null;
    private ProgressBar progressBar;
    private Label centerLabel;
    private boolean requestingLocationUpdates = false;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ImageView gpsDot;

    private Button locationBtn;
    private String location = "";

    // https://stickode.tistory.com/1120
    private KakaoMapReadyCallback readyCallback = new KakaoMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull KakaoMap map) {
            kakaoMap = map; // kakaoMap 초기화
            progressBar.setVisibility(View.GONE);

            if (startPosition != null) {
                LabelLayer layer = kakaoMap.getLabelManager().getLayer();
                centerLabel = layer.addLabel(LabelOptions.from("centerLabel", startPosition)
                        .setStyles(LabelStyle.from(R.drawable.red_dot_marker_32px).setAnchorPoint(0.5f, 0.5f))
                        .setRank(1));
            }
        }

        @NonNull
        @Override
        public LatLng getPosition() {
            return startPosition != null ? startPosition : LatLng.from(0, 0);  // 기본 위치 설정
        }

        @NonNull
        @Override
        public int getZoomLevel() {
            return 17;  // 기본 줌 레벨 설정
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        mapView = findViewById(R.id.map_view);
        progressBar = findViewById(R.id.progressBar);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    centerLabel.moveTo(LatLng.from(location.getLatitude(), location.getLongitude()));
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {
            getStartLocation();
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }

        gpsDot = findViewById(R.id.gps_dot);
        gpsDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToCurrentLocation();
            }
        });

        locationBtn = findViewById(R.id.location_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationFromApi();
            }
        });
    }

    private void locationFromApi() {
       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSIONS_REQUEST_CODE);
            return;
       }
       fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
           @Override
           public void onSuccess(Location location) {
               if (location != null) {
                   double latitude = location.getLatitude();
                   double longitude = location.getLongitude();

                   String queryUrl = "https://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncListInfoInqire?serviceKey=e8KTlQRE%2FBEp0%2FkRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw%2FQ2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ%3D%3D&Q0=" + latitude + "," + longitude + "&pageNo=1&numOfRows=10";
                   callApi(queryUrl);
               } else {
                   Log.d("error", "error");
               }
           }
       });
    }

    private void callApi(String queryUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ok", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", "error");
            }
        });
        queue.add(stringRequest);
    }


    public void addCustomMarker() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void getStartLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                startPosition = LatLng.from(location.getLatitude(), location.getLongitude());
                mapView.start(readyCallback);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        requestingLocationUpdates = true;
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    // 지도 이동 후 사용자 현재 위치로 카메라 이동
    private void moveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, locationPermissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, locationPermissions[1]) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(this, location -> {
                        if (location != null && kakaoMap != null) {
                            LatLng currentLocation = LatLng.from(location.getLatitude(), location.getLongitude());
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCenterPosition(currentLocation, 17);
                            kakaoMap.moveCamera(cameraUpdate);
                        } else {
                            Log.d("asdasd", "asdasd");
                        }
                    })
                    .addOnFailureListener(this, e -> {
                        Log.e("asdasd123", "asdasd123", e);
                    });
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getStartLocation();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 권한 거부시 앱을 사용할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", (dialogInterface, i) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivity(intent);
                    } finally {
                        finish();
                    }
                })
                .setNegativeButton("앱 종료하기", (dialogInterface, i) -> finish())
                .setCancelable(false)
                .show();
    }
}

