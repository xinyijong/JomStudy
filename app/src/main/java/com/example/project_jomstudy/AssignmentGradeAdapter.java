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

public class AssignmentGradeAdapter extends RecyclerView.Adapter<AssignmentGradeAdapter.AssignmentGradeViewHolder> {

    Context context;
    ArrayList<AssignmentClass> list;

    public AssignmentGradeAdapter(Context context, ArrayList<AssignmentClass> list){
        this.context = context;
        this.list = list;
    }

    public static class AssignmentGradeViewHolder extends RecyclerView.ViewHolder{
        TextView assignmentTitle, weight, grade;
        public AssignmentGradeViewHolder(@NonNull View itemView) {
            super(itemView);

            assignmentTitle = itemView.findViewById(R.id.assignmentTitle);
            weight = itemView.findViewById(R.id.weight);
            grade = itemView.findViewById(R.id.grade);

        }

    }

    @NonNull
    @Override
    public AssignmentGradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.assignmentgrade, parent, false);
        return new AssignmentGradeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentGradeViewHolder holder, int position) {
        AssignmentClass assignment = list.get(position);
        String assignmentId = assignment.getAssignmentId();  // Firebase key

        Log.d("AssignmentGradeAdapter", "Binding data: " + assignment.getAssignmentTitle());
        holder.assignmentTitle.setText(assignment.getAssignmentTitle());
        holder.grade.setText(String.format("%.2f", assignment.getGrade()));
        holder.weight.setText(String.format("%.2f", assignment.getWeight()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditGrade.class);
            intent.putExtra("assignmentTitle", assignment.getAssignmentTitle());
            intent.putExtra("courseName", assignment.getCourseName());
            intent.putExtra("weight", assignment.getWeight());
            intent.putExtra("grade", assignment.getGrade());
            intent.putExtra("assignmentId", assignmentId);  // Pass the assignmentId (Firebase key)
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}