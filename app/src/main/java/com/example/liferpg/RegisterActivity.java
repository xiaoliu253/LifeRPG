package com.example.liferpg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_reg_username);
        etPassword = findViewById(R.id.et_reg_password);
        etConfirmPassword = findViewById(R.id.et_reg_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. 检查用户名是否为空
        if (username.isEmpty()) {
            etUsername.setError("用户名不能为空");
            etUsername.requestFocus();
            return;
        }

        // 2. 检查密码是否为空
        if (password.isEmpty()) {
            etPassword.setError("密码不能为空");
            etPassword.requestFocus();
            return;
        }

        // 3. 检查密码长度（至少6位）
        if (password.length() < 6) {
            etPassword.setError("密码至少6位");
            etPassword.requestFocus();
            return;
        }

        // 4. 检查两次密码是否一致
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次密码不一致");
            etConfirmPassword.requestFocus();
            return;
        }

        // 5. 保存用户信息
        saveUserInfo(username, password);


        Toast.makeText(this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 保存用户信息到 SharedPreferences
     */
    private void saveUserInfo(String username, String password) {
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        // 注册日期
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        editor.putString("register_date", sdf.format(new java.util.Date()));
        editor.apply();
    }
}