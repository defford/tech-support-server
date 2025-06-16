package com.techsupport.service;

import com.techsupport.entity.*;
import com.techsupport.repository.TicketRepository;
import com.techsupport.repository.ClientRepository;
import com.techsupport.repository.ServiceTypeRepository;
import com.techsupport.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing tickets and ticket-related operations.
 * Implements business logic for CRUD operations, assignments, and SLA calculations.
 * Week 2 implementation with comprehensive functionality.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ClientRepository clientRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final TechnicianRepository technicianRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                        ClientRepository clientRepository,
                        ServiceTypeRepository serviceTypeRepository,
                        TechnicianRepository technicianRepository) {
        this.ticketRepository = ticketRepository;
        this.clientRepository = clientRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.technicianRepository = technicianRepository;
    }

    // ============================================================================
    // ORIGINAL SMOKE TEST METHODS (from Week 1) - PRESERVED
    // ============================================================================

    /**
     * Check if the ticket repository is available (for smoke testing).
     * @return true if repository is not null
     */
    public boolean isRepositoryAvailable() {
        return ticketRepository != null;
    }

    /**
     * Get total count of tickets (for smoke testing).
     * @return total number of tickets
     */
    public long getTicketCount() {
        return ticketRepository.count();
    }

    // ============================================================================
    // WEEK 2 CRUD OPERATIONS
    // ============================================================================

    /**
     * Create a new ticket with automatic SLA calculation.
     * @param title The ticket title
     * @param description The ticket description
     * @param clientId The client ID
     * @param serviceTypeId The service type ID
     * @param priority The ticket priority
     * @return The created ticket
     * @throws RuntimeException if client or service type not found
     */
    public Ticket createTicket(String title, String description, Long clientId, Long serviceTypeId, Priority priority) {
        // Validate client exists
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found with ID: " + clientId));

        // Validate service type exists
        ServiceType serviceType = serviceTypeRepository.findById(serviceTypeId)
                .orElseThrow(() -> new RuntimeException("Service type not found with ID: " + serviceTypeId));

        // Create new ticket
        Ticket ticket = new Ticket(title, description, client, serviceType, priority);
        
        return ticketRepository.save(ticket);
    }

    /**
     * Find ticket by ID.
     * @param id The ticket ID
     * @return Optional containing the ticket if found
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Find all tickets.
     * @return List of all tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    /**
     * Update an existing ticket.
     * @param ticket The ticket to update
     * @return The updated ticket
     */
    public Ticket update(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Delete ticket by ID.
     * @param id The ticket ID to delete
     */
    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    // ============================================================================
    // BUSINESS LOGIC METHODS
    // ============================================================================

    /**
     * Assign a ticket to a technician.
     * @param ticketId The ticket ID
     * @param technicianId The technician ID
     * @return The updated ticket
     * @throws RuntimeException if ticket or technician not found
     */
    public Ticket assignTicket(Long ticketId, Long technicianId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId));

        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        ticket.assignTo(technician);
        return ticketRepository.save(ticket);
    }

    /**
     * Close a ticket.
     * @param ticketId The ticket ID
     * @return The closed ticket
     * @throws RuntimeException if ticket not found or cannot be closed
     */
    public Ticket closeTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId));

        if (!ticket.canBeClosed()) {
            throw new RuntimeException("Ticket cannot be closed in current status: " + ticket.getStatus());
        }

        ticket.close();
        return ticketRepository.save(ticket);
    }

    /**
     * Bulk close multiple tickets.
     * @param ticketIds List of ticket IDs to close
     * @return List of closed tickets
     */
    public List<Ticket> bulkCloseTickets(List<Long> ticketIds) {
        List<Ticket> ticketsToClose = ticketRepository.findTicketsForBulkClose(ticketIds);
        
        ticketsToClose.forEach(Ticket::close);
        
        return ticketRepository.saveAll(ticketsToClose);
    }

    /**
     * Bulk assign tickets to a technician.
     * @param ticketIds List of ticket IDs to assign
     * @param technicianId The technician ID
     * @return List of assigned tickets
     * @throws RuntimeException if technician not found
     */
    public List<Ticket> bulkAssignTickets(List<Long> ticketIds, Long technicianId) {
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with ID: " + technicianId));

        List<Ticket> ticketsToAssign = ticketRepository.findTicketsForBulkAssign(ticketIds);
        
        ticketsToAssign.forEach(ticket -> ticket.assignTo(technician));
        
        return ticketRepository.saveAll(ticketsToAssign);
    }

    // ============================================================================
    // QUERY METHODS
    // ============================================================================

    /**
     * Find tickets by status.
     * @param status The ticket status
     * @return List of tickets with the specified status
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status);
    }

    /**
     * Find tickets by client.
     * @param client The client
     * @return List of tickets for the client
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByClient(Client client) {
        return ticketRepository.findByClient(client);
    }

    /**
     * Find tickets by technician.
     * @param technician The technician
     * @return List of tickets assigned to the technician
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByTechnician(Technician technician) {
        return ticketRepository.findByAssignedTechnician(technician);
    }

    /**
     * Find tickets by service type.
     * @param serviceType The service type
     * @return List of tickets for the service type
     */
    @Transactional(readOnly = true)
    public List<Ticket> findByServiceType(ServiceType serviceType) {
        return ticketRepository.findByServiceType(serviceType);
    }

    /**
     * Find overdue tickets.
     * @return List of overdue tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> findOverdueTickets() {
        return ticketRepository.findOverdueTickets(LocalDateTime.now());
    }

    /**
     * Find open tickets.
     * @return List of open (not closed) tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> findOpenTickets() {
        return ticketRepository.findOpenTickets();
    }

    /**
     * Find unassigned tickets.
     * @return List of tickets with no assigned technician
     */
    @Transactional(readOnly = true)
    public List<Ticket> findUnassignedTickets() {
        return ticketRepository.findUnassignedTickets();
    }

    /**
     * Find high priority tickets.
     * @return List of high and critical priority tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> findHighPriorityTickets() {
        return ticketRepository.findHighPriorityTickets();
    }

    /**
     * Find tickets due today.
     * @return List of tickets due today
     */
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsDueToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        return ticketRepository.findTicketsDueToday(startOfDay, endOfDay);
    }

    /**
     * Search tickets by title or description.
     * @param searchTerm The search term
     * @return List of matching tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> searchTickets(String searchTerm) {
        return ticketRepository.findByTitleOrDescriptionContaining(searchTerm);
    }

    // ============================================================================
    // REPORTING AND METRICS
    // ============================================================================

    /**
     * Calculate SLA compliance percentage.
     * @return SLA compliance percentage (0-100)
     */
    @Transactional(readOnly = true)
    public double calculateSlaCompliance() {
        Object[] stats = ticketRepository.getSlaComplianceStats(LocalDateTime.now());
        
        if (stats == null) {
            return 0.0;
        }
        
        Long onTime = (Long) stats[0];
        Long late = (Long) stats[1];
        Long overdue = (Long) stats[2];
        
        long total = onTime + late + overdue;
        if (total == 0) {
            return 0.0;
        }
        
        return (onTime.doubleValue() / total) * 100.0;
    }

    /**
     * Get ticket count by status.
     * @return List of status and count pairs
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTicketCountByStatus() {
        return ticketRepository.getTicketCountByStatus();
    }

    /**
     * Get ticket count by priority.
     * @return List of priority and count pairs
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTicketCountByPriority() {
        return ticketRepository.getTicketCountByPriority();
    }

    /**
     * Count tickets by status.
     * @param status The status to count
     * @return Number of tickets with the specified status
     */
    @Transactional(readOnly = true)
    public long countByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    /**
     * Count tickets by priority.
     * @param priority The priority to count
     * @return Number of tickets with the specified priority
     */
    @Transactional(readOnly = true)
    public long countByPriority(Priority priority) {
        return ticketRepository.countByPriority(priority);
    }

    /**
     * Find tickets needing immediate attention.
     * @return List of tickets that are overdue or high priority
     */
    @Transactional(readOnly = true)
    public List<Ticket> findTicketsNeedingAttention() {
        return ticketRepository.findTicketsNeedingAttention(LocalDateTime.now());
    }
} 