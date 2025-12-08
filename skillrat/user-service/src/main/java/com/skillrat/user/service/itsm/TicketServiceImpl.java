package com.skillrat.user.service.itsm;

import com.skillrat.user.domain.User;
import com.skillrat.user.domain.itsm.Ticket;
import com.skillrat.user.domain.itsm.TicketComment;
import com.skillrat.user.repo.itsm.TicketRepository;
import com.skillrat.user.service.UserService;
import com.skillrat.user.web.dto.itsm.CreateTicketRequest;
import com.skillrat.user.web.dto.itsm.TicketResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, User createdBy) {
        log.info("Creating new ticket with title: {}", request.getTitle());
        
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory().name());
        ticket.setPriority(request.getPriority().name());
        ticket.setStatus(TicketResponse.TicketStatus.OPEN.name());
        ticket.setAssetId(request.getAssetId());
        ticket.setLocation(request.getLocation());
        ticket.setCreatedBy(createdBy);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Created ticket with ID: {}", savedTicket.getId());
        
        return TicketResponse.fromEntity(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(UUID ticketId, User currentUser) {
        log.debug("Fetching ticket with ID: {}", ticketId);
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));
        
        // Check if the user has permission to view this ticket
        if (!canViewTicket(ticket, currentUser)) {
            throw new AccessDeniedException("You don't have permission to view this ticket");
        }
        
        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, String status, User currentUser) {
        log.info("Updating status of ticket ID: {} to {}", ticketId, status);
        
        Ticket ticket = getTicketOrThrow(ticketId, currentUser);
        
        // Validate status transition
        if (!isValidStatusTransition(ticket.getStatus(), status)) {
            throw new IllegalArgumentException("Invalid status transition from " + ticket.getStatus() + " to " + status);
        }
        
        ticket.setStatus(status);
        
        // Update timestamps based on status
        if (status.equals("RESOLVED")) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (status.equals("CLOSED")) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // Add a system comment about the status change
        addSystemComment(ticket, "Status changed to " + status, currentUser);
        
        return TicketResponse.fromEntity(updatedTicket);
    }

    @Override
    @Transactional
    public TicketResponse assignTicket(UUID ticketId, UUID assigneeId, User currentUser) {
        log.info("Assigning ticket ID: {} to user ID: {}", ticketId, assigneeId);
        
        Ticket ticket = getTicketOrThrow(ticketId, currentUser);
        User assignee = userService.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + assigneeId));
        
        // Check if the current user has permission to assign tickets
        if (!isAdminOrSupport(currentUser)) {
            throw new AccessDeniedException("You don't have permission to assign tickets");
        }
        
        User previousAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(assignee);
        
        // Add a system comment about the assignment
        String comment = String.format("Ticket assigned to %s", assignee.getEmail());
        if (previousAssignee != null) {
            comment = String.format("Ticket reassigned from %s to %s", 
                    previousAssignee.getEmail(), assignee.getEmail());
        }
        addSystemComment(ticket, comment, currentUser);
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // TODO: Send notification to the assignee
        
        return TicketResponse.fromEntity(updatedTicket);
    }

    @Override
    @Transactional
    public TicketResponse addComment(UUID ticketId, String comment, boolean isInternal, User currentUser) {
        log.debug("Adding {} comment to ticket ID: {}", isInternal ? "internal" : "public", ticketId);
        
        Ticket ticket = getTicketOrThrow(ticketId, currentUser);
        
        // Check if the user has permission to add internal comments
        if (isInternal && !isAdminOrSupport(currentUser)) {
            throw new AccessDeniedException("You don't have permission to add internal comments");
        }
        
        TicketComment ticketComment = new TicketComment();
        ticketComment.setContent(comment);
        ticketComment.setCreatedBy(currentUser);
        ticketComment.setInternal(isInternal);
        
        ticket.addComment(ticketComment);
        
        // If the ticket is in OPEN status and a support agent is adding a comment,
        // update the status to IN_PROGRESS
        if (isAdminOrSupport(currentUser) && "OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("IN_PROGRESS");
        }
        
        Ticket updatedTicket = ticketRepository.save(ticket);
        
        // TODO: Send notifications to relevant users
        
        return TicketResponse.fromEntity(updatedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchTickets(
            String status,
            String category,
            String priority,
            String searchTerm,
            User currentUser,
            Pageable pageable) {
        
        log.debug("Searching tickets with filters - status: {}, category: {}, priority: {}, search: {}", 
                status, category, priority, searchTerm);
        
        // If the user is not an admin or support, they can only see their own tickets
        if (!isAdminOrSupport(currentUser)) {
            return ticketRepository.findByCreatedBy(currentUser, pageable)
                    .map(TicketResponse::fromEntity);
        }
        
        // Admins and support can see all tickets with filters
        return ticketRepository.searchTickets(status, category, priority, searchTerm, pageable)
                .map(TicketResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByUser(User user, Pageable pageable) {
        log.debug("Fetching tickets created by user ID: {}", user.getId());
        return ticketRepository.findByCreatedBy(user, pageable)
                .map(TicketResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAssignedTickets(User assignee, Pageable pageable) {
        log.debug("Fetching tickets assigned to user ID: {}", assignee.getId());
        return ticketRepository.findByAssignedTo(assignee, pageable)
                .map(TicketResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketStatistics getTicketStatistics() {
        log.debug("Fetching ticket statistics");
        
        return new TicketStatistics(
                ticketRepository.count(),
                ticketRepository.countByStatus("OPEN"),
                ticketRepository.countByStatus("IN_PROGRESS"),
                ticketRepository.countByStatus("RESOLVED"),
                ticketRepository.countByStatus("CLOSED")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserTicketStatistics getUserTicketStatistics(User user) {
        log.debug("Fetching ticket statistics for user ID: {}", user.getId());
        
        long assignedTickets = ticketRepository.countByAssignedTo(user);
        long openTickets = ticketRepository.countByAssignedToAndStatus(user, "OPEN");
        long inProgressTickets = ticketRepository.countByAssignedToAndStatus(user, "IN_PROGRESS");
        long resolvedTickets = ticketRepository.countByAssignedToAndStatus(user, "RESOLVED");
        
        return new UserTicketStatistics(
                assignedTickets,
                openTickets,
                inProgressTickets,
                resolvedTickets
        );
    }
    
    // Helper methods
    
    private Ticket getTicketOrThrow(UUID ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));
        
        // Check if the user has permission to access this ticket
        if (!canViewTicket(ticket, currentUser)) {
            throw new AccessDeniedException("You don't have permission to access this ticket");
        }
        
        return ticket;
    }
    
    private boolean canViewTicket(Ticket ticket, User user) {
        // Admins and support can view all tickets
        if (isAdminOrSupport(user)) {
            return true;
        }
        
        // Users can view their own tickets or tickets assigned to them
        return ticket.getCreatedBy().getId().equals(user.getId()) || 
               (ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId()));
    }
    
    private boolean isAdminOrSupport(User user) {
        // Check if the user has admin or support role
        // This is a simplified check - you might want to implement proper role-based access control
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || 
                                role.getName().equals("ROLE_SUPPORT"));
    }
    
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Define valid status transitions
        // This is a simplified implementation - you might want to make this more sophisticated
        if (currentStatus.equals(newStatus)) {
            return true;
        }
        
        switch (currentStatus) {
            case "OPEN":
                return "IN_PROGRESS".equals(newStatus) || "RESOLVED".equals(newStatus) || 
                       "CLOSED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "IN_PROGRESS":
                return "RESOLVED".equals(newStatus) || "ON_HOLD".equals(newStatus) || 
                       "CLOSED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "ON_HOLD":
                return "IN_PROGRESS".equals(newStatus) || "RESOLVED".equals(newStatus) || 
                       "CLOSED".equals(newStatus) || "CANCELLED".equals(newStatus);
            case "RESOLVED":
                return "CLOSED".equals(newStatus) || "REOPENED".equals(newStatus);
            case "CLOSED":
            case "CANCELLED":
                return "REOPENED".equals(newStatus);
            case "REOPENED":
                return "IN_PROGRESS".equals(newStatus) || "RESOLVED".equals(newStatus) || 
                       "CLOSED".equals(newStatus) || "CANCELLED".equals(newStatus);
            default:
                return false;
        }
    }
    
    private void addSystemComment(Ticket ticket, String comment, User user) {
        TicketComment ticketComment = new TicketComment();
        ticketComment.setContent("[System] " + comment);
        ticketComment.setCreatedBy(user);
        ticketComment.setInternal(true);
        ticket.addComment(ticketComment);
    }
}
