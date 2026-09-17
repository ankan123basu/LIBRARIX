# LIBRARIX Technical Trade-Offs & Architecture Analysis

This document analyzes the technical trade-offs evaluated during the architectural design of **LIBRARIX**.

---

## 1. Database Paradigm: MongoDB vs Relational Database (PostgreSQL)

| Criteria | Choice: MongoDB (Spring Data Mongo) | Alternative: PostgreSQL (Spring Data JPA) | Trade-Off Rationale |
| :--- | :--- | :--- | :--- |
| **Schema Flexibility** | Schema-less `@Document` objects permit rapid extension of resource attributes (e.g. edition numbers, publisher metadata, book ISBNs). | Rigid tabular schema requiring migrations for metadata changes. | Chosen MongoDB for dynamic book metadata schemas with varying properties. |
| **Queue Atomic Updates** | Single-document atomic operations (`findAndModify`, `$push`). | Multi-table ACID joins with explicit row locks (`SELECT FOR UPDATE`). | MongoDB allows atomic queue array updates without explicit table locks. |
| **ACID Multi-Doc Transactions** | Supported via replica sets, but adds latency overhead. | Native multi-table transactional guarantees out of the box. | Tradeoff accepted: LIBRARIX relies on single-document atomic updates and `@Version` optimistic locking. |

---

## 2. Real-Time Transport: Simple In-Memory STOMP Broker vs External Message Broker (RabbitMQ)

- **Choice Made**: Spring Boot Simple In-Memory Broker (`WebSocketConfig`).
- **Pros**: Zero external infrastructure setup required for local dev & viva demos; fast in-memory event dispatching.
- **Cons**: Does not scale across horizontally clustered application instances.
- **Production Path**: In a multi-node production cluster, swap `.enableSimpleBroker()` with `.enableStompBrokerRelay()` connected to RabbitMQ / ActiveMQ.

---

## 3. Authentication Strategy: Stateless JWT vs Stateful HTTP Sessions

- **Choice Made**: Stateless JWT with HMAC-SHA512 signing in `Authorization: Bearer` header.
- **Pros**: Scalable, zero server memory footprint, compatible with mobile clients & cross-origin microservices.
- **Cons**: Instant token revocation requires a blacklisting mechanism (e.g., Redis blocklist).
- **Mitigation**: Configured 24-hour token expiration with refresh capability.
