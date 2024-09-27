package com.seyoung.alonepeople;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class apiTestPage extends AppCompatActivity {

    TextView result1;
    TextView result2;
    TextView result3;
    TextView result4;
    EditText edit;
    Button button;

    private String key = "e8KTlQRE%2FBEp0%2FkRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw%2FQ2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ%3D%3D";
    String data;
    XmlPullParser xpp;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.api_test_page);

        edit = findViewById(R.id.edit);
        button = findViewById(R.id.button);

        result1 = findViewById(R.id.result1);
        result2 = findViewById(R.id.result2);
        result3 = findViewById(R.id.result3);
        result4 = findViewById(R.id.result4);

    }

    public void mOnClick(View v) {
        if (v.getId() == R.id.button) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    data = getXmlData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            result1.setText(data);
                            result2.setText(data);
                            result3.setText(data);
                            result4.setText(data);
                        }
                    });
                }
            }) {
            }.start();
        }
    }

    // https://cpcp127.tistory.com/16
    String getXmlData() {
        StringBuffer buffer = new StringBuffer();
        String str = edit.getText().toString();

        String location = URLEncoder.encode(str);
        Log.d("location   " + "location   ", location);

        String queryUrl = "https://apis.data.go.kr/B552657/HsptlAsembySearchService/getHsptlMdcncListInfoInqire?serviceKey=e8KTlQRE%2FBEp0%2FkRGPGRPDSk2HBjZn253hX1jPyfCE1txYtnRw%2FQ2n6xRhMx1yHBcah8IxLOsCSrVsejfw4vhQ%3D%3D&Q0="+location+"&pageNo=1&numOfRows=10";
        Log.d("queryUrl", "queryUrl   " + queryUrl);

        try {
            URL url = new URL(queryUrl);
            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;

            xpp.next();
            int eventType = xpp.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작\n\n");
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();    //테그 이름 얻어오기

                        if (tag.equals("item"));    // 첫번째 검색결과
                        else if (tag.equals("dutyAddr")) {
                            buffer.append("주소 : ");
                            xpp.next();
                            buffer.append(xpp.getText());   // TEXT 읽어와서 문자열버퍼에 추가
                            buffer.append("\n");
                        } else if (tag.equals("dutyInf")) {
                            buffer.append("기관설명상세 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        } else if (tag.equals("dutyName")) {
                            buffer.append("기관명 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        } else if (tag.equals("dutyTel1")) {
                            buffer.append("대표전화 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                        case XmlPullParser.END_TAG:
                            tag = xpp.getName();

                            if (tag.equals("item")) buffer.append("\n");
                            break;

                }
                eventType = xpp.next();

            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        buffer.append("파싱 끝\n");
        return  buffer.toString();
    }
}
