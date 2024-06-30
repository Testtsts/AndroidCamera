package com.example.camera;

public class ResponseData {
    private float left, right, bottom, top;
    private double confidence;

    private String classLabel;

    public ResponseData(float left, float right, float bottom, float top, double confidence, String classLabel) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.confidence = confidence;
        this.classLabel = classLabel;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public float getTop() {
        return top;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getClassLabel() {
        return classLabel;
    }
}
