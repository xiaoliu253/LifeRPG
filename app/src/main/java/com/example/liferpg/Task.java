package com.example.liferpg;

public class Task {
    private int id;
    private String title;
    private int expReward;
    private boolean isCompleted;
    private String dueDate;
    private String remark;
    private String completedDate;

    // 完整构造函数
    public Task(int id, String title, int expReward, boolean isCompleted,
                String dueDate, String remark) {
        this.id = id;
        this.title = title;
        this.expReward = expReward;
        this.isCompleted = isCompleted;
        this.dueDate = dueDate != null ? dueDate : "";
        this.remark = remark != null ? remark : "";
        this.completedDate = "";
    }

    // 将任务转换为字符串（用于保存）
    public String toStorageString() {
        return id + "|" + title + "|" + expReward + "|" + isCompleted + "|" + dueDate + "|" + remark;
    }

    // 从字符串恢复任务
    public static Task fromStorageString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 6) {
            int id = Integer.parseInt(parts[0]);
            String title = parts[1];
            int expReward = Integer.parseInt(parts[2]);
            boolean isCompleted = Boolean.parseBoolean(parts[3]);
            String dueDate = parts[4];
            String remark = parts[5];
            return new Task(id, title, expReward, isCompleted, dueDate, remark);
        }
        return null;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getExpReward() { return expReward; }
    public boolean isCompleted() { return isCompleted; }
    public String getDueDate() { return dueDate; }
    public String getRemark() { return remark; }
    public String getCompletedDate() { return completedDate; }

    // Setters
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }
}