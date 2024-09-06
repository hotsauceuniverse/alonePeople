package com.seyoung.alonepeople;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HouseholdAccountAddActivity extends AppCompatActivity {

    AutoCompleteTextView expenseCategoryTextView;
    AutoCompleteTextView expenseTextView;
    AutoCompleteTextView creditCardTextView;
    ArrayAdapter<String> adapterItems;

    String[] expenseCategory = {"지출", "수입", "저축"};
    String[] expense = {"식비", "교통비", "생활비", "기타"};
    String[] creditCard = {"신한카드", "삼성카드", "국민카드", "현대카드", "롯데카드", "하나카드", "우리카드", "비씨카드", "농협카드", "카카오뱅크", "케이뱅크", "기타"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.household_account_add);

        expenseCategoryTextView = findViewById(R.id.expense_category);
        expenseTextView = findViewById(R.id.expense);
        creditCardTextView = findViewById(R.id.credit_card);

        adapterItems = new ArrayAdapter<String>(this, R.layout.dropdown_item, expenseCategory);
        adapterItems = new ArrayAdapter<String>(this, R.layout.dropdown_item, expense);
        adapterItems = new ArrayAdapter<String>(this, R.layout.dropdown_item, creditCard);

        expenseCategoryTextView.setAdapter(adapterItems);
        expenseCategoryTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(HouseholdAccountAddActivity.this, "Item : " + item, Toast.LENGTH_SHORT).show();
            }
        });

        expenseTextView.setAdapter(adapterItems);
        expenseTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(HouseholdAccountAddActivity.this, "Item : " + item, Toast.LENGTH_SHORT).show();
            }
        });

        creditCardTextView.setAdapter(adapterItems);
        creditCardTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(HouseholdAccountAddActivity.this, "Item : " + item, Toast.LENGTH_SHORT).show();
            }
        });
    }
}