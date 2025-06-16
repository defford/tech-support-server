package com.techsupport.repository;

import com.techsupport.entity.TicketHistory;
import com.techsupport.entity.HistoryActionType;
import com.techsupport.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for TicketHistory entity.
 * Provides CRUD operations and custom finder methods for ticket history
 * tracking and audit reporting.
 */
@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    /**
     * Find history entries for a specific ticket.
     * @param ticket The ticket
     * @return List of history entries for the ticket ordered by creation time
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket = :ticket ORDER BY th.createdAt ASC")
    List<TicketHistory> findByTicketOrderByCreatedAtAsc(Ticket ticket);

    /**
     * Find history entries for a ticket by ID.
     * @param ticketId The ticket ID
     * @return List of history entries for the ticket ordered by creation time
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket.id = :ticketId ORDER BY th.createdAt ASC")
    List<TicketHistory> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);

    /**
     * Find history entries by action type.
     * @param actionType The action type
     * @return List of history entries with the specified action type
     */
    List<TicketHistory> findByActionType(HistoryActionType actionType);

    /**
     * Find history entries by who made the change.
     * @param changedBy The person who made the change
     * @return List of history entries made by the specified person
     */
    List<TicketHistory> findByChangedBy(String changedBy);

    /**
     * Find history entries created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of history entries created in the specified period
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.createdAt BETWEEN :startDate AND :endDate ORDER BY th.createdAt DESC")
    List<TicketHistory> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent history entries for a ticket.
     * @param ticket The ticket
     * @param limit Maximum number of entries
     * @return List of recent history entries
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket = :ticket " +
           "ORDER BY th.createdAt DESC LIMIT :limit")
    List<TicketHistory> findRecentHistoryForTicket(@Param("ticket") Ticket ticket,
                                                  @Param("limit") int limit);

    /**
     * Find status change history for a ticket.
     * @param ticket The ticket
     * @return List of status change entries
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket = :ticket " +
           "AND th.actionType = 'STATUS_CHANGED' " +
           "ORDER BY th.createdAt ASC")
    List<TicketHistory> findStatusChangesForTicket(@Param("ticket") Ticket ticket);

    /**
     * Find assignment history for a ticket.
     * @param ticket The ticket
     * @return List of assignment-related entries
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.ticket = :ticket " +
           "AND th.actionType IN ('ASSIGNED', 'UNASSIGNED') " +
           "ORDER BY th.createdAt ASC")
    List<TicketHistory> findAssignmentHistoryForTicket(@Param("ticket") Ticket ticket);

    /**
     * Find history entries involving a specific technician.
     * @param technicianName The technician's name
     * @return List of history entries involving the technician
     */
    @Query("SELECT th FROM TicketHistory th WHERE " +
           "th.newValue LIKE CONCAT('%', :technicianName, '%') OR " +
           "th.oldValue LIKE CONCAT('%', :technicianName, '%') OR " +
           "th.changedBy = :technicianName")
    List<TicketHistory> findHistoryInvolvingTechnician(@Param("technicianName") String technicianName);

    /**
     * Find most active users (by history entry count).
     * @param limit Maximum number of results
     * @return List of objects containing user name and activity count
     */
    @Query("SELECT th.changedBy, COUNT(th) FROM TicketHistory th " +
           "WHERE th.changedBy IS NOT NULL " +
           "GROUP BY th.changedBy " +
           "ORDER BY COUNT(th) DESC")
    List<Object[]> findMostActiveUsers(@Param("limit") int limit);

    /**
     * Find history by action type within date range.
     * @param actionType The action type
     * @param startDate The start date
     * @param endDate The end date
     * @return List of history entries matching the criteria
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.actionType = :actionType " +
           "AND th.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY th.createdAt DESC")
    List<TicketHistory> findByActionTypeAndDateRange(@Param("actionType") HistoryActionType actionType,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Count history entries for a ticket.
     * @param ticket The ticket
     * @return Number of history entries for the ticket
     */
    long countByTicket(Ticket ticket);

    /**
     * Count history entries by action type.
     * @param actionType The action type
     * @return Number of history entries with the specified action type
     */
    long countByActionType(HistoryActionType actionType);

    /**
     * Get activity statistics by action type.
     * @return List of objects containing action type and count
     */
    @Query("SELECT th.actionType, COUNT(th) FROM TicketHistory th " +
           "GROUP BY th.actionType " +
           "ORDER BY COUNT(th) DESC")
    List<Object[]> getActivityStatsByActionType();

    /**
     * Get daily activity trends.
     * @param startDate The start date
     * @return List of daily activity data
     */
    @Query("SELECT DATE(th.createdAt), COUNT(th) FROM TicketHistory th " +
           "WHERE th.createdAt >= :startDate " +
           "GROUP BY DATE(th.createdAt) " +
           "ORDER BY DATE(th.createdAt)")
    List<Object[]> getDailyActivityTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * Find tickets with most activity (by history count).
     * @param limit Maximum number of results
     * @return List of tickets ordered by activity level
     */
    @Query("SELECT th.ticket FROM TicketHistory th " +
           "GROUP BY th.ticket " +
           "ORDER BY COUNT(th) DESC")
    List<Ticket> findMostActiveTickets(@Param("limit") int limit);

    /**
     * Find system-generated history entries.
     * @return List of system-generated history entries
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.actionType IN " +
           "('CREATED', 'RESOLVED', 'CLOSED', 'APPOINTMENT_SCHEDULED', 'APPOINTMENT_COMPLETED') " +
           "ORDER BY th.createdAt DESC")
    List<TicketHistory> findSystemGeneratedEntries();

    /**
     * Find user-initiated history entries.
     * @return List of user-initiated history entries
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.actionType IN " +
           "('COMMENTED', 'UPDATED', 'PRIORITY_CHANGED', 'ASSIGNED', 'UNASSIGNED') " +
           "ORDER BY th.createdAt DESC")
    List<TicketHistory> findUserInitiatedEntries();

    /**
     * Find history entries with value changes.
     * @return List of history entries that include old and new values
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.oldValue IS NOT NULL AND th.newValue IS NOT NULL")
    List<TicketHistory> findEntriesWithValueChanges();

    /**
     * Get average resolution time from history.
     * @return Average time between creation and resolution
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, created.createdAt, resolved.createdAt)) " +
           "FROM TicketHistory created JOIN TicketHistory resolved ON created.ticket = resolved.ticket " +
           "WHERE created.actionType = 'CREATED' AND resolved.actionType = 'RESOLVED'")
    Double getAverageResolutionTimeHours();

    /**
     * Find tickets that were reopened.
     * @return List of tickets that have reopening history
     */
    @Query("SELECT DISTINCT th.ticket FROM TicketHistory th WHERE th.actionType = 'REOPENED'")
    List<Ticket> findReopenedTickets();

    /**
     * Find latest history entry for each ticket.
     * @return List of latest history entries per ticket
     */
    @Query("SELECT th FROM TicketHistory th WHERE th.id IN " +
           "(SELECT MAX(th2.id) FROM TicketHistory th2 GROUP BY th2.ticket)")
    List<TicketHistory> findLatestHistoryPerTicket();
} 