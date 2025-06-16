package com.techsupport.service;

import com.techsupport.entity.*;
import com.techsupport.repository.TicketRepository;
import com.techsupport.repository.ClientRepository;
import com.techsupport.repository.ServiceTypeRepository;
import com.techsupport.repository.TechnicianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for TicketService.
 * Tests CRUD operations, business logic, and SLA calculations.
 * Expanded for Week 2 requirements.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    
    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private ServiceTypeRepository serviceTypeRepository;
    
    @Mock
    private TechnicianRepository technicianRepository;

    @InjectMocks
    private TicketService ticketService;

    private Client testClient;
    private ServiceType testServiceType;
    private Technician testTechnician;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Set up test data
        testClient = new Client("John", "Doe", "john.doe@test.com");
        testClient.setId(1L);

        testServiceType = new ServiceType("Hardware Issue", 24, Priority.HIGH);
        testServiceType.setId(1L);

        testTechnician = new Technician("Jane", "Tech", "jane.tech@company.com");
        testTechnician.setId(1L);

        testTicket = new Ticket("Test Issue", "Test description", testClient, testServiceType, Priority.HIGH);
        testTicket.setId(1L);
    }

    // ============================================================================
    // ORIGINAL SMOKE TESTS (from Week 1) - PRESERVED
    // ============================================================================

    @Test
    void testRepositoryAvailable_ShouldReturnTrue() {
        // Given: TicketService is initialized with mocked repository
        
        // When: We check if repository is available
        boolean result = ticketService.isRepositoryAvailable();
        
        // Then: Repository should be available (not null)
        assertTrue(result, "Repository should be available");
    }

    @Test 
    void testGetTicketCount_ShouldReturnMockedValue() {
        // Given: Repository returns a specific count
        long expectedCount = 5L;
        when(ticketRepository.count()).thenReturn(expectedCount);
        
        // When: We get the ticket count
        long actualCount = ticketService.getTicketCount();
        
        // Then: The service should return the mocked count
        assertEquals(expectedCount, actualCount, "Should return mocked ticket count");
        
        // And: The repository count method should have been called
        verify(ticketRepository, times(1)).count();
    }

    @Test
    void testGetTicketCount_WithZeroTickets() {
        // Given: Repository has no tickets
        when(ticketRepository.count()).thenReturn(0L);
        
        // When: We get the ticket count
        long actualCount = ticketService.getTicketCount();
        
        // Then: Count should be zero
        assertEquals(0L, actualCount, "Should return zero when no tickets exist");
        verify(ticketRepository).count();
    }

    @Test
    void testService_IsNotNull() {
        // Given: Service is injected
        
        // When & Then: Service should not be null
        assertNotNull(ticketService, "TicketService should be instantiated");
    }

    // ============================================================================
    // EXPANDED WEEK 2 TESTS - CRUD OPERATIONS
    // ============================================================================

    @Test
    void createTicket_ShouldCalculateDueDateAndSaveSuccessfully() {
        // Given: Valid client and service type exist
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(serviceTypeRepository.findById(1L)).thenReturn(Optional.of(testServiceType));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When: Creating a new ticket
        Ticket result = ticketService.createTicket("Test Issue", "Description", 1L, 1L, Priority.HIGH);

        // Then: Ticket should be created with proper due date
        assertNotNull(result, "Created ticket should not be null");
        assertEquals("Test Issue", result.getTitle(), "Title should match");
        assertEquals(TicketStatus.OPEN, result.getStatus(), "New ticket should be OPEN");
        assertEquals(Priority.HIGH, result.getPriority(), "Priority should match");
        assertNotNull(result.getDueAt(), "Due date should be calculated");
        
        // And: Repository methods should be called
        verify(clientRepository).findById(1L);
        verify(serviceTypeRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithInvalidClient_ShouldThrowException() {
        // Given: Client does not exist
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw exception
        assertThrows(RuntimeException.class, () -> 
            ticketService.createTicket("Test", "Description", 999L, 1L, Priority.MEDIUM),
            "Should throw exception for invalid client");
        
        // And: Should not attempt to save
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createTicket_WithInvalidServiceType_ShouldThrowException() {
        // Given: Service type does not exist
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(serviceTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then: Should throw exception
        assertThrows(RuntimeException.class, () -> 
            ticketService.createTicket("Test", "Description", 1L, 999L, Priority.MEDIUM),
            "Should throw exception for invalid service type");
    }

    @Test
    void findTicketById_WhenExists_ShouldReturnTicket() {
        // Given: Ticket exists
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When: Finding ticket by ID
        Optional<Ticket> result = ticketService.findById(1L);

        // Then: Should return the ticket
        assertTrue(result.isPresent(), "Ticket should be found");
        assertEquals(testTicket.getId(), result.get().getId(), "Should return correct ticket");
        verify(ticketRepository).findById(1L);
    }

    @Test
    void findTicketById_WhenNotExists_ShouldReturnEmpty() {
        // Given: Ticket does not exist
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        // When: Finding non-existent ticket
        Optional<Ticket> result = ticketService.findById(999L);

        // Then: Should return empty
        assertFalse(result.isPresent(), "Should return empty for non-existent ticket");
        verify(ticketRepository).findById(999L);
    }

    @Test
    void findAllTickets_ShouldReturnAllTickets() {
        // Given: Multiple tickets exist
        Ticket ticket2 = new Ticket("Second ticket", testClient, testServiceType);
        ticket2.setId(2L);
        List<Ticket> tickets = List.of(testTicket, ticket2);
        when(ticketRepository.findAll()).thenReturn(tickets);

        // When: Finding all tickets
        List<Ticket> result = ticketService.findAll();

        // Then: Should return all tickets
        assertEquals(2, result.size(), "Should return all tickets");
        assertEquals(tickets, result, "Should return the same list");
        verify(ticketRepository).findAll();
    }

    @Test
    void updateTicket_ShouldSaveAndReturnUpdatedTicket() {
        // Given: Existing ticket
        when(ticketRepository.save(testTicket)).thenReturn(testTicket);

        // When: Updating ticket
        testTicket.setDescription("Updated description");
        testTicket.setPriority(Priority.CRITICAL);
        Ticket result = ticketService.update(testTicket);

        // Then: Should save and return updated ticket
        assertEquals("Updated description", result.getDescription(), "Description should be updated");
        assertEquals(Priority.CRITICAL, result.getPriority(), "Priority should be updated");
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void deleteTicket_ShouldCallRepositoryDelete() {
        // Given: Ticket ID to delete
        Long ticketId = 1L;

        // When: Deleting ticket
        ticketService.deleteById(ticketId);

        // Then: Repository delete should be called
        verify(ticketRepository).deleteById(ticketId);
    }

    @Test
    void assignTicket_ShouldUpdateTicketAndStatus() {
        // Given: Ticket and technician exist
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(technicianRepository.findById(1L)).thenReturn(Optional.of(testTechnician));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When: Assigning ticket to technician
        Ticket result = ticketService.assignTicket(1L, 1L);

        // Then: Ticket should be assigned and status updated
        assertEquals(testTechnician, result.getAssignedTechnician(), "Technician should be assigned");
        assertEquals(TicketStatus.ASSIGNED, result.getStatus(), "Status should be ASSIGNED");
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void findTicketsByStatus_ShouldReturnFilteredTickets() {
        // Given: Tickets with specific status
        List<Ticket> openTickets = List.of(testTicket);
        when(ticketRepository.findByStatus(TicketStatus.OPEN)).thenReturn(openTickets);

        // When: Finding tickets by status
        List<Ticket> result = ticketService.findByStatus(TicketStatus.OPEN);

        // Then: Should return filtered tickets
        assertEquals(1, result.size(), "Should return one open ticket");
        assertEquals(TicketStatus.OPEN, result.get(0).getStatus(), "Ticket should be open");
        verify(ticketRepository).findByStatus(TicketStatus.OPEN);
    }

    @Test
    void findTicketsByClient_ShouldReturnClientTickets() {
        // Given: Tickets for specific client
        List<Ticket> clientTickets = List.of(testTicket);
        when(ticketRepository.findByClient(testClient)).thenReturn(clientTickets);

        // When: Finding tickets by client
        List<Ticket> result = ticketService.findByClient(testClient);

        // Then: Should return client's tickets
        assertEquals(1, result.size(), "Should return client's tickets");
        assertEquals(testClient, result.get(0).getClient(), "Should be correct client");
        verify(ticketRepository).findByClient(testClient);
    }

    @Test
    void findTicketsByTechnician_ShouldReturnTechnicianTickets() {
        // Given: Tickets assigned to technician
        testTicket.setAssignedTechnician(testTechnician);
        List<Ticket> technicianTickets = List.of(testTicket);
        when(ticketRepository.findByAssignedTechnician(testTechnician)).thenReturn(technicianTickets);

        // When: Finding tickets by technician
        List<Ticket> result = ticketService.findByTechnician(testTechnician);

        // Then: Should return technician's tickets
        assertEquals(1, result.size(), "Should return technician's tickets");
        assertEquals(testTechnician, result.get(0).getAssignedTechnician(), "Should be correct technician");
        verify(ticketRepository).findByAssignedTechnician(testTechnician);
    }

    @Test
    void findOverdueTickets_ShouldReturnOverdueTickets() {
        // Given: Overdue tickets exist
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> overdueTickets = List.of(testTicket);
        when(ticketRepository.findOverdueTickets(any(LocalDateTime.class))).thenReturn(overdueTickets);

        // When: Finding overdue tickets
        List<Ticket> result = ticketService.findOverdueTickets();

        // Then: Should return overdue tickets
        assertEquals(1, result.size(), "Should return overdue tickets");
        verify(ticketRepository).findOverdueTickets(any(LocalDateTime.class));
    }

    @Test
    void closeTicket_ShouldUpdateStatusAndTimestamp() {
        // Given: Ticket can be closed
        testTicket.setStatus(TicketStatus.RESOLVED);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When: Closing ticket
        Ticket result = ticketService.closeTicket(1L);

        // Then: Should update status and timestamp
        assertEquals(TicketStatus.CLOSED, result.getStatus(), "Status should be CLOSED");
        assertNotNull(result.getClosedAt(), "Closed timestamp should be set");
        verify(ticketRepository).save(testTicket);
    }

    @Test
    void bulkCloseTickets_ShouldCloseMultipleTickets() {
        // Given: Multiple tickets can be closed
        List<Long> ticketIds = List.of(1L, 2L);
        Ticket ticket2 = new Ticket("Second", testClient, testServiceType);
        ticket2.setId(2L);
        ticket2.setStatus(TicketStatus.RESOLVED);
        
        List<Ticket> ticketsToClose = List.of(testTicket, ticket2);
        when(ticketRepository.findTicketsForBulkClose(ticketIds)).thenReturn(ticketsToClose);
        when(ticketRepository.saveAll(anyList())).thenReturn(ticketsToClose);

        // When: Bulk closing tickets
        List<Ticket> result = ticketService.bulkCloseTickets(ticketIds);

        // Then: Should close all tickets
        assertEquals(2, result.size(), "Should close two tickets");
        result.forEach(ticket -> {
            assertEquals(TicketStatus.CLOSED, ticket.getStatus(), "All tickets should be closed");
            assertNotNull(ticket.getClosedAt(), "All tickets should have closed timestamp");
        });
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    void calculateSlaCompliance_ShouldReturnCorrectPercentage() {
        // Given: SLA compliance data
        Object[] slaData = new Object[]{5L, 2L, 1L}; // onTime, late, overdue
        when(ticketRepository.getSlaComplianceStats(any(LocalDateTime.class))).thenReturn(slaData);

        // When: Calculating SLA compliance
        double compliance = ticketService.calculateSlaCompliance();

        // Then: Should calculate correct percentage
        assertEquals(62.5, compliance, 0.01, "SLA compliance should be 62.5%"); // 5/(5+2+1) * 100
        verify(ticketRepository).getSlaComplianceStats(any(LocalDateTime.class));
    }
} 