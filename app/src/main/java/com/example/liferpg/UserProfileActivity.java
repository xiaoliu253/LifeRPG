package com.example.liferpg;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    // UI 组件
    private Button btnBack;
    private TextView tvUsername;
    private TextView tvLevel;
    private TextView tvTitle;
    private TextView tvTotalExp;
    private TextView tvCompletedTasks;
    private TextView tvRegisterDate;
    private TextView tvLastLogin;
    private TextView btnChangeUsername;
    private TextView btnChangePassword;
    private Button btnLogout;
    private Button btnNewGestureTask;
    private ImageView ivAvatar;
    private Button btnChangeAvatar;

    // 数据
    private SharedPreferences userPrefs;
    private SharedPreferences gamePrefs;
    private String currentUsername;  // 当前登录用户名

    // 默认头像资源数组
    private int[] avatarResources = {
            R.drawable.bird_one,
            R.drawable.bird_two,
            R.drawable.bird_three,
            R.drawable.bird_four,
            R.drawable.bird_five,
            R.drawable.bird_six
    };

    // 头衔数组
    private String[] titles = {
            "", "初学者", "践行者", "坚持者", "探索者", "成长者",
            "进取者", "专注者", "钻研者", "攀登者", "行家",
            "领跑者", "卓越者", "智识者", "引路者", "知行合一者"
    };

    // 启动手势页面并接收返回结果
    private final ActivityResultLauncher<Intent> gestureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String title = result.getData().getStringExtra("task_title");
                    int expReward = result.getData().getIntExtra("task_exp", 0);
                    String dueDate = result.getData().getStringExtra("task_due_date");
                    String remark = result.getData().getStringExtra("task_remark");
                    if (title != null && !title.isEmpty() && expReward > 0) {
                        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                        intent.putExtra("task_title", title);
                        intent.putExtra("task_exp", expReward);
                        intent.putExtra("task_due_date", dueDate);
                        intent.putExtra("task_remark", remark);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initViews();
        loadCurrentUser();
        loadUserData();
        setupListeners();
        loadAvatar();
    }

    /**
     * 获取当前登录用户
     */
    private void loadCurrentUser() {
        SharedPreferences loginPrefs = getSharedPreferences("login_state", MODE_PRIVATE);
        currentUsername = loginPrefs.getString("current_user", "旅行者");
        userPrefs = getSharedPreferences("user_info", MODE_PRIVATE);
        gamePrefs = getSharedPreferences("user_data", MODE_PRIVATE);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvUsername = findViewById(R.id.tv_username);
        tvLevel = findViewById(R.id.tv_level);
        tvTitle = findViewById(R.id.tv_title);
        tvTotalExp = findViewById(R.id.tv_total_exp);
        tvCompletedTasks = findViewById(R.id.tv_completed_tasks);
        tvRegisterDate = findViewById(R.id.tv_register_date);
        tvLastLogin = findViewById(R.id.tv_last_login);
        btnChangeUsername = findViewById(R.id.btn_change_username);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnNewGestureTask = findViewById(R.id.btn_new_gesture_task);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
    }

    private void loadUserData() {
        // 读取用户基本信息
        String username = userPrefs.getString("username", currentUsername);
        String registerDate = userPrefs.getString("register_date", "2024-01-01");
        String lastLogin = userPrefs.getString("last_login", "2024-01-01 12:00");

        // 读取游戏数据
        int level = gamePrefs.getInt("level", 1);
        int totalExp = gamePrefs.getInt("total_exp", 0);
        int completedTasks = gamePrefs.getInt("completed_tasks", 0);

        String title = (level >= 1 && level <= 15) ? titles[level] : "初学者";

        tvUsername.setText(username);
        tvLevel.setText("Lv." + level);
        tvTitle.setText(title);
        tvTotalExp.setText(String.valueOf(totalExp));
        tvCompletedTasks.setText(String.valueOf(completedTasks));
        tvRegisterDate.setText(registerDate);
        tvLastLogin.setText(lastLogin);
    }

    private void loadAvatar() {
        SharedPreferences avatarPrefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int avatarIndex = avatarPrefs.getInt("avatar_index", 0);
        if (avatarIndex >= 0 && avatarIndex < avatarResources.length) {
            ivAvatar.setImageResource(avatarResources[avatarIndex]);
        } else {
            ivAvatar.setImageResource(avatarResources[0]);
        }
    }

    private void saveAvatar(int index) {
        SharedPreferences avatarPrefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        avatarPrefs.edit().putInt("avatar_index", index).apply();
        ivAvatar.setImageResource(avatarResources[index]);
        Toast.makeText(this, "头像已更换", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChangeUsername.setOnClickListener(v -> showChangeUsernameDialog());

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnNewGestureTask.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, GestureTaskActivity.class);
            gestureLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // 清除登录状态
            SharedPreferences loginPrefs = getSharedPreferences("login_state", MODE_PRIVATE);
            loginPrefs.edit().putBoolean("is_logged_in", false).clear().apply();

            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnChangeAvatar.setOnClickListener(v -> showChooseAvatarDialog());
    }

    /**
     * 显示选择头像对话框
     */
    private void showChooseAvatarDialog() {
        String[] avatarNames = {"头像1", "头像2", "头像3", "头像4", "头像5", "头像6"};

        SharedPreferences avatarPrefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int currentIndex = avatarPrefs.getInt("avatar_index", 0);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择头像")
                .setSingleChoiceItems(avatarNames, currentIndex, (dialog, which) -> {
                    saveAvatar(which);
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showChangeUsernameDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_username);

        EditText etNewUsername = dialog.findViewById(R.id.et_new_username);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newUsername = etNewUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(UserProfileActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newUsername.length() < 2) {
                Toast.makeText(UserProfileActivity.this, "昵称至少2个字符", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新用户名
            userPrefs.edit().putString("username", newUsername).apply();

            // 更新登录状态中的用户名
            SharedPreferences loginPrefs = getSharedPreferences("login_state", MODE_PRIVATE);
            loginPrefs.edit().putString("current_user", newUsername).apply();

            tvUsername.setText(newUsername);
            Toast.makeText(UserProfileActivity.this, "昵称修改成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_password);

        EditText etOldPassword = dialog.findViewById(R.id.et_old_password);
        EditText etNewPassword = dialog.findViewById(R.id.et_new_password);
        EditText etConfirmPassword = dialog.findViewById(R.id.et_confirm_password);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            String savedPassword = userPrefs.getString("password", "");

            if (oldPassword.isEmpty()) {
                Toast.makeText(UserProfileActivity.this, "请输入原密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!oldPassword.equals(savedPassword)) {
                Toast.makeText(UserProfileActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.isEmpty()) {
                Toast.makeText(UserProfileActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(UserProfileActivity.this, "新密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(UserProfileActivity.this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新密码
            userPrefs.edit().putString("password", newPassword).apply();
            Toast.makeText(UserProfileActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}