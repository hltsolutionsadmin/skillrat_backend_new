package com.skillrat.user.service.itsm;

import com.skillrat.user.domain.User;
import com.skillrat.user.web.dto.itsm.CreateTicketRequest;
import com.skillrat.user.web.dto.itsm.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TicketService {
    
    /**
     * Create a new support ticket
     */
    TicketResponse createTicket(CreateTicketRequest request, User createdBy);
    
    /**
     * Get a ticket by ID
     */
    TicketResponse getTicketById(UUID ticketId, User currentUser);
    
    /**
     * Update ticket status
     */
    TicketResponse updateTicketStatus(UUID ticketId, String status, User currentUser);
    
    /**
     * Assign ticket to a support agent
     */
    TicketResponse assignTicket(UUID ticketId, UUID assigneeId, User currentUser);
    
    /**
     * Add a comment to a ticket
     */
    TicketResponse addComment(UUID ticketId, String comment, boolean isInternal, User currentUser);
    
    /**
     * Search tickets with filters
     */
    Page<TicketResponse> searchTickets(
            String status,
            String category,
            String priority,
            String searchTerm,
            User currentUser,
            Pageable pageable
    );
    
    /**
     * Get tickets created by a specific user
     */
    Page<TicketResponse> getTicketsByUser(User user, Pageable pageable);
    
    /**
     * Get tickets assigned to a specific user
     */
    Page<TicketResponse> getAssignedTickets(User assignee, Pageable pageable);
    
    /**
     * Get ticket statistics
     */
    TicketStatistics getTicketStatistics();
    
    /**
     * Get ticket statistics for a specific user
     */
    UserTicketStatistics getUserTicketStatistics(User user);
    
    record TicketStatistics(
        long totalTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets,
        long closedTickets
    ) {}
    
    record UserTicketStatistics(
        long assignedTickets,
        long openTickets,
        long inProgressTickets,
        long resolvedTickets
    ) {}
}
