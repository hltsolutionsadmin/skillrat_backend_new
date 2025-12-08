package com.skillrat.user.repo.itsm;

import com.skillrat.user.domain.itsm.Ticket;
import com.skillrat.user.domain.itsm.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {
    
    /**
     * Find all comments for a specific ticket
     * @param ticket The ticket to find comments for
     * @param pageable Pagination information
     * @return Page of ticket comments
     */
    Page<TicketComment> findByTicket(Ticket ticket, Pageable pageable);
    
    /**
     * Find all non-internal comments for a specific ticket
     * @param ticket The ticket to find comments for
     * @param internal Whether to include internal comments
     * @param pageable Pagination information
     * @return Page of ticket comments
     */
    Page<TicketComment> findByTicketAndInternal(Ticket ticket, boolean internal, Pageable pageable);
    
    /**
     * Count the number of comments for a specific ticket
     * @param ticket The ticket to count comments for
     * @return The number of comments
     */
    long countByTicket(Ticket ticket);
    
    /**
     * Count the number of internal comments for a specific ticket
     * @param ticket The ticket to count internal comments for
     * @param internal Whether to count internal (true) or public (false) comments
     * @return The number of internal comments
     */
    long countByTicketAndInternal(Ticket ticket, boolean internal);
}
