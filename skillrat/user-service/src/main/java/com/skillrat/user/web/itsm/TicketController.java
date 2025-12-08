package com.skillrat.user.web.itsm;

import com.skillrat.user.domain.User;
import com.skillrat.user.service.itsm.TicketService;
import com.skillrat.user.web.dto.itsm.CreateTicketRequest;
import com.skillrat.user.web.dto.itsm.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/itsm/tickets")
@RequiredArgsConstructor
@Tag(name = "ITSM Ticket Management", description = "APIs for managing support tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Create a new support ticket")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal User currentUser) {
        log.info("Received request to create a new ticket from user: {}", currentUser.getEmail());
        TicketResponse response = ticketService.createTicket(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal User currentUser) {
        log.debug("Fetching ticket with ID: {}", ticketId);
        TicketResponse response = ticketService.getTicketById(ticketId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/status/{status}")
    @Operation(summary = "Update ticket status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable UUID ticketId,
            @PathVariable String status,
            @AuthenticationPrincipal User currentUser) {
        log.info("Updating status of ticket ID: {} to {}", ticketId, status);
        TicketResponse response = ticketService.updateTicketStatus(ticketId, status, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{ticketId}/assign/{assigneeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPPORT')")
    @Operation(summary = "Assign ticket to a support agent (Admin/Support only)")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID ticketId,
            @PathVariable UUID assigneeId,
            @AuthenticationPrincipal User currentUser) {
        log.info("Assigning ticket ID: {} to user ID: {}", ticketId, assigneeId);
        TicketResponse response = ticketService.assignTicket(ticketId, assigneeId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ticketId}/comments")
    @Operation(summary = "Add a comment to a ticket")
    public ResponseEntity<TicketResponse> addComment(
            @PathVariable UUID ticketId,
            @RequestParam String comment,
            @RequestParam(required = false, defaultValue = "false") boolean internal,
            @AuthenticationPrincipal User currentUser) {
        log.debug("Adding comment to ticket ID: {}", ticketId);
        TicketResponse response = ticketService.addComment(ticketId, comment, internal, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search tickets with filters")
    public ResponseEntity<Page<TicketResponse>> searchTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        log.debug("Searching tickets with filters - status: {}, category: {}, priority: {}, search: {}", 
                status, category, priority, search);
        Page<TicketResponse> response = ticketService.searchTickets(
                status, category, priority, search, currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tickets")
    @Operation(summary = "Get tickets created by the current user")
    public ResponseEntity<Page<TicketResponse>> getMyTickets(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        log.debug("Fetching tickets created by user: {}", currentUser.getEmail());
        Page<TicketResponse> response = ticketService.getTicketsByUser(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assigned-to-me")
    @Operation(summary = "Get tickets assigned to the current user")
    public ResponseEntity<Page<TicketResponse>> getAssignedTickets(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        log.debug("Fetching tickets assigned to user: {}", currentUser.getEmail());
        Page<TicketResponse> response = ticketService.getAssignedTickets(currentUser, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPPORT')")
    @Operation(summary = "Get ticket statistics (Admin/Support only)")
    public ResponseEntity<TicketService.TicketStatistics> getStatistics() {
        log.debug("Fetching ticket statistics");
        TicketService.TicketStatistics statistics = ticketService.getTicketStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/my-statistics")
    @Operation(summary = "Get ticket statistics for the current user")
    public ResponseEntity<TicketService.UserTicketStatistics> getMyStatistics(
            @AuthenticationPrincipal User currentUser) {
        log.debug("Fetching ticket statistics for user: {}", currentUser.getEmail());
        TicketService.UserTicketStatistics statistics = ticketService.getUserTicketStatistics(currentUser);
        return ResponseEntity.ok(statistics);
    }
}
