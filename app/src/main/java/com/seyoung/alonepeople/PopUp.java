package com.seyoung.alonepeople;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

public class PopUp extends Dialog {

    private TextView ConfirmTextView;
    private AppCompatButton CloseBtn;

    public PopUp(@NonNull Context context, String contents) {
        super(context);
        setContentView(R.layout.popup);

        ConfirmTextView = findViewById(R.id.confirmTextView);
        ConfirmTextView.setText(contents);
        CloseBtn = findViewById(R.id.closeBtn);
        CloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
