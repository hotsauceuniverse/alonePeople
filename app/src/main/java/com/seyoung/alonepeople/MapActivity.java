package com.seyoung.alonepeople;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private ViewGroup mapViewContainer;
    private FusedLocationProviderClient fusedLocationClient;
    private Button locationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // 권한ID를 가져옵니다
        int permission1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);

        int permission2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int permission3 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        // 권한이 열려있는지 확인
        if (permission1 == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED || permission3 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한 확인
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크(READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            return;
        }

        //지도 설정
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 현재 위치 가져오기
        getCurrentLocation();

        // 커스텀 말풍선 등록
        mapView.setCalloutBalloonAdapter(new CustomBalloonAdapter(getLayoutInflater()));
    }

    private void getCurrentLocation() {
        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // 위치를 성공적으로 가져왔을 때
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Log.d("latitude   ", "latitude   " + latitude);
                                Log.d("longitude   ", "longitude   " + longitude);

                                getAddressFromLocation(latitude, longitude);
                            }
                        }
                    });
        } else {
            Log.d("error", "error");
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                // https://junyoru.tistory.com/92
                Address address = addresses.get(0);
//                String fullAddress = address.getAddressLine(0);  // 전체 주소 -> 대한민국 서울특별시 영등포구 여의대로 -> null ok
//                String state = address.getAdminArea();            // 도시 -> 서울특별시 -> ok
//                String districtName = address.getSubLocality();    // 구 -> 영등포구 -> ok
                String roadName = address.getFeatureName();         // 도로명 주소 -> 여의대로 -> ok
                Log.d("roadName   ", "roadName   " + roadName);

                // https://bennyziiolab.tistory.com/entry/Lecture-35-Java14-URLEncode-Decode-JSON-Library-socket-tcpserver-tcpclient
                String encodeAddress = URLEncoder.encode(roadName, "UTF-8");

                String queryUrl = "https://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncListInfoInqire?serviceKey=e8KTlQRE%2FBEp0%2FkRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw%2FQ2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ%3D%3D&Q0=" + encodeAddress + "&pageNo=1&numOfRows=10";
                Log.d("queryUrl   ", "queryUrl   " + queryUrl);

                callApi(queryUrl);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void callApi(String queryUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, queryUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ok", response);
                // 응답을 받은 후 바로 마커 추가
                addCustomMarker(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", "error");
            }
        });
        queue.add(stringRequest);
    }

    // api 호출 후 api내에 위도/경도를 통해 지도에 마커 찍기
    public void addCustomMarker(String response) {
        locationBtn = findViewById(R.id.location_btn);
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // XML 응답을 처리
                    // https://www.masterqna.com/android/49958
                    InputStream inputStream = new ByteArrayInputStream(response.getBytes("ISO-8859-1"));
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(inputStream);
                    doc.getDocumentElement().normalize();

                    NodeList nodeList = doc.getElementsByTagName("item");

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;

                            Node lonNode = element.getElementsByTagName("wgs84Lon").item(0);
                            Node latNode = element.getElementsByTagName("wgs84Lat").item(0);
                            Node nameNode = element.getElementsByTagName("dutyName").item(0);

                            // 위도/경도가 존재하는지 확인
                            if (lonNode != null && latNode != null && nameNode != null) {
                                double lon = Double.parseDouble(lonNode.getTextContent());
                                double lat = Double.parseDouble(latNode.getTextContent());
                                String name = nameNode.getTextContent();

                                Log.d("lat", "lat   " + lat);
                                Log.d("lon", "lon   " + lon);
                                Log.d("name", "name   " + name);

                                // 위치를 약간씩 변동시키기 (중복 마커 문제 해결)
                                // 같은 위치일 경우 마커를 약간 위로 이동
                                lat += (i * 0.000017);
                                lon += (i * 0.000017);

                                // 마커 추가
                                MapPOIItem marker = new MapPOIItem();
                                marker.setItemName(name);
                                marker.setTag(i);
                                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(lat, lon));
                                marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

                                // 지도에 마커 추가
                                mapView.addPOIItem(marker);
                            } else {
                                Log.d("Error", "위도/경도가 존재하지 않습니다.");
                            }
                        }
                    }
                } catch (Exception xmlException) {
                    xmlException.printStackTrace();
                }
            }
        });
    }

    // 마커 클릭 이벤트 추가
    // https://mechacat.tistory.com/17
    public class CustomBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;
        private final TextView name;
        private final TextView address;

        public CustomBalloonAdapter(LayoutInflater inflater) {
            mCalloutBalloon = inflater.inflate(R.layout.map_cardview, null);
            name = mCalloutBalloon.findViewById(R.id.ball_tv_name);
            address = mCalloutBalloon.findViewById(R.id.ball_tv_address);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem mapPOIItem) {
            name.setText(mapPOIItem != null ? mapPOIItem.getItemName() : "");
            address.setText("getCalloutBalloon");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem mapPOIItem) {
            address.setText("getPressedCalloutBalloon");
            Log.d("222   ", "222   " + mapPOIItem.getItemName());
            return mCalloutBalloon;
        }
    }

    // 권한 체크 이후로직
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (requestCode == 1000) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if (check_result == false) {
                finish();
            }
        }
    }
}
