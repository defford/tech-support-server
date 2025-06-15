Server Tasks
Week 1:
Initialize the tech-support-server GitHub repo with pom.xml, .gitignore, README.md and Maven structure.
Scaffold Spring Boot starter application (TechSupportApplication.java) and add H2, Spring Data JPA, and Web dependencies.
Create a basic GitHub Actions CI workflow that checks out the code, runs mvn compile, and executes any existing tests.
Write a single “smoke” unit test in TicketServiceTest that mocks TicketRepository to verify the test framework and CI pipeline are wired correctly.
Week 2:
Define all JPA entities (Technician, Client, ServiceType, Ticket, Appointment, Feedback, TechnicianSkill, TicketHistory) with appropriate relationships and annotations.
Create Spring Data JPA repositories for each entity and add any custom finder methods you’ll need.
Wire up H2 for dev and test profiles and add an initial schema migration (SQL or Flyway) to create all tables.
Expand your service‐layer unit tests to mock repositories and verify basic CRUD operations on each entity.
Week 3:
Implement TicketService.createTicket() to validate inputs, calculate dueAt from SLA, and persist the new ticket.
Add getByTechnician(), getByClient(), and getByServiceType() methods to TicketService.
Write unit tests for TicketService covering missing client/service type, due-date calculation, and each GET method.
Implement bulk operations in TicketService: bulkClose(List<id>) and bulkAssign(List<id>, technicianId), along with unit tests for invalid IDs and mixed states.
Week 4:
Build out TicketController with endpoints for creating tickets, fetching tickets, and bulk operations.
Add a global exception handler (GlobalExceptionHandler) to translate business exceptions into HTTP responses.
Write @WebMvcTest suites for each controller endpoint, verifying JSON payloads and status codes.
Manual smoke-test all ticket endpoints in Postman using an exported OpenAPI spec.
Week 5:
Enhance TicketService with skill-based auto-assignment: query TechnicianSkill, count each tech’s open tickets, and choose the least-loaded.
Write unit tests that mock repository calls to simulate multiple technicians and verify load-balancing logic.
Implement AppointmentService.schedule() to check for overlapping slots and persist appointments.
Cover appointment scheduling and conflict detection in unit tests.
Week 6:
Implement FeedbackService.submitFeedback(), enforcing that only closed tickets can receive feedback, then write unit tests for valid and invalid cases.
Build TicketHistoryService.logEvent() and hook it into every ticket mutation; unit-test that history entries capture old and new values correctly.
Create ReportService methods for SLA compliance and technician utilization metrics, with unit tests that feed in mocked tickets to verify calculations.
Expose feedback, history, and report endpoints in their controllers and write WebMvc tests for each.
Week 7:
Polish the server: finalize Swagger/OpenAPI documentation, generate the Postman collection, and ensure all controllers are documented.
Review style and static analysis: integrate Checkstyle/SpotBugs, fail the build on violations, and bump unit test coverage toward 90%+.
Merge any outstanding feature branches, clean up debug logging, and tag a v1.0.0-server release.
Week 8:
(Optional) Prepare for future integration tests by adding placeholders for Testcontainers or Docker Compose, but keep H2 as your primary dev/test database.
Record a short server-focused demo showing ticket creation, auto-assignment, appointment booking, feedback submission, history retrieval, and reporting endpoints.