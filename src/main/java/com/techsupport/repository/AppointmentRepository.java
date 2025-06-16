package com.techsupport.repository;

import com.techsupport.entity.Appointment;
import com.techsupport.entity.AppointmentStatus;
import com.techsupport.entity.Ticket;
import com.techsupport.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Appointment entity.
 * Provides CRUD operations and custom finder methods for appointment management
 * and scheduling conflict detection.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find appointments by status.
     * @param status The appointment status
     * @return List of appointments with the specified status
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Find appointments for a specific ticket.
     * @param ticket The ticket
     * @return List of appointments for the ticket
     */
    List<Appointment> findByTicket(Ticket ticket);

    /**
     * Find appointments for a specific technician.
     * @param technician The technician
     * @return List of appointments for the technician
     */
    List<Appointment> findByTechnician(Technician technician);

    /**
     * Find appointments scheduled within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of appointments in the specified period
     */
    @Query("SELECT a FROM Appointment a WHERE a.scheduledAt BETWEEN :startDate AND :endDate")
    List<Appointment> findByScheduledAtBetween(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Find appointments for a technician within a date range.
     * @param technician The technician
     * @param startDate The start date
     * @param endDate The end date
     * @return List of appointments for the technician in the specified period
     */
    @Query("SELECT a FROM Appointment a WHERE a.technician = :technician " +
           "AND a.scheduledAt BETWEEN :startDate AND :endDate")
    List<Appointment> findByTechnicianAndScheduledAtBetween(@Param("technician") Technician technician,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Find overlapping appointments for a technician (for conflict detection).
     * @param technicianId The technician ID
     * @param startTime The proposed start time
     * @param endTime The proposed end time
     * @return List of overlapping appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.technician.id = :technicianId " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "AND a.scheduledAt < :endTime " +
           "AND FUNCTION('DATEADD', 'MINUTE', a.durationMinutes, a.scheduledAt) > :startTime")
    List<Appointment> findOverlappingAppointments(@Param("technicianId") Long technicianId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * Find overlapping appointments for a technician excluding a specific appointment.
     * @param technicianId The technician ID
     * @param startTime The proposed start time
     * @param endTime The proposed end time
     * @param excludeAppointmentId The appointment ID to exclude
     * @return List of overlapping appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.technician.id = :technicianId " +
           "AND a.id != :excludeAppointmentId " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "AND a.scheduledAt < :endTime " +
           "AND FUNCTION('DATEADD', 'MINUTE', a.durationMinutes, a.scheduledAt) > :startTime")
    List<Appointment> findOverlappingAppointmentsExcluding(@Param("technicianId") Long technicianId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("excludeAppointmentId") Long excludeAppointmentId);

    /**
     * Find upcoming appointments for a technician.
     * @param technician The technician
     * @param now Current date and time
     * @return List of upcoming appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.technician = :technician " +
           "AND a.scheduledAt > :now " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledAt ASC")
    List<Appointment> findUpcomingAppointments(@Param("technician") Technician technician,
                                             @Param("now") LocalDateTime now);

    /**
     * Find appointments scheduled for today.
     * @param startOfDay Start of today
     * @param endOfDay End of today
     * @return List of today's appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.scheduledAt BETWEEN :startOfDay AND :endOfDay " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledAt ASC")
    List<Appointment> findAppointmentsToday(@Param("startOfDay") LocalDateTime startOfDay,
                                           @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find appointments for a technician today.
     * @param technician The technician
     * @param startOfDay Start of today
     * @param endOfDay End of today
     * @return List of today's appointments for the technician
     */
    @Query("SELECT a FROM Appointment a WHERE a.technician = :technician " +
           "AND a.scheduledAt BETWEEN :startOfDay AND :endOfDay " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "ORDER BY a.scheduledAt ASC")
    List<Appointment> findTechnicianAppointmentsToday(@Param("technician") Technician technician,
                                                     @Param("startOfDay") LocalDateTime startOfDay,
                                                     @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Find overdue appointments (past scheduled time but not completed).
     * @param now Current date and time
     * @return List of overdue appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.scheduledAt < :now " +
           "AND a.status = 'SCHEDULED' " +
           "ORDER BY a.scheduledAt ASC")
    List<Appointment> findOverdueAppointments(@Param("now") LocalDateTime now);

    /**
     * Find active appointments (scheduled or in progress).
     * @return List of active appointments
     */
    @Query("SELECT a FROM Appointment a WHERE a.status IN ('SCHEDULED', 'IN_PROGRESS')")
    List<Appointment> findActiveAppointments();

    /**
     * Find completed appointments within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of completed appointments in the specified period
     */
    @Query("SELECT a FROM Appointment a WHERE a.status = 'COMPLETED' " +
           "AND a.completedAt BETWEEN :startDate AND :endDate")
    List<Appointment> findCompletedAppointmentsBetween(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Count appointments by status.
     * @param status The appointment status
     * @return Number of appointments with the specified status
     */
    long countByStatus(AppointmentStatus status);

    /**
     * Count appointments for a technician.
     * @param technician The technician
     * @return Number of appointments for the technician
     */
    long countByTechnician(Technician technician);

    /**
     * Find technician workload (appointments count) for a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of objects containing technician and appointment count
     */
    @Query("SELECT a.technician, COUNT(a) FROM Appointment a " +
           "WHERE a.scheduledAt BETWEEN :startDate AND :endDate " +
           "GROUP BY a.technician " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> getTechnicianWorkload(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Count conflicting appointments for a technician at a specific time slot.
     * @param technicianId The technician ID
     * @param startTime The start time
     * @param endTime The end time
     * @return count of conflicting appointments
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.technician.id = :technicianId " +
           "AND a.status IN ('SCHEDULED', 'IN_PROGRESS') " +
           "AND a.scheduledAt < :endTime " +
           "AND FUNCTION('DATEADD', 'MINUTE', a.durationMinutes, a.scheduledAt) > :startTime")
    long countConflictingAppointments(@Param("technicianId") Long technicianId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
} 