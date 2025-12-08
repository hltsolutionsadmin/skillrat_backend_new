package com.skillrat.user.web.dto.itsm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.skillrat.user.domain.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TicketResponse {
    private UUID id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private String assetId;
    private String location;
    private String createdBy;
    private String assignedTo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        ON_HOLD,
        RESOLVED,
        CLOSED,
        CANCELLED
    }
    
    // Static factory method to create response from entity
    public static TicketResponse fromEntity(com.skillrat.user.domain.itsm.Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setCategory(TicketCategory.valueOf(ticket.getCategory()));
        response.setPriority(TicketPriority.valueOf(ticket.getPriority()));
        response.setStatus(TicketStatus.valueOf(ticket.getStatus()));
        response.setAssetId(ticket.getAssetId());
        response.setLocation(ticket.getLocation());
        
        if (ticket.getCreatedBy() != null) {
            response.setCreatedBy(ticket.getCreatedBy().getEmail());
        }
        
        if (ticket.getAssignedTo() != null) {
            response.setAssignedTo(ticket.getAssignedTo().getEmail());
        }
        
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        
        return response;
    }
}
