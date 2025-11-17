package com.roomscanner.app.models;

/**
 * Represents a 3D point in space with coordinates and optional color data
 */
public class Point3D {
    private float x;
    private float y;
    private float z;
    private int color; // ARGB color

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = 0xFFFFFFFF; // Default white
    }

    public Point3D(float x, float y, float z, int color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
