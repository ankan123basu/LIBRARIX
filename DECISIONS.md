# LIBRARIX Architectural Decisions & Algorithmic Design

This document details key architectural decisions, data structure selections, and algorithms powering **LIBRARIX**.

---

## 1. Algorithmic Selection: Dynamic Urgency & Aging Score Queue vs Plain FIFO

### Problem Statement
Standard library systems employ a strict FIFO (First-In, First-Out) waitlist queue. In an academic campus setting, this creates severe inefficiencies:
- A student requiring a high-demand core textbook for a Capstone project due in 48 hours is blocked behind general interest requests.
- Conversely, pure priority queues cause **starvation**: low-priority members may never receive high-demand resources.

### Solution: Dynamic Multi-Criteria Score Algorithm with Wait-Time Aging
LIBRARIX implements a custom prioritization algorithm inside [PriorityQueueEngine.java](file:///e:/LIBRARIX/backend/src/main/java/com/librarix/algorithm/PriorityQueueEngine.java):

$$\text{PriorityScore} = (T_{\text{wait\_hours}} \times 1.5) + U_{\text{urgency\_boost}} + (A_{\text{user\_tier}} \times 10.0)$$

#### Parameters:
1. **$T_{\text{wait\_hours}}$ (Wait-Time Aging)**: Every hour a user remains in the queue increases their score by $1.5$ points. Over time, any user waiting long enough will outrank a newly placed high-urgency request, mathematically solving the starvation problem.
2. **$U_{\text{urgency\_boost}}$**: Static boost based on self-declared need level (`STANDARD`: 10 pts, `HIGH`: 25 pts, `CRITICAL`: 50 pts).
3. **$A_{\text{user\_tier}}$**: Academic role weighting multiplier (`REGULAR`: 1.0, `CAPSTONE`: 1.5, `FACULTY`: 2.0).

---

## 2. Rule Engine Design: Composite Overdue Fine Engine

Instead of hardcoding fine logic inside the controller or repository, LIBRARIX delegates fine evaluation to [FineCalculationService.java](file:///e:/LIBRARIX/backend/src/main/java/com/librarix/service/FineCalculationService.java).

### Rule Matrix:
1. **Grace Period**: $24\text{ hours}$ post due-date exemption.
2. **Category Rate Differentiation**:
   - `BOOK`: Standard daily rate ($\$2.00/\text{day}$).
3. **Cap Constraint**: Penalties are bounded by a configurable cap ($\$50.00\text{ max}$) to avoid unmanageable debt traps.
4. **Administrative Waiver Control**: `ROLE_LIBRARIAN` and `ROLE_ADMIN` can waive fines with audit trail logging (`waivedBy` field).

---

## 3. Concurrency & Locking Strategy

### MongoDB Optimistic Locking
To prevent race conditions during high-concurrency book borrowing (e.g., 100 students attempting to borrow the last available CLRS textbook copy simultaneously):
- The `Resource` entity includes a `@Version Long version` attribute.
- Spring Data Mongo automatically verifies version matching during updates. If another transaction modified the resource concurrently, an `OptimisticLockingFailureException` is thrown, preventing double-allocation.
