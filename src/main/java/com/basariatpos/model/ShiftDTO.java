package com.basariatpos.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Data Transfer Object for Shift information.
 */
public class ShiftDTO {
    private final int shiftId;
    private final int userId; // The user who started/owns this shift
    private final OffsetDateTime startTime;
    // Add more fields as needed, e.g., endTime, cashFloatStart, cashFloatEnd, status, etc.

    public ShiftDTO(int shiftId, int userId, OffsetDateTime startTime) {
        this.shiftId = shiftId;
        this.userId = userId;
        this.startTime = startTime;
    }

    public int getShiftId() {
        return shiftId;
    }

    public int getUserId() {
        return userId;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "ShiftDTO{" +
               "shiftId=" + shiftId +
               ", userId=" + userId +
               ", startTime=" + startTime +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShiftDTO shiftDTO = (ShiftDTO) o;
        return shiftId == shiftDTO.shiftId &&
               userId == shiftDTO.userId &&
               Objects.equals(startTime, shiftDTO.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shiftId, userId, startTime);
    }
}
