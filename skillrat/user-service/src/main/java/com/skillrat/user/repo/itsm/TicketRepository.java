package com.skillrat.user.repo.itsm;

import com.skillrat.user.domain.User;
import com.skillrat.user.domain.itsm.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    
    Page<Ticket> findByCreatedBy(User user, Pageable pageable);
    
    Page<Ticket> findByAssignedTo(User user, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:searchTerm IS NULL OR " +
           " LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> searchTickets(
            @Param("status") String status,
            @Param("category") String category,
            @Param("priority") String priority,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
    
    long countByStatus(String status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo = :user AND t.status = 'OPEN'")
    long countOpenTicketsAssignedToUser(@Param("user") User user);
}
