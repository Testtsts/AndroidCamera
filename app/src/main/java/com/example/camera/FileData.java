package com.example.camera;

import android.util.Log;

import org.json.JSONObject;

public class FileData {
    private int id;
    private String fileName;
    private String fileData;

    private String extractBase64(String JSONString){
        try {
            JSONObject obj = new JSONObject(JSONString);
            return obj.getString("base64");
        }
        catch (Throwable t){
            Log.e("err","error parsing");
            return null;
        }

    }
    public FileData(int id, String fileName, String fileData) {
        this.id = id;
        this.fileName = fileName;
        this.fileData = extractBase64(fileData);
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
