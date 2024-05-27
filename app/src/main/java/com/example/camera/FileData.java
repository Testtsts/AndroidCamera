package com.example.camera;

public class FileData {
    private int id;
    private String fileName;
    private String fileData;

    public FileData(int id, String fileName, String fileData) {
        this.id = id;
        this.fileName = fileName;
        this.fileData = fileData;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getfileData() {
        return fileData;
    }

    public void setfileData(String fileData) {
        this.fileData = fileData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
