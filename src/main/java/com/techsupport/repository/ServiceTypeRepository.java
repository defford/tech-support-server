package com.techsupport.repository;

import com.techsupport.entity.ServiceType;
import com.techsupport.entity.Priority;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ServiceType entity.
 * Provides CRUD operations and custom finder methods for service type management.
 */
@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {

    /**
     * Find service type by name.
     * @param name The service type name
     * @return Optional containing the service type if found
     */
    Optional<ServiceType> findByName(String name);

    /**
     * Find service type by name (case insensitive).
     * @param name The service type name
     * @return Optional containing the service type if found
     */
    Optional<ServiceType> findByNameIgnoreCase(String name);

    /**
     * Find all active service types.
     * @return List of active service types
     */
    List<ServiceType> findByIsActiveTrue();

    /**
     * Find all inactive service types.
     * @return List of inactive service types
     */
    List<ServiceType> findByIsActiveFalse();

    /**
     * Find service types by priority.
     * @param priority The priority level
     * @return List of service types with the specified priority
     */
    List<ServiceType> findByPriority(Priority priority);

    /**
     * Find service types by SLA hours range.
     * @param minHours Minimum SLA hours
     * @param maxHours Maximum SLA hours
     * @return List of service types within the SLA range
     */
    @Query("SELECT st FROM ServiceType st WHERE st.slaHours BETWEEN :minHours AND :maxHours")
    List<ServiceType> findBySlaHoursBetween(@Param("minHours") Integer minHours, 
                                           @Param("maxHours") Integer maxHours);

    /**
     * Find service types with high priority (HIGH or CRITICAL).
     * @return List of high priority service types
     */
    @Query("SELECT st FROM ServiceType st WHERE st.priority IN ('HIGH', 'CRITICAL') AND st.isActive = true")
    List<ServiceType> findHighPriorityServiceTypes();

    /**
     * Find service types ordered by SLA (shortest first).
     * @return List of service types ordered by SLA hours ascending
     */
    @Query("SELECT st FROM ServiceType st WHERE st.isActive = true ORDER BY st.slaHours ASC")
    List<ServiceType> findByOrderBySlaHoursAsc();

    /**
     * Find service types with active tickets.
     * @return List of service types that have active tickets
     */
    @Query("SELECT DISTINCT st FROM ServiceType st " +
           "JOIN st.tickets t WHERE t.status != 'CLOSED'")
    List<ServiceType> findServiceTypesWithActiveTickets();

    /**
     * Find service types with no tickets.
     * @return List of service types that have never been used
     */
    @Query("SELECT st FROM ServiceType st WHERE st.tickets IS EMPTY")
    List<ServiceType> findServiceTypesWithNoTickets();

    /**
     * Find service types created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of service types created in the specified period
     */
    @Query("SELECT st FROM ServiceType st WHERE st.createdAt BETWEEN :startDate AND :endDate")
    List<ServiceType> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * Find most used service types (by ticket count).
     * @param pageable Pagination information (use PageRequest.of(0, limit) to limit results)
     * @return List of service types ordered by usage descending
     */
    @Query("SELECT st FROM ServiceType st " +
           "LEFT JOIN st.tickets t " +
           "GROUP BY st " +
           "ORDER BY COUNT(t) DESC")
    List<ServiceType> findMostUsedServiceTypes(Pageable pageable);

    /**
     * Find service types with technician coverage.
     * @return List of service types that have at least one skilled technician
     */
    @Query("SELECT DISTINCT st FROM ServiceType st " +
           "JOIN st.technicianSkills ts " +
           "WHERE ts.technician.isActive = true")
    List<ServiceType> findServiceTypesWithTechnicianCoverage();

    /**
     * Find service types without technician coverage.
     * @return List of service types that have no skilled technicians
     */
    @Query("SELECT st FROM ServiceType st " +
           "WHERE st.technicianSkills IS EMPTY " +
           "OR NOT EXISTS (SELECT ts FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = st AND ts.technician.isActive = true)")
    List<ServiceType> findServiceTypesWithoutTechnicianCoverage();

    /**
     * Count service types by priority.
     * @param priority The priority level
     * @return Number of service types with the specified priority
     */
    long countByPriority(Priority priority);

    /**
     * Count active service types.
     * @return Number of active service types
     */
    @Query("SELECT COUNT(st) FROM ServiceType st WHERE st.isActive = true")
    long countActiveServiceTypes();

    /**
     * Check if name exists (for validation).
     * @param name The name to check
     * @return true if name exists
     */
    boolean existsByName(String name);

    /**
     * Check if name exists excluding a specific service type (for updates).
     * @param name The name to check
     * @param serviceTypeId The service type ID to exclude
     * @return true if name exists for another service type
     */
    @Query("SELECT COUNT(st) > 0 FROM ServiceType st WHERE st.name = :name AND st.id != :serviceTypeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("serviceTypeId") Long serviceTypeId);
} 