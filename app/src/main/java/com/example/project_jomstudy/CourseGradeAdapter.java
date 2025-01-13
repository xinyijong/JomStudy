package com.example.project_jomstudy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseGradeAdapter extends RecyclerView.Adapter<CourseGradeAdapter.CourseGradeViewHolder> {

    Context context;
    ArrayList<CourseGrade> list;

    // Constructor
    public CourseGradeAdapter(Context context, ArrayList<CourseGrade> list) {
        this.context = context;
        this.list = list;
    }

    // ViewHolder class
    public static class CourseGradeViewHolder extends RecyclerView.ViewHolder {
        TextView courseCode, courseName, averageGrade;

        public CourseGradeViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.courseCode);
            courseName = itemView.findViewById(R.id.courseName);
            averageGrade = itemView.findViewById(R.id.averageGrade);
        }
    }

    @NonNull
    @Override
    public CourseGradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.grade, parent, false);
        return new CourseGradeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseGradeViewHolder holder, int position) {
        CourseGrade courseGrade = list.get(position);
        String formattedGrade = String.format(Locale.US, "%.2f", courseGrade.getAverageGrade());

        holder.courseCode.setText(courseGrade.getCourseCode());
        holder.courseName.setText(courseGrade.getCourseName());
        holder.averageGrade.setText(formattedGrade);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseGradeDetail.class);
            intent.putExtra("courseCode", courseGrade.getCourseCode());
            intent.putExtra("courseName", courseGrade.getCourseName());
            intent.putExtra("averageGrade", courseGrade.getAverageGrade());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

