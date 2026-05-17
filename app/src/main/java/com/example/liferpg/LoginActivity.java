package com.example.liferpg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        // 检查视图是否为空（调试用）
        if (btnLogin == null) {
            android.util.Log.e("LoginActivity", "btnLogin is null");
        }
        if (tvRegister == null) {
            android.util.Log.e("LoginActivity", "tvRegister is null");
        }
    }

    private void setupListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> attemptLogin());
        } else {
            android.util.Log.e("LoginActivity", "btnLogin is null, cannot set listener");
        }

        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            });
        } else {
            android.util.Log.e("LoginActivity", "tvRegister is null, cannot set listener");
        }
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("请输入用户名");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }

        if (checkUser(username, password)) {
            saveLoginState(username);
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkUser(String username, String password) {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "");
        String savedPassword = prefs.getString("password", "");

        if (savedUsername.isEmpty()) {
            return username.equals("demo") && password.equals("demo");
        }

        return username.equals(savedUsername) && password.equals(savedPassword);
    }

    private void saveLoginState(String username) {
        SharedPreferences prefs = getSharedPreferences("login_state", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("current_user", username);
        editor.apply();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        SharedPreferences userPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
        userPrefs.edit().putString("last_login", sdf.format(new java.util.Date())).apply();
    }
}