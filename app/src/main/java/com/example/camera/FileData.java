package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FileData {
    private int id;
    private String fileName;
    private Bitmap fileData;

    private Bitmap extractBase64(String JSONString){
        try {
            JSONObject obj = new JSONObject(JSONString);
            String encodedImage = obj.getString("base64");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            imageBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            Canvas canvas = new Canvas(decodedImage);

            Paint bounding_box = new Paint();
            bounding_box.setColor(Color.RED);
            bounding_box.setStyle(Paint.Style.STROKE);
            bounding_box.setStrokeWidth(5);

            Paint class_confidence = new Paint();
            class_confidence.setColor(Color.BLACK); // Set text color (can be any color)
            class_confidence.setTextSize(30f); // Set text size in pixels
            class_confidence.setTextAlign(Paint.Align.LEFT); // Set text alignment (CENTER, LEFT, RIGHT)

            JSONArray predict = obj.getJSONArray("predictions");
            for(int i=0; i<predict.length(); i++){
                JSONObject data = predict.getJSONObject(i);
                double x1 = data.getDouble("x");
                double y1 = data.getDouble("y");
                double width = data.getDouble("width");
                double height = data.getDouble("height");
                String classLabel = data.getString("class");
                double confidence = data.getDouble("confidence");

                double right = x1 + width;
                double bottom = y1 + height;

                canvas.drawRect((float) x1, (float) y1, (float) right, (float) bottom, bounding_box);
                canvas.drawText(classLabel + " " + String.valueOf(confidence), (float) x1, ((float) y1) + 1, class_confidence);
            }
            Bitmap.createScaledBitmap(decodedImage, 500, 500, false);
            return decodedImage;
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

    public Bitmap getfileData() {
        return fileData;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}