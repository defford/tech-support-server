package com.techsupport.repository;

import com.techsupport.entity.Client;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Client entity.
 * Provides CRUD operations and custom finder methods for client management.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Find client by email address.
     * @param email The client's email
     * @return Optional containing the client if found
     */
    Optional<Client> findByEmail(String email);

    /**
     * Find all active clients.
     * @return List of active clients
     */
    List<Client> findByIsActiveTrue();

    /**
     * Find all inactive clients.
     * @return List of inactive clients
     */
    List<Client> findByIsActiveFalse();

    /**
     * Find clients by company name.
     * @param company The company name
     * @return List of clients from the specified company
     */
    List<Client> findByCompanyIgnoreCase(String company);

    /**
     * Find clients by name (first or last name containing the search term).
     * @param name The search term
     * @return List of matching clients
     */
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Client> findByNameContaining(@Param("name") String name);

    /**
     * Find clients created within a date range.
     * @param startDate The start date
     * @param endDate The end date
     * @return List of clients created in the specified period
     */
    @Query("SELECT c FROM Client c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Client> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find clients with open tickets.
     * @return List of clients who have open tickets
     */
    @Query("SELECT DISTINCT c FROM Client c " +
           "JOIN c.tickets t WHERE t.status != 'CLOSED'")
    List<Client> findClientsWithOpenTickets();

    /**
     * Find clients with no tickets.
     * @return List of clients who have never submitted a ticket
     */
    @Query("SELECT c FROM Client c WHERE c.tickets IS EMPTY")
    List<Client> findClientsWithNoTickets();

    /**
     * Count clients by company.
     * @param company The company name
     * @return Number of clients from the specified company
     */
    @Query("SELECT COUNT(c) FROM Client c WHERE LOWER(c.company) = LOWER(:company)")
    long countByCompany(@Param("company") String company);

    /**
     * Find clients with most tickets (top N).
     * @param pageable Pagination information (use PageRequest.of(0, limit) to limit results)
     * @return List of clients ordered by ticket count descending
     */
    @Query("SELECT c FROM Client c " +
           "LEFT JOIN c.tickets t " +
           "GROUP BY c " +
           "ORDER BY COUNT(t) DESC")
    List<Client> findTopClientsByTicketCount(Pageable pageable);

    /**
     * Check if email exists (for validation).
     * @param email The email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if email exists excluding a specific client (for updates).
     * @param email The email to check
     * @param clientId The client ID to exclude
     * @return true if email exists for another client
     */
    @Query("SELECT COUNT(c) > 0 FROM Client c WHERE c.email = :email AND c.id != :clientId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("clientId") Long clientId);
} 