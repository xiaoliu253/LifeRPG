package com.example.liferpg;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ImageView ivUserAvatar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MainPagerAdapter pagerAdapter;
    private TaskListFragment taskListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 检查登录状态
        SharedPreferences loginPrefs = getSharedPreferences("login_state", MODE_PRIVATE);
        boolean isLoggedIn = loginPrefs.getBoolean("is_logged_in", false);
        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        loadAvatar();
        setupViewPager();

        handleIntent(getIntent());
    }

    private void initViews() {
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void loadAvatar() {
        SharedPreferences avatarPrefs = getSharedPreferences("avatar_prefs", MODE_PRIVATE);
        int avatarIndex = avatarPrefs.getInt("avatar_index", 0);
        int[] avatarResources = {
                android.R.drawable.sym_def_app_icon,
                android.R.drawable.ic_menu_gallery,
                android.R.drawable.ic_menu_camera,
                android.R.drawable.ic_menu_edit,
                android.R.drawable.ic_menu_save,
                android.R.drawable.ic_menu_delete
        };
        ivUserAvatar.setImageResource(avatarResources[avatarIndex]);

        ivUserAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("任务列表");
                    } else {
                        tab.setText("任务分类");
                    }
                }
        ).attach();

        // 获取 TaskListFragment 实例
        viewPager.post(() -> {
            taskListFragment = pagerAdapter.getTaskListFragment();
        });
    }

    /**
     * 刷新任务列表（供 TaskCategoryFragment 调用）
     */
    public void refreshTaskList() {
        if (taskListFragment != null) {
            taskListFragment.refreshAllData();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        loadAvatar();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String taskTitle = intent.getStringExtra("task_title");
        int taskExp = intent.getIntExtra("task_exp", 0);
        String dueDate = intent.getStringExtra("task_due_date");
        String remark = intent.getStringExtra("task_remark");

        if (taskTitle != null && !taskTitle.isEmpty() && taskExp > 0) {
            viewPager.post(() -> {
                if (pagerAdapter.getTaskListFragment() != null) {
                    pagerAdapter.getTaskListFragment().addNewTask(taskTitle, taskExp, dueDate, remark);
                }
            });

            intent.removeExtra("task_title");
            intent.removeExtra("task_exp");
            intent.removeExtra("task_due_date");
            intent.removeExtra("task_remark");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAvatar();
        // 刷新任务列表
        if (taskListFragment != null) {
            taskListFragment.refreshAllData();
        }
    }
}