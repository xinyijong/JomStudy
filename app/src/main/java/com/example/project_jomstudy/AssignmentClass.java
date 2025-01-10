package com.example.project_jomstudy;

public class AssignmentClass {
    String assignmentTitle;
    String dueDate;
    String courseCode;
    String courseName;
    String description;
    int progress;
    private String assignmentId;

    public AssignmentClass(){

    }

    public AssignmentClass(String assignmentTitle, String dueDate, String courseCode, String courseName, String description, int progress) {
        this.assignmentTitle = assignmentTitle;
        this.dueDate = dueDate;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.progress = progress;

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
}
