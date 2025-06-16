package com.techsupport.repository;

import com.techsupport.entity.TechnicianSkill;
import com.techsupport.entity.Technician;
import com.techsupport.entity.ServiceType;
import com.techsupport.entity.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TechnicianSkill entity.
 * Provides CRUD operations and custom finder methods for skill management
 * and technician-service type matching for auto-assignment.
 */
@Repository
public interface TechnicianSkillRepository extends JpaRepository<TechnicianSkill, Long> {

    /**
     * Find skills for a specific technician.
     * @param technician The technician
     * @return List of skills for the technician
     */
    List<TechnicianSkill> findByTechnician(Technician technician);

    /**
     * Find skills for a specific service type.
     * @param serviceType The service type
     * @return List of skills for the service type
     */
    List<TechnicianSkill> findByServiceType(ServiceType serviceType);

    /**
     * Find skill by technician and service type.
     * @param technician The technician
     * @param serviceType The service type
     * @return Optional containing the skill if found
     */
    Optional<TechnicianSkill> findByTechnicianAndServiceType(Technician technician, ServiceType serviceType);

    /**
     * Find skills by skill level.
     * @param skillLevel The skill level
     * @return List of skills with the specified level
     */
    List<TechnicianSkill> findBySkillLevel(SkillLevel skillLevel);

    /**
     * Find primary skills for a technician.
     * @param technician The technician
     * @return List of primary skills for the technician
     */
    List<TechnicianSkill> findByTechnicianAndIsPrimarySkillTrue(Technician technician);

    /**
     * Find expert-level skills.
     * @return List of expert-level skills
     */
    @Query("SELECT ts FROM TechnicianSkill ts WHERE ts.skillLevel = 'EXPERT'")
    List<TechnicianSkill> findExpertSkills();

    /**
     * Find skills for active technicians in a service type.
     * @param serviceType The service type
     * @return List of skills for active technicians
     */
    @Query("SELECT ts FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = :serviceType " +
           "AND ts.technician.isActive = true")
    List<TechnicianSkill> findActiveSkillsForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Find technicians with expertise in a service type (for auto-assignment).
     * @param serviceType The service type
     * @param minSkillLevel Minimum skill level required
     * @return List of qualified technician skills ordered by skill and workload
     */
    @Query("SELECT ts FROM TechnicianSkill ts " +
           "LEFT JOIN ts.technician.assignedTickets at ON (at.status != 'CLOSED') " +
           "WHERE ts.serviceType = :serviceType " +
           "AND ts.technician.isActive = true " +
           "AND ts.skillLevel >= :minSkillLevel " +
           "GROUP BY ts " +
           "ORDER BY ts.skillLevel DESC, ts.isPrimarySkill DESC, COUNT(at) ASC")
    List<TechnicianSkill> findQualifiedTechniciansForAssignment(@Param("serviceType") ServiceType serviceType,
                                                               @Param("minSkillLevel") SkillLevel minSkillLevel);

    /**
     * Find best technician skills for auto-assignment.
     * Returns list ordered by skill level descending, primary skill descending.
     * Service layer should retrieve first element for best technician.
     * @param serviceType The service type
     * @param isActive Whether the technician is active
     * @return List of technician skills ordered by priority for assignment
     */
    List<TechnicianSkill> findByServiceTypeAndTechnicianIsActiveOrderBySkillLevelDescIsPrimarySkillDesc(
            ServiceType serviceType, boolean isActive);

    /**
     * Find skills by years of experience range.
     * @param minYears Minimum years of experience
     * @param maxYears Maximum years of experience
     * @return List of skills within the experience range
     */
    @Query("SELECT ts FROM TechnicianSkill ts " +
           "WHERE ts.yearsExperience BETWEEN :minYears AND :maxYears")
    List<TechnicianSkill> findByYearsExperienceBetween(@Param("minYears") Integer minYears,
                                                      @Param("maxYears") Integer maxYears);

