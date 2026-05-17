package com.example.liferpg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GestureTaskActivity extends AppCompatActivity {

    // UI 组件
    private View gestureArea;
    private EditText etTaskTitle;
    private TextView tvExpReward;
    private EditText etDueDate;
    private EditText etRemark;
    private TextView tvHint;
    private Button btnClear;
    private Button btnAdd;
    private Button btnBack;
    private Switch switchVibrate;
    private Switch switchSound;

    // 数据
    private int currentExp = 0;
    private long lastClickTime = 0;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    // 手势类型常量
    private static final int GESTURE_SINGLE_CLICK = 1;
    private static final int GESTURE_LONG_PRESS = 2;
    private static final int GESTURE_DOUBLE_CLICK = 3;
    private static final int KEY_VOLUME_UP = 4;
    private static final int KEY_VOLUME_DOWN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_task);

        // 初始化组件
        initViews();

        // 初始化系统服务
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // 加载保存的设置
        loadSettings();

        // 设置监听器
        setupListeners();
    }

    private void initViews() {
        gestureArea = findViewById(R.id.gesture_area);
        etTaskTitle = findViewById(R.id.et_task_title);
        etDueDate = findViewById(R.id.et_due_date);
        etRemark = findViewById(R.id.et_remark);
        tvExpReward = findViewById(R.id.tv_exp_reward);
        tvHint = findViewById(R.id.tv_hint);
        btnClear = findViewById(R.id.btn_clear);
        btnAdd = findViewById(R.id.btn_add);
        btnBack = findViewById(R.id.btn_back);
        switchVibrate = findViewById(R.id.switch_vibrate);
        switchSound = findViewById(R.id.switch_sound);
    }

    private void loadSettings() {
        switchVibrate.setChecked(prefs.getBoolean("vibrate", true));
        switchSound.setChecked(prefs.getBoolean("sound", true));
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("vibrate", switchVibrate.isChecked());
        editor.putBoolean("sound", switchSound.isChecked());
        editor.apply();
    }

    private void setupListeners() {
        // 触摸事件监听（核心）
        gestureArea.setOnTouchListener(new View.OnTouchListener() {
            private Handler handler = new Handler();
            private Runnable longPressRunnable;
            private boolean isLongPressed = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongPressed = false;
                        // 设置长按检测
                        longPressRunnable = new Runnable() {
                            @Override
                            public void run() {
                                isLongPressed = true;
                                handleGesture(GESTURE_LONG_PRESS);
                            }
                        };
                        handler.postDelayed(longPressRunnable, 500);
                        break;

                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(longPressRunnable);
                        if (!isLongPressed) {
                            // 判断是单击还是双击
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastClickTime < 300) {
                                handleGesture(GESTURE_DOUBLE_CLICK);
                                lastClickTime = 0;
                            } else {
                                lastClickTime = currentTime;
                                // 延迟判断是否是双击
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (lastClickTime != 0) {
                                            handleGesture(GESTURE_SINGLE_CLICK);
                                            lastClickTime = 0;
                                        }
                                    }
                                }, 300);
                            }
                        }
                        break;
                }
                return true;
            }
        });

        // 清空按钮
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etTaskTitle.setText("");
                currentExp = 0;
                tvExpReward.setText("0");
                tvHint.setText("已清空，请重新选择手势");
                playFeedback(GESTURE_SINGLE_CLICK);
            }
        });

        // 添加任务按钮
        // 修改添加任务按钮的点击事件（在 setupListeners() 方法中）

// 添加任务按钮
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTaskTitle.getText().toString().trim();
                String dueDate = etDueDate.getText().toString().trim();
                String remark = etRemark.getText().toString().trim();

                if (title.isEmpty()) {
                    Toast.makeText(GestureTaskActivity.this, "请输入任务名称", Toast.LENGTH_SHORT).show();
                    playFeedback(GESTURE_SINGLE_CLICK);
                    return;
                }
                if (currentExp == 0) {
                    Toast.makeText(GestureTaskActivity.this, "请通过手势选择经验值", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 将任务数据返回给调用方
                Intent resultIntent = new Intent();
                resultIntent.putExtra("task_title", title);
                resultIntent.putExtra("task_exp", currentExp);
                resultIntent.putExtra("task_due_date", dueDate);
                resultIntent.putExtra("task_remark", remark);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(GestureTaskActivity.this, "委托已创建：" + title + " (" + currentExp + " EXP)", Toast.LENGTH_SHORT).show();
                finish();  // 返回上一个页面
            }
        });
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        // 设置开关监听
        switchVibrate.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> saveSettings());
    }

    /**
     * 处理各种手势和按键事件
     */
    private void handleGesture(int gestureType) {
        switch (gestureType) {
            case GESTURE_SINGLE_CLICK:
                currentExp = 10;
                tvExpReward.setText("10");
                tvHint.setText("✓ 单击：简单任务 +10 EXP");
                break;
            case GESTURE_LONG_PRESS:
                currentExp = 30;
                tvExpReward.setText("30");
                tvHint.setText("✓ 长按：普通任务 +30 EXP");
                break;
            case GESTURE_DOUBLE_CLICK:
                currentExp = 50;
                tvExpReward.setText("50");
                tvHint.setText("✓ 双击：困难任务 +50 EXP");
                break;
            case KEY_VOLUME_UP:
                if (currentExp < 100) {
                    currentExp += 10;
                    tvExpReward.setText(String.valueOf(currentExp));
                    tvHint.setText("音量+：经验值增加10，当前 " + currentExp + " EXP");
                }
                break;
            case KEY_VOLUME_DOWN:
                if (currentExp > 0) {
                    currentExp -= 10;
                    tvExpReward.setText(String.valueOf(currentExp));
                    tvHint.setText("音量-：经验值减少10，当前 " + currentExp + " EXP");
                }
                break;
        }
        // 播放反馈（震动+音效）
        playFeedback(gestureType);
    }

    /**
     * 播放反馈效果（震动 + 音效）
     */
    private void playFeedback(int gestureType) {
        // 震动反馈
        if (switchVibrate.isChecked() && vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }

        // 音效反馈
        if (switchSound.isChecked()) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
    }

    // ==================== 按键事件处理（核心） ====================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                handleGesture(KEY_VOLUME_UP);
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                handleGesture(KEY_VOLUME_DOWN);
                return true;

            case KeyEvent.KEYCODE_BACK:
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}