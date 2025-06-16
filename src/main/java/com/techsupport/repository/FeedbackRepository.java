package com.techsupport.repository;

import com.techsupport.entity.Feedback;
import com.techsupport.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Feedback entity.
 * Provides CRUD operations and custom finder methods for feedback management
 * and satisfaction reporting.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Find feedback for a specific ticket.
     * @param ticket The ticket
     * @return Optional containing the feedback if found
     */
    Optional<Feedback> findByTicket(Ticket ticket);

    /**
     * Find feedback by ticket ID.
     * @param ticketId The ticket ID
     * @return Optional containing the feedback if found
     */
    @Query("SELECT f FROM Feedback f WHERE f.ticket.id = :ticketId")
    Optional<Feedback> findByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Find feedback by rating.
     * @param rating The rating (1-5)
     * @return List of feedback with the specified rating
     */
    List<Feedback> findByRating(Integer rating);

    /**
     * Find satisfied feedback (rating >= 4).
     * @return List of satisfied feedback
     */
    @Query("SELECT f FROM Feedback f WHERE f.isSatisfied = true")
    List<Feedback> findSatisfiedFeedback();

    /**
     * Find unsatisfied feedback (rating <= 2).
     * @return List of unsatisfied feedback
     */
    @Query("SELECT f FROM Feedback f WHERE f.isSatisfied = false")
    List<Feedback> findUnsatisfiedFeedback();

    /**
     * Find feedback with comments.
     * @return List of feedback that includes comments
     */
    @Query("SELECT f FROM Feedback f WHERE f.comments IS NOT NULL AND f.comments != ''")
    List<Feedback> findFeedbackWithComments();

    /**
     * Find feedback created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of feedback created in the specified period
     */
    @Query("SELECT f FROM Feedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    List<Feedback> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Find feedback for tickets handled by a specific technician.
     * @param technicianId The technician ID
     * @return List of feedback for tickets handled by the technician
     */
    @Query("SELECT f FROM Feedback f WHERE f.ticket.assignedTechnician.id = :technicianId")
    List<Feedback> findByTechnicianId(@Param("technicianId") Long technicianId);

    /**
     * Find feedback for a specific service type.
     * @param serviceTypeId The service type ID
     * @return List of feedback for the service type
     */
    @Query("SELECT f FROM Feedback f WHERE f.ticket.serviceType.id = :serviceTypeId")
    List<Feedback> findByServiceTypeId(@Param("serviceTypeId") Long serviceTypeId);

    /**
     * Count feedback by rating.
     * @param rating The rating
     * @return Number of feedback entries with the specified rating
     */
    long countByRating(Integer rating);

    /**
     * Count satisfied feedback.
     * @return Number of satisfied feedback entries
     */
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.isSatisfied = true")
    long countSatisfiedFeedback();

    /**
     * Count unsatisfied feedback.
     * @return Number of unsatisfied feedback entries
     */
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.isSatisfied = false")
    long countUnsatisfiedFeedback();

    /**
     * Get average rating overall.
     * @return Average rating across all feedback
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();

    /**
     * Get average rating for a specific technician.
     * @param technicianId The technician ID
     * @return Average rating for the technician
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.ticket.assignedTechnician.id = :technicianId")
    Double getAverageRatingForTechnician(@Param("technicianId") Long technicianId);

    /**
     * Get average rating for a specific service type.
     * @param serviceTypeId The service type ID
     * @return Average rating for the service type
     */
    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.ticket.serviceType.id = :serviceTypeId")
    Double getAverageRatingForServiceType(@Param("serviceTypeId") Long serviceTypeId);

    /**
     * Get feedback statistics by rating.
     * @return List of objects containing rating and count
     */
    @Query("SELECT f.rating, COUNT(f) FROM Feedback f GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> getFeedbackCountByRating();

    /**
     * Get satisfaction percentage.
     * @return Percentage of satisfied feedback
     */
    @Query("SELECT (COUNT(CASE WHEN f.isSatisfied = true THEN 1 END) * 100.0 / COUNT(f)) " +
           "FROM Feedback f")
    Double getSatisfactionPercentage();

    /**
     * Find recent negative feedback (rating <= 2, last 30 days).
     * @param thirtyDaysAgo Date 30 days ago
     * @return List of recent negative feedback
     */
    @Query("SELECT f FROM Feedback f WHERE f.rating <= 2 " +
           "AND f.createdAt >= :thirtyDaysAgo " +
           "ORDER BY f.createdAt DESC")
    List<Feedback> findRecentNegativeFeedback(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    /**
     * Find feedback for tickets with specific priority.
     * @param priority The priority
     * @return List of feedback for tickets with the specified priority
     */
    @Query("SELECT f FROM Feedback f WHERE f.ticket.priority = :priority")
    List<Feedback> findByTicketPriority(@Param("priority") Priority priority);

    /**
     * Get customer satisfaction trends (monthly).
     * @param startDate The start date
     * @return List of monthly satisfaction data
     */
    @Query("SELECT " +
           "FUNCTION('YEAR', f.createdAt), " +
           "FUNCTION('MONTH', f.createdAt), " +
           "AVG(f.rating), " +
           "COUNT(f) " +
           "FROM Feedback f " +
           "WHERE f.createdAt >= :startDate " +
           "GROUP BY FUNCTION('YEAR', f.createdAt), FUNCTION('MONTH', f.createdAt) " +
           "ORDER BY FUNCTION('YEAR', f.createdAt), FUNCTION('MONTH', f.createdAt)")
    List<Object[]> getMonthlySatisfactionTrends(@Param("startDate") LocalDateTime startDate);

    /**
     * Check if feedback exists for a ticket.
     * @param ticketId The ticket ID
     * @return true if feedback exists for the ticket
     */
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.ticket.id = :ticketId")
    boolean existsByTicketId(@Param("ticketId") Long ticketId);
} 