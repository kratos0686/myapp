package com.roomscanner.app.scanning;

import com.roomscanner.app.models.Point3D;
import com.roomscanner.app.models.RoomScan;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages multi-room scanning and stitching of point clouds
 */
public class MultiRoomScanner {
    
    private List<RoomScan> rooms;
    private RoomScan currentRoom;
    
    public MultiRoomScanner() {
        this.rooms = new ArrayList<>();
    }
    
    /**
     * Start scanning a new room
     * @param roomName Name of the room to scan
     */
    public void startNewRoom(String roomName) {
        currentRoom = new RoomScan(roomName);
        rooms.add(currentRoom);
    }
    
    /**
     * Add a point to the current room scan
     * @param point The 3D point to add
     */
    public void addPointToCurrentRoom(Point3D point) {
        if (currentRoom != null) {
            currentRoom.addPoint(point);
        }
    }
    
    /**
     * Finish scanning the current room
     */
    public void finishCurrentRoom() {
        currentRoom = null;
    }
    
    /**
     * Stitch multiple room scans into a single unified scan
     * @return A combined scan of all rooms
     */
    public RoomScan stitchRooms() {
        RoomScan stitched = new RoomScan("Combined Scan");
        
        for (RoomScan room : rooms) {
            // Add all points from each room to the combined scan
            for (Point3D point : room.getPointCloud()) {
                stitched.addPoint(point);
            }
        }
        
        return stitched;
    }
    
    /**
     * Get all scanned rooms
     * @return List of all room scans
     */
    public List<RoomScan> getAllRooms() {
        return rooms;
    }
    
    /**
     * Get the current room being scanned
     * @return Current room scan, or null if none active
     */
    public RoomScan getCurrentRoom() {
        return currentRoom;
    }
    
    /**
     * Get the total number of rooms scanned
     * @return Number of rooms
     */
    public int getRoomCount() {
        return rooms.size();
    }
    
    /**
     * Clear all scanned rooms
     */
    public void clearAllRooms() {
        rooms.clear();
        currentRoom = null;
    }
}
