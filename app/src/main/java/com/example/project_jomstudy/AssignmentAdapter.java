package com.example.project_jomstudy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    Context context;
    ArrayList<AssignmentClass> list;

    public AssignmentAdapter(Context context, ArrayList<AssignmentClass> list){
        this.context = context;
        this.list = list;
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder{
        TextView assignmentTitle, courseCode, courseName, dueDate, progress;
        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);

            assignmentTitle = itemView.findViewById(R.id.assignmentTitle);
            courseCode = itemView.findViewById(R.id.courseCode);
            courseName = itemView.findViewById(R.id.courseName);
            dueDate = itemView.findViewById(R.id.dueDate);
            progress = itemView.findViewById(R.id.progress);

        }

    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.assignment, parent, false);
        return new AssignmentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        AssignmentClass assignment = list.get(position);
        Log.d("AssignmentAdapter", "Binding data: " + assignment.getAssignmentTitle());
        holder.assignmentTitle.setText(assignment.getAssignmentTitle());
        holder.courseCode.setText(assignment.getCourseCode());
        holder.courseName.setText(assignment.getCourseName());
        holder.dueDate.setText(assignment.getDueDate());
        holder.progress.setText(assignment.getProgress() + "%");

        /*holder.itemView.setOnClickListener(v ->  {
            Intent intent = new Intent(context, CourseDetail.class);
            intent.putExtra("Course", course.getCourseCode());
            intent.putExtra("CourseName", course.getCourseName());
            intent.putExtra("InstructorName", course.getInstructorName());
            intent.putExtra("InstructorEmail", course.getInstructorEmail());
            intent.putExtra("Day", course.getDay());
            intent.putExtra("StartTime", course.getStartTime());
            intent.putExtra("EndTime", course.getEndTime());
            context.startActivity(intent);
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
