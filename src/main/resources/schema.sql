-- Tech Support System Database Schema
-- This schema supports all entities and relationships defined in Week 2

-- Create sequence for H2 database (if needed)
-- H2 will automatically create sequences for IDENTITY columns

-- ============================================================================
-- CORE ENTITIES
-- ============================================================================

-- Clients table
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    company VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for performance
    INDEX idx_clients_email (email),
    INDEX idx_clients_company (company),
    INDEX idx_clients_active (is_active),
    INDEX idx_clients_created (created_at)
);

-- Technicians table
CREATE TABLE IF NOT EXISTS technicians (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes for performance
    INDEX idx_technicians_email (email),
    INDEX idx_technicians_active (is_active),
    INDEX idx_technicians_created (created_at)
);

-- Service Types table
CREATE TABLE IF NOT EXISTS service_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    sla_hours INTEGER NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_service_types_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_service_types_sla_positive CHECK (sla_hours > 0),
    
    -- Indexes for performance
    INDEX idx_service_types_name (name),
    INDEX idx_service_types_priority (priority),
    INDEX idx_service_types_sla (sla_hours),
    INDEX idx_service_types_active (is_active)
);

-- ============================================================================
-- MAIN TICKET ENTITY
-- ============================================================================

-- Tickets table (main entity)
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    
    -- Foreign Keys
    client_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,
    assigned_technician_id BIGINT,
    
    -- Constraints
    CONSTRAINT chk_tickets_status CHECK (status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS', 'PENDING_CLIENT', 'RESOLVED', 'CLOSED')),
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT fk_tickets_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_tickets_service_type FOREIGN KEY (service_type_id) REFERENCES service_types(id),
    CONSTRAINT fk_tickets_technician FOREIGN KEY (assigned_technician_id) REFERENCES technicians(id),
    
    -- Indexes for performance
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_priority (priority),
    INDEX idx_tickets_client (client_id),
    INDEX idx_tickets_service_type (service_type_id),
    INDEX idx_tickets_technician (assigned_technician_id),
    INDEX idx_tickets_created (created_at),
    INDEX idx_tickets_due (due_at),
    INDEX idx_tickets_resolved (resolved_at),
    INDEX idx_tickets_closed (closed_at),
    INDEX idx_tickets_overdue (due_at, status) -- Composite index for overdue queries
);

-- ============================================================================
-- RELATIONSHIP AND SUPPORTING ENTITIES
-- ============================================================================

-- Technician Skills table (many-to-many with additional attributes)
CREATE TABLE IF NOT EXISTS technician_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,
    skill_level VARCHAR(20) NOT NULL DEFAULT 'INTERMEDIATE',
    years_experience INTEGER,
    is_primary_skill BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_technician_skills_level CHECK (skill_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    CONSTRAINT chk_technician_skills_experience CHECK (years_experience IS NULL OR years_experience >= 0),
    CONSTRAINT fk_technician_skills_technician FOREIGN KEY (technician_id) REFERENCES technicians(id) ON DELETE CASCADE,
    CONSTRAINT fk_technician_skills_service_type FOREIGN KEY (service_type_id) REFERENCES service_types(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate skills
    UNIQUE KEY uk_technician_skills (technician_id, service_type_id),
    
    -- Indexes for performance
    INDEX idx_technician_skills_technician (technician_id),
    INDEX idx_technician_skills_service_type (service_type_id),
    INDEX idx_technician_skills_level (skill_level),
    INDEX idx_technician_skills_primary (is_primary_skill)
);

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 60,
    notes VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_appointments_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_appointments_duration CHECK (duration_minutes > 0),
    CONSTRAINT fk_appointments_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_technician FOREIGN KEY (technician_id) REFERENCES technicians(id),
    
    -- Indexes for performance
    INDEX idx_appointments_ticket (ticket_id),
    INDEX idx_appointments_technician (technician_id),
    INDEX idx_appointments_scheduled (scheduled_at),
    INDEX idx_appointments_status (status),
    INDEX idx_appointments_technician_time (technician_id, scheduled_at), -- For conflict detection
    INDEX idx_appointments_completed (completed_at)
);

-- Feedback table
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL UNIQUE, -- One feedback per ticket
    rating INTEGER NOT NULL,
    comments TEXT,
    is_satisfied BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_feedback_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_feedback_ticket (ticket_id),
    INDEX idx_feedback_rating (rating),
    INDEX idx_feedback_satisfied (is_satisfied),
    INDEX idx_feedback_created (created_at)
);

-- Ticket History table (audit trail)
CREATE TABLE IF NOT EXISTS ticket_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    old_value VARCHAR(1000),
    new_value VARCHAR(1000),
    changed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_history_action_type CHECK (action_type IN (
        'CREATED', 'STATUS_CHANGED', 'PRIORITY_CHANGED', 'ASSIGNED', 'UNASSIGNED',
        'UPDATED', 'COMMENTED', 'APPOINTMENT_SCHEDULED', 'APPOINTMENT_CANCELLED',
        'APPOINTMENT_COMPLETED', 'FEEDBACK_SUBMITTED', 'RESOLVED', 'CLOSED', 'REOPENED'
    )),
    CONSTRAINT fk_ticket_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_ticket_history_ticket (ticket_id),
    INDEX idx_ticket_history_action (action_type),
    INDEX idx_ticket_history_created (created_at),
    INDEX idx_ticket_history_changed_by (changed_by),
    INDEX idx_ticket_history_ticket_time (ticket_id, created_at) -- For chronological queries
);

