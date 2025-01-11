package com.example.project_jomstudy;

public class CourseGrade {
    private String courseCode;
    private String courseName;
    private double averageGrade;

    public CourseGrade(){

    }

    public CourseGrade(String courseCode, String courseName, double averageGrade) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.averageGrade = averageGrade;
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

    public double getAverageGrade() {
        return averageGrade;
    }

    public void setAverageGrade(double averageGrade) {
        this.averageGrade = averageGrade;
    }
}
