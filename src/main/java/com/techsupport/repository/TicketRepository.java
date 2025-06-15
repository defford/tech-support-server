package com.techsupport.repository;

import com.techsupport.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Ticket entities.
 * This is a placeholder for Week 1 smoke testing.
 * Will be fully implemented in Week 2.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // For now, just inherit basic CRUD operations from JpaRepository
    // Custom finder methods will be added in Week 2
} 