    /**
     * Find skills created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of skills created in the specified period
     */
    @Query("SELECT ts FROM TechnicianSkill ts WHERE ts.createdAt BETWEEN :startDate AND :endDate")
    List<TechnicianSkill> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Count skills for a technician.
     * @param technician The technician
     * @return Number of skills for the technician
     */
    long countByTechnician(Technician technician);

    /**
     * Count technicians with skills for a service type.
     * @param serviceType The service type
     * @return Number of technicians with skills for the service type
     */
    @Query("SELECT COUNT(DISTINCT ts.technician) FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = :serviceType AND ts.technician.isActive = true")
    long countActiveTechniciansForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Count skills by skill level.
     * @param skillLevel The skill level
     * @return Number of skills with the specified level
     */
    long countBySkillLevel(SkillLevel skillLevel);

    /**
     * Find service types a technician can handle.
     * @param technician The technician
     * @return List of service types the technician can handle
     */
    @Query("SELECT ts.serviceType FROM TechnicianSkill ts WHERE ts.technician = :technician")
    List<ServiceType> findServiceTypesForTechnician(@Param("technician") Technician technician);

    /**
     * Find technicians who can handle a service type.
     * @param serviceType The service type
     * @return List of technicians who can handle the service type
     */
    @Query("SELECT ts.technician FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = :serviceType AND ts.technician.isActive = true")
    List<Technician> findTechniciansForServiceType(@Param("serviceType") ServiceType serviceType);

    /**
     * Get skill coverage statistics (technicians per service type).
     * @return List of objects containing service type and technician count
     */
    @Query("SELECT ts.serviceType, COUNT(DISTINCT ts.technician) FROM TechnicianSkill ts " +
           "WHERE ts.technician.isActive = true " +
           "GROUP BY ts.serviceType " +
           "ORDER BY COUNT(DISTINCT ts.technician) DESC")
    List<Object[]> getSkillCoverageStats();

    /**
     * Get skill level distribution.
     * @return List of objects containing skill level and count
     */
    @Query("SELECT ts.skillLevel, COUNT(ts) FROM TechnicianSkill ts " +
           "GROUP BY ts.skillLevel " +
           "ORDER BY ts.skillLevel")
    List<Object[]> getSkillLevelDistribution();

    /**
     * Find uncovered service types (no active technicians).
     * @return List of service types with no active technician coverage
     */
    @Query("SELECT st FROM ServiceType st " +
           "WHERE st.isActive = true " +
           "AND NOT EXISTS (SELECT ts FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = st AND ts.technician.isActive = true)")
    List<ServiceType> findUncoveredServiceTypes();

    /**
     * Find technicians with most skills.
     * @param limit Maximum number of results
     * @return List of technicians ordered by skill count descending
     */
    @Query("SELECT ts.technician FROM TechnicianSkill ts " +
           "WHERE ts.technician.isActive = true " +
           "GROUP BY ts.technician " +
           "ORDER BY COUNT(ts) DESC")
    List<Technician> findTechniciansWithMostSkills(@Param("limit") int limit);

    /**
     * Check if technician has skill for service type.
     * @param technicianId The technician ID
     * @param serviceTypeId The service type ID
     * @return true if technician has the skill
     */
    @Query("SELECT COUNT(ts) > 0 FROM TechnicianSkill ts " +
           "WHERE ts.technician.id = :technicianId AND ts.serviceType.id = :serviceTypeId")
    boolean existsByTechnicianIdAndServiceTypeId(@Param("technicianId") Long technicianId,
                                                @Param("serviceTypeId") Long serviceTypeId);

    /**
     * Get average years of experience for a service type.
     * @param serviceType The service type
     * @return Average years of experience
     */
    @Query("SELECT AVG(ts.yearsExperience) FROM TechnicianSkill ts " +
           "WHERE ts.serviceType = :serviceType " +
           "AND ts.yearsExperience IS NOT NULL " +
           "AND ts.technician.isActive = true")
    Double getAverageExperienceForServiceType(@Param("serviceType") ServiceType serviceType);
} 