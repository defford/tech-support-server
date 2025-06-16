package com.techsupport.repository;

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
 * Repository interface for Technician entity.
 * Provides CRUD operations and custom finder methods for technician management
 * and auto-assignment logic.
 */
@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    /**
     * Find technician by email address.
     * @param email The technician's email
     * @return Optional containing the technician if found
     */
    Optional<Technician> findByEmail(String email);

    /**
     * Find all active technicians.
     * @return List of active technicians
     */
    List<Technician> findByIsActiveTrue();

    /**
     * Find all inactive technicians.
     * @return List of inactive technicians
     */
    List<Technician> findByIsActiveFalse();

    /**
     * Find technicians by name (first or last name containing the search term).
     * @param name The search term
     * @return List of matching technicians
     */
    @Query("SELECT t FROM Technician t WHERE " +
           "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Technician> findByNameContaining(@Param("name") String name);

    /**
     * Find technicians who can handle a specific service type.
     * @param serviceType The service type
     * @return List of qualified technicians
     */
    @Query("SELECT DISTINCT t FROM Technician t " +
           "JOIN t.skills ts " +
           "WHERE ts.serviceType = :serviceType AND t.isActive = true")
    List<Technician> findByServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Find available technicians for a service type (least loaded first).
     * This is crucial for auto-assignment logic.
     * @param serviceType The service type
     * @return List of technicians ordered by current workload (ascending)
     */
    @Query("SELECT t FROM Technician t " +
           "JOIN t.skills ts " +
           "LEFT JOIN t.assignedTickets at " +
           "WHERE ts.serviceType = :serviceType " +
           "AND t.isActive = true " +
           "AND (at.status IS NULL OR at.status != 'CLOSED') " +
           "GROUP BY t " +
           "ORDER BY COUNT(at) ASC, MAX(ts.skillLevel) DESC")
    List<Technician> findAvailableForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Find technicians with expertise in a service type (expert level only).
     * @param serviceType The service type
     * @return List of expert technicians
     */
    @Query("SELECT DISTINCT t FROM Technician t " +
           "JOIN t.skills ts " +
           "WHERE ts.serviceType = :serviceType " +
           "AND ts.skillLevel = 'EXPERT' " +
           "AND t.isActive = true")
    List<Technician> findExpertsForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Find technicians with open tickets count.
     * @return List of technicians with their open ticket counts
     */
    @Query("SELECT t, COUNT(at) as openTickets FROM Technician t " +
           "LEFT JOIN t.assignedTickets at " +
           "WHERE at.status IS NULL OR at.status != 'CLOSED' " +
           "GROUP BY t " +
           "ORDER BY COUNT(at) ASC")
    List<Object[]> findTechniciansWithOpenTicketCount();

    /**
     * Find overloaded technicians (more than specified ticket count).
     * @param maxTickets The maximum acceptable ticket count
     * @return List of overloaded technicians
     */
    @Query("SELECT t FROM Technician t " +
           "LEFT JOIN t.assignedTickets at " +
           "WHERE at.status != 'CLOSED' " +
           "GROUP BY t " +
           "HAVING COUNT(at) > :maxTickets")
    List<Technician> findOverloadedTechnicians(@Param("maxTickets") long maxTickets);

    /**
     * Find technicians created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of technicians created in the specified period
     */
    @Query("SELECT t FROM Technician t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Technician> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find technicians with primary skills in a service type.
     * @param serviceType The service type
     * @return List of technicians with primary skills
     */
    @Query("SELECT DISTINCT t FROM Technician t " +
           "JOIN t.skills ts " +
           "WHERE ts.serviceType = :serviceType " +
           "AND ts.isPrimarySkill = true " +
           "AND t.isActive = true")
    List<Technician> findWithPrimarySkillForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Count active technicians.
     * @return Number of active technicians
     */
    @Query("SELECT COUNT(t) FROM Technician t WHERE t.isActive = true")
    long countActiveTechnicians();

    /**
     * Check if email exists (for validation).
     * @param email The email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists excluding a specific technician (for updates).
     * @param email The email to check
     * @param technicianId The technician ID to exclude
     * @return true if email exists for another technician
     */
    @Query("SELECT COUNT(t) > 0 FROM Technician t WHERE t.email = :email AND t.id != :technicianId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("technicianId") Long technicianId);

    /**
     * Find best technician for auto-assignment (least loaded with highest skill).
     * This is the key method for the auto-assignment feature in Week 5.
     * @param serviceType The service type
     * @return Optional containing the best technician for assignment
     */
    @Query("SELECT t FROM Technician t " +
           "JOIN t.skills ts " +
           "LEFT JOIN t.assignedTickets at ON (at.status != 'CLOSED') " +
           "WHERE ts.serviceType = :serviceType " +
           "AND t.isActive = true " +
           "GROUP BY t, ts.skillLevel " +
           "ORDER BY COUNT(at) ASC, ts.skillLevel DESC, ts.isPrimarySkill DESC")
    Optional<Technician> findFirstBestForAutoAssignment(@Param("serviceType") ServiceType serviceType);
} 