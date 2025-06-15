package com.techsupport.service;

import com.techsupport.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Smoke test for TicketService to verify test framework and CI pipeline.
 * This is the Week 1 requirement: "Write a single 'smoke' unit test in 
 * TicketServiceTest that mocks TicketRepository to verify the test framework 
 * and CI pipeline are wired correctly."
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        // Mock setup is handled by @Mock and @InjectMocks annotations
    }

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
} 