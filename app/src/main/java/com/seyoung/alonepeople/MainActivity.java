package com.seyoung.alonepeople;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private LinearLayout Five;
    private LinearLayout three;
    private LinearLayout four;
    private TextView Step;          // 걸음 수
    private TextView Distance;      // 이동거리
    private TextView Kcal;          // 칼로리

    private int totalStep = 0;
    private long totalStepTimeNumber = 0;
    private float totalCaloriesFloat = 0;
    private int totalCaloriesInt = 0;
    private float totalDistanceFloat = 0;
    private int totalDistanceInt = 0;
    private String totalStepTimeString = "";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private SensorManager sensorManager;
    private Sensor stepSensor;

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1001);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        Step = findViewById(R.id.step);
        Distance = findViewById(R.id.distance);
        Kcal = findViewById(R.id.kcal);

        // 센서 초기화
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // https://mcflynn.tistory.com/3, https://blog.naver.com/sinda72/222158733556, https://blog.naver.com/cloud_akashic_record/221818634311
        // 내가 필요한 걸음 수, 걸은 거리, 걸은 시간, 칼로리 데이터를 가져오기 위해 필요한 권한을 정의
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .build();

        // 앱 실행 후 연결 된 구글 계정에 Fitness 접근 권한이 있는지 확인
        // 만약 권한이 없다면 권한을 요청하는 팝업을 만들어 주고 권한 요청에 대한 결과값을 onActivityResult에서 받음
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, REQUEST_OAUTH_REQUEST_CODE, GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
        } else {
            subscribe();
        }

        Five = findViewById(R.id.five);
        Five.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        three = findViewById(R.id.three);
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, apiTestPage.class);
                startActivity(intent);
            }
        });

        four = findViewById(R.id.four);
        four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MenuChoiceActivity.class);
                startActivity(intent);
            }
        });
    }

    // 걸음 수 실시간 측정
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            totalStep += (int) sensorEvent.values[0];
            Step.setText(String.valueOf(totalStep));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }

    public void subscribe() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                // TYPE_STEP_COUNT_CUMULATIVE는 누적걸음수를 한번에 조회하기 위해 필요한 권한
                // TYPE_STEP_COUNT_DELTA는 단위시간별 걸음수를 확인하기 위해 필요한 권한
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("success   ", "success   " + task.isSuccessful());
                            readData();
                        } else {
                            Log.d("fail   ", "fail   " + task.getException());
                        }
                    }
                });
    }

    private void readData() {
        final Calendar calendar = Calendar.getInstance();
        Date now  = Calendar.getInstance().getTime();
        calendar.setTime(now);

        // 시작 시간
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),0, 0, 0);
        long startTime = calendar.getTimeInMillis();

        // 종료 시간
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        long endTime = calendar.getTimeInMillis();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)   // Raw 걸음 수
                        .read(DataType.TYPE_CALORIES_EXPENDED)  // 칼로리 데이터
                        .read(DataType.TYPE_DISTANCE_DELTA)     // 이동거리 데이터
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)  // 시간 범위 설정
                        .build())
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse response) {
//                        int totalStep = 0;
//                        long totalStepTimeNumber = 0;
//                        float totalCaloriesFloat = 0;
//                        int totalCaloriesInt = 0;
//                        float totalDistanceFloat = 0;
//                        int totalDistanceInt = 0;
//                        String totalStepTimeString = "";

                        // 걸음 수 계산
                        DataSet dataSetStepCount = response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        for (DataPoint dpStep : dataSetStepCount.getDataPoints()) {
                            totalStepTimeNumber += dpStep.getEndTime(TimeUnit.MILLISECONDS) - dpStep.getStartTime(TimeUnit.MILLISECONDS);
                            for (Field field : dpStep.getDataType().getFields()) {
                                int step = dpStep.getValue(field).asInt();
                                if (!"user_input".equals(dpStep.getOriginalDataSource().getStreamName())) {
                                    totalStep += step;
                                }
                            }
                        }

                        // 시간 형식 설정
                        if (totalStepTimeNumber > 0) {
                            totalStepTimeString = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalStepTimeNumber),
                                    TimeUnit.MILLISECONDS.toMinutes(totalStepTimeNumber) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalStepTimeNumber)),
                                    TimeUnit.MILLISECONDS.toSeconds(totalStepTimeNumber) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalStepTimeNumber)));
                        }

                        // 거리 계산
                        DataSet dataSetDistance = response.getDataSet(DataType.TYPE_DISTANCE_DELTA);
                        for (DataPoint daDis : dataSetDistance.getDataPoints()) {
                            for (Field field : daDis.getDataType().getFields()) {
                                totalDistanceFloat += daDis.getValue(field).asFloat();
                            }
                        }

                        // 거리 단위를 km로 변환하고 소수점 2자리까지 표시
                        float totalDistanceKm = totalDistanceFloat / 1000;
                        String totalDistanceKmString = String.format("%.2f", totalDistanceKm);

                        // 칼로리 계산
                        DataSet dataSetCalories = response.getDataSet(DataType.TYPE_CALORIES_EXPENDED);
                        if (dataSetCalories.isEmpty()) {
                            Log.d("데이터 없음   ", "데이터 없음   ");
                        } else{
                            for (DataPoint daCal : dataSetCalories.getDataPoints()) {
                                for (Field field : daCal.getDataType().getFields()) {
                                    totalCaloriesFloat += daCal.getValue(field).asFloat();
                                }
                            }
                            totalCaloriesInt = (int) Math.floor(totalCaloriesFloat);
                        }

                        // 결과 출력
                        Log.d("StepInfo", "오늘 걸은 걸음 수 : " + totalStep);
                        Log.d("StepInfo", "오늘 걸은 시간 : " + totalStepTimeString);
                        Log.d("StepInfo", "오늘 걸은 거리 : " + totalDistanceKmString + "km");
                        Log.d("StepInfo", "오늘 총 소모 칼로리 : " + totalCaloriesInt + "cal");

                        // UI 업데이트
                        Step.setText(String.valueOf(totalStep));
                        Distance.setText(totalDistanceKmString);
                        Kcal.setText(String.valueOf(totalCaloriesInt));
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "fail   ", e);
                    }
                });
    }
}
