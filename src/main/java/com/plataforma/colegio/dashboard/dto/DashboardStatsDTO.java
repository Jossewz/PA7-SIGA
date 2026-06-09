package com.plataforma.colegio.dashboard.dto;

public class DashboardStatsDTO {
    private int totalStudents;
    private int totalClasses;
    private int totalTeachers;

    public DashboardStatsDTO() {}
    public DashboardStatsDTO(int totalStudents, int totalClasses, int totalTeachers) {
        this.totalStudents = totalStudents;
        this.totalClasses = totalClasses;
        this.totalTeachers = totalTeachers;
    }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public int getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(int totalTeachers) { this.totalTeachers = totalTeachers; }
}
