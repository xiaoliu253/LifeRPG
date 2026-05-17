package com.example.liferpg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI 组件
    private TextView tvLevel;
    private TextView tvTitle;
    private TextView tvNextTitle;
    private View expBar;
    private TextView tvCurrentExp;
    private TextView tvRequiredExp;
    private TextView tvTaskStats;
    private RecyclerView rvTaskList;
    private ImageView ivUserAvatar;

    // 数据
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private int currentLevel = 1;
    private int currentExp = 0;
    private int totalExp = 0;
    private String currentTitle = "初学者";

    // 默认头像资源数组（与 UserProfileActivity 保持一致）
    private int[] avatarResources = {
            R.drawable.bird_one,
            R.drawable.bird_two,
            R.drawable.bird_three,
            R.drawable.bird_four,
            R.drawable.bird_five,
            R.drawable.bird_six
    };

    // 头衔数组（根据等级索引，1-15级）
    private String[] titles = {
            "", "初学者", "践行者", "坚持者", "探索者", "成长者",
            "进取者", "专注者", "钻研者", "攀登者", "行家",
            "领跑者", "卓越者", "智识者", "引路者", "知行合一者"
    };

    // SharedPreferences 键名
    private static final String TASKS_PREFS = "tasks_prefs";
    private static final String KEY_TASK_LIST = "task_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查登录状态（如果从登录页跳转过来则不需要）
        SharedPreferences loginPrefs = getSharedPreferences("login_state", MODE_PRIVATE);
        boolean isLoggedIn = loginPrefs.getBoolean("is_logged_in", false);
        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        loadUserData();
        loadTasks();
        updateExpBar();
        loadAvatar();

        // 处理首次打开时的 Intent 数据
        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvatar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        android.util.Log.d("MainActivity", "onNewIntent 被调用");
        handleIntent(intent);
        loadAvatar();
    }

    /**
     * 加载头像
     */
    private void loadAvatar() {
        SharedPreferences avatarPrefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int avatarIndex = avatarPrefs.getInt("avatar_index", 0);
        if (avatarIndex >= 0 && avatarIndex < avatarResources.length) {
            ivUserAvatar.setImageResource(avatarResources[avatarIndex]);
        } else {
            ivUserAvatar.setImageResource(avatarResources[0]);
        }
    }

    /**
     * 处理 Intent 中的数据
     */
    private void handleIntent(Intent intent) {
        if (intent == null) return;

        android.util.Log.d("MainActivity", "handleIntent 被调用");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                android.util.Log.d("MainActivity", "  " + key + " = " + extras.get(key));
            }
        } else {
            android.util.Log.d("MainActivity", "没有 extra 数据");
        }

        String taskTitle = intent.getStringExtra("task_title");
        int taskExp = intent.getIntExtra("task_exp", 0);

        android.util.Log.d("MainActivity", "task_title: " + taskTitle);
        android.util.Log.d("MainActivity", "task_exp: " + taskExp);

        if (taskTitle != null && !taskTitle.isEmpty() && taskExp > 0) {
            String dueDate = intent.getStringExtra("task_due_date");
            String remark = intent.getStringExtra("task_remark");
            addNewTask(taskTitle, taskExp, dueDate, remark);

            intent.removeExtra("task_title");
            intent.removeExtra("task_exp");
            intent.removeExtra("task_due_date");
            intent.removeExtra("task_remark");
        }
    }

    private void initViews() {
        tvLevel = findViewById(R.id.tv_level);
        tvTitle = findViewById(R.id.tv_title);
        tvNextTitle = findViewById(R.id.tv_next_title);
        expBar = findViewById(R.id.exp_bar);
        tvCurrentExp = findViewById(R.id.tv_current_exp);
        tvRequiredExp = findViewById(R.id.tv_required_exp);
        tvTaskStats = findViewById(R.id.tv_task_stats);
        rvTaskList = findViewById(R.id.rv_task_list);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);

        taskAdapter = new TaskAdapter(taskList, this::onTaskCompleted);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        rvTaskList.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        ivUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 添加新任务到列表
     */
    private void addNewTask(String title, int expReward, String dueDate, String remark) {
        int newId = taskList.size() + 1;
        Task newTask = new Task(newId, title, expReward, false,
                (dueDate != null ? dueDate : ""),
                (remark != null ? remark : ""));
        taskList.add(0, newTask);
        taskAdapter.notifyItemInserted(0);
        updateTaskStats();
        saveTasks();
        Toast.makeText(this, "新委托已添加：" + title, Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        currentLevel = prefs.getInt("level", 1);
        currentExp = prefs.getInt("current_exp", 0);
        totalExp = prefs.getInt("total_exp", 0);
        currentTitle = prefs.getString("title", "初学者");

        if (currentLevel >= 1 && currentLevel <= 15) {
            currentTitle = titles[currentLevel];
        }
        updateUserInfoDisplay();
    }

    /**
     * 保存用户数据
     */
    private void saveUserData() {
        SharedPreferences.Editor editor = getSharedPreferences("user_data", MODE_PRIVATE).edit();
        editor.putInt("level", currentLevel);
        editor.putInt("current_exp", currentExp);
        editor.putInt("total_exp", totalExp);
        editor.putString("title", currentTitle);
        editor.apply();
    }

    /**
     * 保存完整任务列表
     */
    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(TASKS_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < taskList.size(); i++) {
            if (i > 0) sb.append("|||");
            sb.append(taskList.get(i).toStorageString());
        }
        editor.putString(KEY_TASK_LIST, sb.toString());
        editor.apply();
    }

    /**
     * 加载任务列表
     */
    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences(TASKS_PREFS, MODE_PRIVATE);
        String savedTaskList = prefs.getString(KEY_TASK_LIST, null);

        taskList.clear();

        if (savedTaskList != null && !savedTaskList.isEmpty()) {
            String[] tasks = savedTaskList.split("\\|\\|\\|");
            for (String taskStr : tasks) {
                Task task = Task.fromStorageString(taskStr);
                if (task != null) {
                    taskList.add(task);
                }
            }
        } else {
            // 首次运行，添加默认任务
            taskList.add(new Task(1, "完成实训报告", 50, false, "今日 23:59", "提交电子版和纸质版"));
            taskList.add(new Task(2, "晨跑30分钟", 30, false, "今日 08:00", "记得拉伸"));
            taskList.add(new Task(3, "背单词20个", 10, false, "今日 20:00", "记得背诵"));
        }

        taskAdapter.notifyDataSetChanged();
        updateTaskStats();
    }

    /**
     * 更新用户信息显示
     */
    private void updateUserInfoDisplay() {
        tvLevel.setText("Lv." + currentLevel);
        tvTitle.setText(currentTitle);
        if (currentLevel < 15) {
            tvNextTitle.setText("下一级：" + titles[currentLevel + 1]);
        } else {
            tvNextTitle.setText("已达最高级");
        }
        updateExpBar();
    }

    /**
     * 更新经验条
     */
    private void updateExpBar() {
        try {
            int requiredExp = getRequiredExp(currentLevel);
            tvCurrentExp.setText(String.valueOf(currentExp));
            tvRequiredExp.setText("/ " + requiredExp);

            int progressPercent = (int) ((float) currentExp / requiredExp * 100);
            if (progressPercent > 100) progressPercent = 100;
            if (progressPercent < 0) progressPercent = 0;

            android.util.Log.d("MainActivity", "更新经验条: progressPercent=" + progressPercent);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) expBar.getLayoutParams();
            params.weight = progressPercent;
            params.width = 0;
            expBar.setLayoutParams(params);

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "updateExpBar 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取升级所需经验
     */
    private int getRequiredExp(int level) {
        return 100 * level;
    }

    /**
     * 完成任务后增加经验值
     */
    private void addExp(int expReward) {
        android.util.Log.d("MainActivity", "增加经验前: currentExp=" + currentExp + ", level=" + currentLevel);

        currentExp += expReward;
        totalExp += expReward;

        android.util.Log.d("MainActivity", "增加经验后: currentExp=" + currentExp + ", level=" + currentLevel);

        int requiredExp = getRequiredExp(currentLevel);
        boolean leveledUp = false;

        while (currentExp >= requiredExp && currentLevel < 15) {
            currentExp -= requiredExp;
            currentLevel++;
            requiredExp = getRequiredExp(currentLevel);
            leveledUp = true;
            android.util.Log.d("MainActivity", "升级! 新等级=" + currentLevel + ", 剩余经验=" + currentExp);
        }

        if (leveledUp) {
            if (currentLevel <= 15) {
                currentTitle = titles[currentLevel];
            }
            Toast.makeText(this, "恭喜！升级到 " + currentTitle + "！", Toast.LENGTH_LONG).show();
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(100);
            }
        }

        updateUserInfoDisplay();
        saveUserData();

        android.util.Log.d("MainActivity", "最终: currentExp=" + currentExp + ", level=" + currentLevel);
    }

    /**
     * 更新任务统计
     */
    private void updateTaskStats() {
        int total = taskList.size();
        int completed = 0;
        for (Task task : taskList) {
            if (task.isCompleted()) {
                completed++;
            }
        }
        tvTaskStats.setText(completed + "/" + total);

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        prefs.edit().putInt("completed_tasks", completed).apply();
    }

    /**
     * 任务完成时的回调
     */
    private void onTaskCompleted(Task task) {
        android.util.Log.d("MainActivity", "onTaskCompleted 被调用, 任务=" + task.getTitle());
        android.util.Log.d("MainActivity", "task.getExpReward() = " + task.getExpReward());

        addExp(task.getExpReward());
        taskAdapter.notifyDataSetChanged();
        updateTaskStats();
        saveTasks();
        Toast.makeText(this, "完成任务：" + task.getTitle() + " +" + task.getExpReward() + " EXP", Toast.LENGTH_SHORT).show();
    }
}