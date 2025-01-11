package com.example.project_jomstudy;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AssignmentClass {
    String assignmentTitle;
    String dueDate;
    String courseCode;
    String courseName;
    String description;
    int progress;
    private String assignmentId;

    private String status; // Added status field
    private Double grade;  // Added grade field
    private Double weight;

    public AssignmentClass(){

    }

    public AssignmentClass(String assignmentTitle, String dueDate, String courseCode, String courseName, String description, int progress, String status, Double grade, Double weight) {
        this.assignmentTitle = assignmentTitle;
        this.dueDate = dueDate;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.progress = progress;
        this.status = status;
        this.grade = grade;
        this.weight = weight;

    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    public void setAssignmentTitle(String assignmentTitle) {
        this.assignmentTitle = assignmentTitle;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
