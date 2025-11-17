package com.roomscanner.app.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Represents a scanned room with 3D point cloud data and metadata
 */
public class RoomScan {
    private String id;
    private String name;
    private Date scanDate;
    private List<Point3D> pointCloud;
    private String materialType;
    private double estimatedCost;
    private boolean synced;

    public RoomScan() {
        this.id = UUID.randomUUID().toString();
        this.scanDate = new Date();
        this.pointCloud = new ArrayList<>();
        this.synced = false;
    }

    public RoomScan(String name) {
        this();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public List<Point3D> getPointCloud() {
        return pointCloud;
    }

    public void setPointCloud(List<Point3D> pointCloud) {
        this.pointCloud = pointCloud;
    }

    public void addPoint(Point3D point) {
        this.pointCloud.add(point);
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public int getPointCount() {
        return pointCloud.size();
    }
}
