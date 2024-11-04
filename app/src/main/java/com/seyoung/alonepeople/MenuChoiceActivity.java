package com.seyoung.alonepeople;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class MenuChoiceActivity extends AppCompatActivity {

    private AppCompatButton StartBtn;
    private ImageView BackBtn;
    private ImageView SlotImg;
    private PopUp PopUp;

    private int[] imageResources = {
            R.drawable.chicken, R.drawable.jjajangmyeon, R.drawable.jjambbong, R.drawable.meat,
            R.drawable.pizza, R.drawable.salad, R.drawable.sushi, R.drawable.udon,
            R.drawable.tteokbbokki, R.drawable.spaghetti, R.drawable.ramyeon, R.drawable.odeng,
            R.drawable.jjigae, R.drawable.donggass, R.drawable.burger, R.drawable.bossam,
            R.drawable.bibimbap
    };

    private String[] imageLabels = {
            "치킨", "짜장면", "짬뽕", "고기",
            "피자", "샐러드", "스시", "우동",
            "떡볶이", "스파게티", "라면", "오뎅",
            "국밥", "돈가스", "햄버거", "보쌈",
            "비빔밥"
    };

    private Random random = new Random();
    private int currentIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_choice_activity);

        StartBtn = findViewById(R.id.start_btn);
        BackBtn = findViewById(R.id.back_btn);
        SlotImg = findViewById(R.id.slotImg);

        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleImages();
            }
        });

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void shuffleImages() {
        // ValueAnimator를 사용해 애니메이션의 속도를 설정 (0에서 이미지 개수 - 1까지)
        ValueAnimator animator = ValueAnimator.ofInt(0, imageResources.length -1);
        animator.setDuration(3000); // 애니메이션 지속 시간 설정
        animator.setRepeatCount(ValueAnimator.INFINITE); // 애니메이션 반복 설정

        // 애니메이션 진행 중에 호출되는 콜백
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentIndex = (int) animation.getAnimatedValue();
                SlotImg.setImageResource(imageResources[currentIndex]);
            }
        });

        // 애니메이션 반복 제한 또는 멈추는 로직 설정
        animator.setRepeatCount(1); // 반복 횟수 설정
        animator.setRepeatMode(ValueAnimator.RESTART);

        // 애니메이션 종료 후, 랜덤 이미지 선택
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                int randomIndex = random.nextInt(imageResources.length);
                SlotImg.setImageResource(imageResources[randomIndex]);

                showPopUp(randomIndex);
            }
        });
        animator.start();
    }

    private void showPopUp(int selectedIndex) {
        // 선택된 이미지에 대한 한글 이름을 가져오기
        String selectedLabel = imageLabels[selectedIndex];
        PopUp = new PopUp(this, "오늘은 " + selectedLabel + " 먹자!");

        Window window = PopUp.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            layoutParams.dimAmount = 0.8f;
            window.setAttributes(layoutParams);
        }
        PopUp.show();
    }
}
