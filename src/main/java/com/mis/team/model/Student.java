package com.mis.team.model;

public class Student {
    private int id;
    private String studentNo;
    private String password;
    private String name;

    public Student() {
    }

    public Student(int id, String studentNo, String password, String name) {
        this.id = id;
        this.studentNo = studentNo;
        this.password = password;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
