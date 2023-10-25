package com.android.ddsoft;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AccessActivity extends AppCompatActivity {

    private EditText accessCodeEditText;
    private Button accessButton;
    private String adminCode = "admin123";
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PREF_KEY_ACCESS_CODE = "accessCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        accessCodeEditText = findViewById(R.id.accessCodeEditText);
        accessButton = findViewById(R.id.accessButton);

        // Load saved access code from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedCode = prefs.getString(PREF_KEY_ACCESS_CODE, "");
        if (!savedCode.isEmpty()) {
            // If a code is saved, automatically fill the EditText
            accessCodeEditText.setText(savedCode);
            // Automatically attempt to log in the user
            attemptLogin(savedCode);
        }

        accessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredCode = accessCodeEditText.getText().toString().trim();
                attemptLogin(enteredCode);
            }
        });
    }

    private void attemptLogin(String enteredCode) {
        if (checkAccessCode(enteredCode)) {
            if (!isCodeInUse(enteredCode)) {
                if (isCodeExpired()) {
                    // Code is expired, deny access
                    Toast.makeText(AccessActivity.this, "Access Denied. Code expired.", Toast.LENGTH_SHORT).show();
                } else {
                    // Code is valid, grant access
                    Toast.makeText(AccessActivity.this, "Access Granted!", Toast.LENGTH_SHORT).show();
                    saveAccessTime(enteredCode);
                    saveAccessCode(enteredCode); // Save the access code in SharedPreferences
                    navigateToMainActivity(); // Navigate to the main activity
                }
            } else {
                // Code is already in use, deny access
                Toast.makeText(AccessActivity.this, "Access Denied. Code in use on another device.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Incorrect code, deny access
            Toast.makeText(AccessActivity.this, "Access Denied. Incorrect code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AccessActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkAccessCode(String enteredCode) {
        return enteredCode.equals(adminCode);
    }

    private void saveAccessTime(String code) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(code + "_lastAccess", getCurrentDate());
        editor.apply();
    }

    private boolean isCodeExpired() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastAccess = prefs.getString("lastAccess", "");
        if (!lastAccess.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date accessDate = dateFormat.parse(lastAccess);
                Calendar cal = Calendar.getInstance();
                cal.setTime(accessDate);
                cal.add(Calendar.YEAR, 1); // Add 1 year to the last access date
                Date expirationDate = cal.getTime();
                Date currentDate = new Date();
                return currentDate.after(expirationDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean isCodeInUse(String code) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastAccess = prefs.getString(code + "_lastAccess", "");
        return !lastAccess.isEmpty();
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private void saveAccessCode(String code) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(PREF_KEY_ACCESS_CODE, code);
        editor.apply();
    }
}
