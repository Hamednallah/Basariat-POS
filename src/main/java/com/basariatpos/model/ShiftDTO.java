package com.basariatpos.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Shift information.
 */
public class ShiftDTO {
    private int shiftId;
    private int userId;
    private String startedByUsername; // For display purposes
    private OffsetDateTime startTime;
    private OffsetDateTime endTime; // Nullable
    private String status; // e.g., "Active", "Paused", "Ended"
    private BigDecimal openingFloat;
    // Add closingFloat, cashSales, cardSales etc. later

    // Default constructor
    public ShiftDTO() {}

    // Constructor for starting a shift (some fields will be set later or by DB)
    public ShiftDTO(int userId, String startedByUsername, BigDecimal openingFloat) {
        this.userId = userId;
        this.startedByUsername = startedByUsername;
        this.openingFloat = openingFloat;
        // startTime, shiftId, status would be set upon actual start
    }

    // Full constructor
    public ShiftDTO(int shiftId, int userId, String startedByUsername, OffsetDateTime startTime,
                    OffsetDateTime endTime, String status, BigDecimal openingFloat) {
        this.shiftId = shiftId;
        this.userId = userId;
        this.startedByUsername = startedByUsername;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.openingFloat = openingFloat;
    }

    // Getters
    public int getShiftId() { return shiftId; }
    public int getUserId() { return userId; }
    public String getStartedByUsername() { return startedByUsername; }
    public OffsetDateTime getStartTime() { return startTime; }
    public OffsetDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public BigDecimal getOpeningFloat() { return openingFloat; }

    // Setters
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setStartedByUsername(String startedByUsername) { this.startedByUsername = startedByUsername; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }
    public void setStatus(String status) { this.status = status; }
    public void setOpeningFloat(BigDecimal openingFloat) { this.openingFloat = openingFloat; }

    @Override
    public String toString() {
        return "ShiftDTO{" +
               "shiftId=" + shiftId +
               ", userId=" + userId +
               ", startedByUsername='" + startedByUsername + '\'' +
               ", startTime=" + startTime +
               ", endTime=" + endTime +
               ", status='" + status + '\'' +
               ", openingFloat=" + openingFloat +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftDTO shiftDTO = (ShiftDTO) o;
        return shiftId == shiftDTO.shiftId &&
               userId == shiftDTO.userId &&
               Objects.equals(startedByUsername, shiftDTO.startedByUsername) &&
               Objects.equals(startTime, shiftDTO.startTime) &&
               Objects.equals(endTime, shiftDTO.endTime) &&
               Objects.equals(status, shiftDTO.status) &&
               Objects.equals(openingFloat, shiftDTO.openingFloat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shiftId, userId, startedByUsername, startTime, endTime, status, openingFloat);
    }
}
