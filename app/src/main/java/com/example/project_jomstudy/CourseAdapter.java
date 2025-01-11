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

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    Context context;
    ArrayList<CourseClass> list;

    public CourseAdapter(Context context, ArrayList<CourseClass> list){
        this.context = context;
        this.list = list;
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder{
        TextView courseCode, courseName, day, startTime, endTime;
        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);

            courseCode = itemView.findViewById(R.id.courseCode);
            courseName = itemView.findViewById(R.id.courseName);
            day = itemView.findViewById(R.id.day);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);
        }

    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.course, parent, false);
        return new CourseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        CourseClass course = list.get(position);
        Log.d("CourseAdapter", "Binding data: " + course.getCourseCode());
        holder.courseCode.setText(course.getCourseCode());
        holder.courseName.setText(course.getCourseName());
        holder.day.setText(course.getDay());
        holder.startTime.setText(course.getStartTime());
        holder.endTime.setText(course.getEndTime());

        holder.itemView.setOnClickListener(v ->  {
            Intent intent = new Intent(context, CourseDetail.class);
            intent.putExtra("Course", course.getCourseCode());
            intent.putExtra("CourseName", course.getCourseName());
            intent.putExtra("InstructorName", course.getInstructorName());
            intent.putExtra("InstructorEmail", course.getInstructorEmail());
            intent.putExtra("Day", course.getDay());
            intent.putExtra("StartTime", course.getStartTime());
            intent.putExtra("EndTime", course.getEndTime());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}