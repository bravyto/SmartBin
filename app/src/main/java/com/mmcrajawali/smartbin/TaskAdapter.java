package com.mmcrajawali.smartbin;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Bambang on 6/25/2016.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private List<Task> taskList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView location_name;

        public MyViewHolder(View view) {
            super(view);
            location_name = (TextView) view.findViewById(R.id.location_name);
        }
    }

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.location_name.setText(task.getLocationName());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