-- ============================================================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- ============================================================================

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_tickets_status_priority ON tickets(status, priority);
CREATE INDEX IF NOT EXISTS idx_tickets_client_status ON tickets(client_id, status);
CREATE INDEX IF NOT EXISTS idx_tickets_technician_status ON tickets(assigned_technician_id, status);
CREATE INDEX IF NOT EXISTS idx_tickets_service_type_status ON tickets(service_type_id, status);
CREATE INDEX IF NOT EXISTS idx_tickets_overdue_critical ON tickets(due_at, status, priority);

-- ============================================================================
-- SAMPLE DATA FOR DEVELOPMENT AND TESTING
-- ============================================================================

-- Insert sample service types
INSERT INTO service_types (name, description, sla_hours, priority) VALUES
('Hardware Issue', 'Physical hardware problems and repairs', 24, 'HIGH'),
('Software Bug', 'Software defects and application issues', 48, 'MEDIUM'),
('Network Problem', 'Connectivity and network infrastructure issues', 4, 'CRITICAL'),
('Account Access', 'User account and authentication problems', 8, 'HIGH'),
('General Inquiry', 'General questions and information requests', 72, 'LOW'),
('Security Incident', 'Security breaches and vulnerability reports', 2, 'CRITICAL'),
('Performance Issue', 'System performance and optimization problems', 24, 'MEDIUM'),
('Training Request', 'User training and documentation requests', 120, 'LOW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert sample clients
INSERT INTO clients (first_name, last_name, email, company) VALUES
('John', 'Smith', 'john.smith@techcorp.com', 'TechCorp Inc'),
('Jane', 'Doe', 'jane.doe@innovate.com', 'Innovate Solutions'),
('Mike', 'Johnson', 'mike.johnson@startup.io', 'StartupIO'),
('Sarah', 'Wilson', 'sarah.wilson@enterprise.com', 'Enterprise Systems'),
('David', 'Brown', 'david.brown@freelance.com', NULL)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Insert sample technicians
INSERT INTO technicians (first_name, last_name, email) VALUES
('Alice', 'Technical', 'alice.technical@techsupport.com'),
('Bob', 'Hardware', 'bob.hardware@techsupport.com'),
('Carol', 'Software', 'carol.software@techsupport.com'),
('Dan', 'Network', 'dan.network@techsupport.com'),
('Eve', 'Security', 'eve.security@techsupport.com')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Insert sample technician skills (this will be expanded in service layer tests)
INSERT INTO technician_skills (technician_id, service_type_id, skill_level, years_experience, is_primary_skill) 
SELECT t.id, st.id, 'EXPERT', 5, TRUE
FROM technicians t, service_types st 
WHERE t.email = 'alice.technical@techsupport.com' AND st.name = 'Hardware Issue'
ON DUPLICATE KEY UPDATE skill_level = VALUES(skill_level);

INSERT INTO technician_skills (technician_id, service_type_id, skill_level, years_experience, is_primary_skill)
SELECT t.id, st.id, 'EXPERT', 7, TRUE
FROM technicians t, service_types st 
WHERE t.email = 'carol.software@techsupport.com' AND st.name = 'Software Bug'
ON DUPLICATE KEY UPDATE skill_level = VALUES(skill_level);

INSERT INTO technician_skills (technician_id, service_type_id, skill_level, years_experience, is_primary_skill)
SELECT t.id, st.id, 'EXPERT', 10, TRUE
FROM technicians t, service_types st 
WHERE t.email = 'dan.network@techsupport.com' AND st.name = 'Network Problem'
ON DUPLICATE KEY UPDATE skill_level = VALUES(skill_level);

-- ============================================================================
-- VIEWS FOR REPORTING (OPTIONAL)
-- ============================================================================

-- View for ticket summary with related entity names
CREATE OR REPLACE VIEW ticket_summary AS
SELECT 
    t.id,
    t.title,
    t.status,
    t.priority,
    t.created_at,
    t.due_at,
    t.resolved_at,
    t.closed_at,
    CONCAT(c.first_name, ' ', c.last_name) AS client_name,
    c.company AS client_company,
    CONCAT(tech.first_name, ' ', tech.last_name) AS technician_name,
    st.name AS service_type_name,
    st.sla_hours,
    CASE 
        WHEN t.due_at < CURRENT_TIMESTAMP AND t.status != 'CLOSED' THEN 'OVERDUE'
        WHEN t.due_at < DATEADD('HOUR', 2, CURRENT_TIMESTAMP) AND t.status != 'CLOSED' THEN 'DUE_SOON'
        ELSE 'ON_TIME'
    END AS sla_status
FROM tickets t
JOIN clients c ON t.client_id = c.id
JOIN service_types st ON t.service_type_id = st.id
LEFT JOIN technicians tech ON t.assigned_technician_id = tech.id;

-- View for technician workload
CREATE OR REPLACE VIEW technician_workload AS
SELECT 
    tech.id,
    CONCAT(tech.first_name, ' ', tech.last_name) AS technician_name,
    tech.email,
    COUNT(CASE WHEN t.status != 'CLOSED' THEN 1 END) AS open_tickets,
    COUNT(CASE WHEN t.status = 'CLOSED' THEN 1 END) AS closed_tickets,
    COUNT(t.id) AS total_tickets,
    AVG(CASE 
        WHEN t.status = 'CLOSED' AND t.closed_at <= t.due_at THEN 1.0
        WHEN t.status = 'CLOSED' AND t.closed_at > t.due_at THEN 0.0
        ELSE NULL
    END) AS sla_compliance_rate
FROM technicians tech
LEFT JOIN tickets t ON tech.id = t.assigned_technician_id
WHERE tech.is_active = TRUE
GROUP BY tech.id, tech.first_name, tech.last_name, tech.email; 