package com.techsupport.service;

import com.techsupport.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for managing tickets.
 * This is a placeholder for Week 1 smoke testing.
 * Will be fully implemented in Week 3.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Simple method to verify the service is working.
     * Returns the count of tickets in the repository.
     */
    public long getTicketCount() {
        return ticketRepository.count();
    }

    /**
     * Simple method for testing - returns true if repository is available.
     */
    public boolean isRepositoryAvailable() {
        return ticketRepository != null;
    }
} 