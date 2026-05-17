package com.example.liferpg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskCompleteListener listener;

    public interface OnTaskCompleteListener {
        void onTaskCompleted(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskCompleteListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbComplete;
        private TextView tvTitle;
        private TextView tvDueDate;
        private TextView tvExp;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbComplete = itemView.findViewById(R.id.cb_task_complete);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDueDate = itemView.findViewById(R.id.tv_task_due_date);
            tvExp = itemView.findViewById(R.id.tv_task_exp);
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());
            tvExp.setText("+" + task.getExpReward() + " EXP");

            // 显示时间和备注
            StringBuilder detail = new StringBuilder();
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                detail.append("📅 ").append(task.getDueDate());
            }
            if (task.getRemark() != null && !task.getRemark().isEmpty()) {
                if (detail.length() > 0) detail.append("  ");
                detail.append("📝 ").append(task.getRemark());
            }
            tvDueDate.setText(detail.toString());

            // 先移除监听器，避免重复触发
            cbComplete.setOnCheckedChangeListener(null);
            cbComplete.setChecked(task.isCompleted());

            // 设置透明度
            if (task.isCompleted()) {
                tvTitle.setAlpha(0.5f);
                tvExp.setAlpha(0.5f);
                tvDueDate.setAlpha(0.5f);
                cbComplete.setEnabled(false);  // 已完成的任务不能再勾选
            } else {
                tvTitle.setAlpha(1f);
                tvExp.setAlpha(1f);
                tvDueDate.setAlpha(1f);
                cbComplete.setEnabled(true);
            }

            // 重新设置监听器
            cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                android.util.Log.d("TaskAdapter", "CheckBox 点击: isChecked=" + isChecked + ", 任务=" + task.getTitle() + ", task.isCompleted=" + task.isCompleted());

                if (isChecked && !task.isCompleted()) {
                    android.util.Log.d("TaskAdapter", "条件满足，调用 onTaskCompleted");
                    task.setCompleted(true);
                    listener.onTaskCompleted(task);
                    int position = getAdapterPosition();
                    android.util.Log.d("TaskAdapter", "position=" + position);
                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemChanged(position);
                    }
                } else {
                    android.util.Log.d("TaskAdapter", "条件不满足，跳过");
                }
            });
        }
    }
}