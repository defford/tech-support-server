package com.techsupport.repository;

import com.techsupport.entity.Ticket;
import com.techsupport.entity.TicketStatus;
import com.techsupport.entity.Priority;
import com.techsupport.entity.Client;
import com.techsupport.entity.Technician;
import com.techsupport.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ticket entity.
 * Provides CRUD operations and comprehensive custom finder methods for ticket management,
 * filtering, reporting, and bulk operations.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Status-based queries
    /**
     * Find tickets by status.
     * @param status The ticket status
     * @return List of tickets with the specified status
     */
    List<Ticket> findByStatus(TicketStatus status);

    /**
     * Find all open tickets (not closed).
     * @return List of open tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.status != 'CLOSED'")
    List<Ticket> findOpenTickets();

    /**
     * Find all closed tickets.
     * @return List of closed tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'CLOSED'")
    List<Ticket> findClosedTickets();

    // Assignment-based queries
    /**
     * Find tickets assigned to a specific technician.
     * @param technician The assigned technician
     * @return List of tickets assigned to the technician
     */
    List<Ticket> findByAssignedTechnician(Technician technician);

    /**
     * Find unassigned tickets.
     * @return List of tickets with no assigned technician
     */
    @Query("SELECT t FROM Ticket t WHERE t.assignedTechnician IS NULL")
    List<Ticket> findUnassignedTickets();

    /**
     * Find tickets assigned to a technician by ID.
     * @param technicianId The technician ID
     * @return List of assigned tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.assignedTechnician.id = :technicianId")
    List<Ticket> findByAssignedTechnicianId(@Param("technicianId") Long technicianId);

    // Client-based queries
    /**
     * Find tickets for a specific client.
     * @param client The client
     * @return List of tickets for the client
     */
    List<Ticket> findByClient(Client client);

    /**
     * Find tickets for a client by ID.
     * @param clientId The client ID
     * @return List of tickets for the client
     */
    @Query("SELECT t FROM Ticket t WHERE t.client.id = :clientId")
    List<Ticket> findByClientId(@Param("clientId") Long clientId);

    // Service type queries
    /**
     * Find tickets for a specific service type.
     * @param serviceType The service type
     * @return List of tickets for the service type
     */
    List<Ticket> findByServiceType(ServiceType serviceType);

    /**
     * Find tickets for a service type by ID.
     * @param serviceTypeId The service type ID
     * @return List of tickets for the service type
     */
    @Query("SELECT t FROM Ticket t WHERE t.serviceType.id = :serviceTypeId")
    List<Ticket> findByServiceTypeId(@Param("serviceTypeId") Long serviceTypeId);

    // Priority-based queries
    /**
     * Find tickets by priority.
     * @param priority The priority level
     * @return List of tickets with the specified priority
     */
    List<Ticket> findByPriority(Priority priority);

    /**
     * Find high priority tickets (HIGH or CRITICAL).
     * @return List of high priority tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.priority IN ('HIGH', 'CRITICAL')")
    List<Ticket> findHighPriorityTickets();

    // Date-based queries
    /**
     * Find tickets created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of tickets created in the specified period
     */
    @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find tickets due within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of tickets due in the specified period
     */
    @Query("SELECT t FROM Ticket t WHERE t.dueAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByDueAtBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find overdue tickets.
     * @return List of tickets that are past their due date
     */
    @Query("SELECT t FROM Ticket t WHERE t.dueAt < :now AND t.status != 'CLOSED'")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);

    /**
     * Find tickets due today.
     * @param startOfDay Start of today
     * @param endOfDay End of today
     * @return List of tickets due today
     */
    @Query("SELECT t FROM Ticket t WHERE t.dueAt BETWEEN :startOfDay AND :endOfDay AND t.status != 'CLOSED'")
    List<Ticket> findTicketsDueToday(@Param("startOfDay") LocalDateTime startOfDay,
                                    @Param("endOfDay") LocalDateTime endOfDay);

    // Complex filtering queries
    /**
     * Find tickets by multiple criteria.
     * @param status The status (optional)
     * @param priority The priority (optional)
     * @param technicianId The technician ID (optional)
     * @param clientId The client ID (optional)
     * @param serviceTypeId The service type ID (optional)
     * @return List of matching tickets
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:technicianId IS NULL OR t.assignedTechnician.id = :technicianId) AND " +
           "(:clientId IS NULL OR t.client.id = :clientId) AND " +
           "(:serviceTypeId IS NULL OR t.serviceType.id = :serviceTypeId)")
    List<Ticket> findByMultipleCriteria(@Param("status") TicketStatus status,
                                       @Param("priority") Priority priority,
                                       @Param("technicianId") Long technicianId,
                                       @Param("clientId") Long clientId,
                                       @Param("serviceTypeId") Long serviceTypeId);

    // Search queries
    /**
     * Find tickets by title containing search term.
     * @param searchTerm The search term
     * @return List of matching tickets
     */
    @Query("SELECT t FROM Ticket t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> findByTitleContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find tickets by title or description containing search term.
     * @param searchTerm The search term
     * @return List of matching tickets
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ticket> findByTitleOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    // Bulk operation support queries
    /**
     * Find tickets by multiple IDs (for bulk operations).
     * @param ids List of ticket IDs
     * @return List of tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.id IN :ids")
    List<Ticket> findByIdIn(@Param("ids") List<Long> ids);

    /**
     * Find tickets that can be bulk closed.
     * @param ids List of ticket IDs
     * @return List of tickets that can be closed
     */
    @Query("SELECT t FROM Ticket t WHERE t.id IN :ids AND t.status IN ('RESOLVED', 'PENDING_CLIENT')")
    List<Ticket> findTicketsForBulkClose(@Param("ids") List<Long> ids);

    /**
     * Find tickets that can be bulk assigned.
     * @param ids List of ticket IDs
     * @return List of tickets that can be assigned
     */
    @Query("SELECT t FROM Ticket t WHERE t.id IN :ids AND t.status = 'OPEN'")
    List<Ticket> findTicketsForBulkAssign(@Param("ids") List<Long> ids);

    // Reporting queries
    /**
     * Count tickets by status.
     * @param status The status
     * @return Number of tickets with the specified status
     */
    long countByStatus(TicketStatus status);

    /**
     * Count tickets by priority.
     * @param priority The priority
     * @return Number of tickets with the specified priority
     */
    long countByPriority(Priority priority);

    /**
     * Get ticket statistics by status.
     * @return List of objects containing status and count
     */
    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> getTicketCountByStatus();

    /**
     * Get ticket statistics by priority.
     * @return List of objects containing priority and count
     */
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> getTicketCountByPriority();

    /**
     * Get SLA compliance statistics.
     * @return List of objects containing compliance data
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN t.status = 'CLOSED' AND t.closedAt <= t.dueAt THEN 1 END) as onTime, " +
           "COUNT(CASE WHEN t.status = 'CLOSED' AND t.closedAt > t.dueAt THEN 1 END) as late, " +
           "COUNT(CASE WHEN t.status != 'CLOSED' AND :now > t.dueAt THEN 1 END) as overdue " +
           "FROM Ticket t")
    Object[] getSlaComplianceStats(@Param("now") LocalDateTime now);

    /**
     * Find tickets needing attention (overdue or high priority open tickets).
     * @return List of tickets needing immediate attention
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "(t.dueAt < :now AND t.status != 'CLOSED') OR " +
           "(t.priority IN ('HIGH', 'CRITICAL') AND t.status IN ('OPEN', 'ASSIGNED'))")
    List<Ticket> findTicketsNeedingAttention(@Param("now") LocalDateTime now);
} 