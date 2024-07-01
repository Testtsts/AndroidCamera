package com.example.camera;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class FileData {
    private int id;
    private String fileName;
    private String fileData;
    private ArrayList<Predictions> predictions;

    public class Predictions{

        private double x1,x2,y1,y2;

        public double getX1() {
            return x1;
        }

        public double getX2() {
            return x2;
        }

        public double getY1() {
            return y1;
        }

        public double getY2() {
            return y2;
        }

        public String getPrediction() {
            return prediction;
        }

        private String prediction;
        public  Predictions(double x,double y,double width,double height,String prediction){
            this.x1 = x - width/2;
            this.x2 = x + width/2;
            this.y1 = y - height/2;
            this.y2 = y + height/2;
            this.prediction = prediction;
        }
    }




    public FileData(int id, String fileName, String fileData) {
        this.id = id;
        this.fileName = fileName;
        this.predictions = new ArrayList<>();
        JSONObject obj;
        try {
            obj = new JSONObject(fileData);

            this.fileData = obj.getString("base64");
            JSONArray predictionsArray = obj.getJSONArray("predictions");
            for (int i = 0; i < predictionsArray.length();i++){
                JSONObject predictionJSON = predictionsArray.getJSONObject(i);
                double x = predictionJSON.getInt("x");
                double y = predictionJSON.getInt("y");
                double width = predictionJSON.getInt("width");
                double height= predictionJSON.getInt("height");
                String classPrediction = predictionJSON.getString("class");
                this.predictions.add(new Predictions(x,y,width,height,classPrediction));
            }


        }
        catch (Throwable t){
            Log.e("cameraApp","error parsing");
        }
    }

    public ArrayList<Predictions> getPredictions() {
        return predictions;
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
