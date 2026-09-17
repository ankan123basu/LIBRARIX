# LIBRARIX — System Operations, JDK Configuration & Execution Guide

---

## 🚀 1. How to Run LIBRARIX (Without Docker)

### Prerequisites
* **MongoDB Server**: Running locally on `localhost:27017` (Windows Service `MongoDB` is active).
* **Java JDK 21**: Located at `D:\java21`.
* **Node.js**: Installed (v18+ / v20+).

---

### Step-by-Step Execution Commands

#### 🔷 Step A: Run Spring Boot Backend
Open PowerShell in `e:\LIBRARIX\backend`:

```powershell
$env:JAVA_HOME="D:\java21"; $env:Path="D:\java21\bin;" + $env:Path; mvn spring-boot:run
```
> 📍 **Backend Service**: `http://localhost:8085`  
> 📡 **WebSocket Endpoint**: `ws://localhost:8085/ws`

---

#### 🔷 Step B: Run Next.js Frontend
Open a separate PowerShell terminal in `e:\LIBRARIX\frontend`:

```powershell
npm run dev
```
> 🌐 **Web Interface**: `http://localhost:3000`  
> 🧊 **3D Immersive Hall**: `http://localhost:3000/shelf-hall`

---

## 🐳 2. How to Run with Docker (Alternative)

If you prefer containerized deployment:

```powershell
cd e:\LIBRARIX
docker-compose up --build
```

---

## 🍃 3. MongoDB & Compass Setup

1. **Local MongoDB Server**: Running as Windows Service on port `27017`.
2. **MongoDB Compass**: Open MongoDB Compass and connect to `mongodb://localhost:27017`.
3. **Database Creation**: The database `librarix` and collections (`users`, `resources`, `loans`, `reservation_queues`, `fines`, `notifications`) are created automatically when Spring Boot starts up.

---

## ☕ 4. Technical Explanation: JDK 21 vs. JDK 26

### Why `D:\java21` (Java 21 LTS) is Used:
* **Lombok Compiler Hook**: LIBRARIX uses Lombok (`@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`) across entities, DTOs, and services.
* **JDK 26 Internal Changes**: Oracle modified internal `javac` classes (`com.sun.tools.javac.code.TypeTag`) in JDK 26 early access builds. When compiling under JDK 26, Lombok throws:
  ```text
  Caused by: java.lang.NoSuchFieldException: com.sun.tools.javac.code.TypeTag :: UNKNOWN
      at lombok.javac.JavacTreeMaker$TypeTag.typeTag(JavacTreeMaker.java:259)
  ```
* **Java 26 Non-Lombok Projects**: Java 26 works fine for projects using standard Java classes, Java 17 `record`s, or manual getters/setters.
* **Java 21 (LTS)**: Spring Boot 3.2.3 officially targets Java 21 LTS. Under JDK 21, all 80 Java files compile cleanly (`BUILD SUCCESS`) and pass 100% of unit tests.

---

## 🧠 5. Architecture & Algorithmic Highlights

1. **Weighted Fair Queue Engine (WFQ)**: Dynamic scoring ($S = T_{\text{wait}} \times 1.5 + U_{\text{boost}} + A_{\text{tier}}$) preventing waitlist starvation.
2. **LRU-K Cache ($K=2$)**: In-memory catalog cache preventing single-pass scan pollution.
3. **Co-Borrow Graph Label Propagation**: Unsupervised community detection algorithm calculating resource borrowing affinity.
4. **Greedy 3D Shelf Slotting Optimizer**: Maps popularity demand and affinity clusters to 3D shelf coordinates $(X, Y, Z)$.
5. **RAG AI Package**: Groq API integration (`openai/gpt-oss-120b`), HuggingFace L2 Euclidean vector distance search, and exponential smoothing exam demand forecasting.

---

## 🧪 6. Testing & Endpoint Verification

* **Unit Tests**:
  ```powershell
  $env:JAVA_HOME="D:\java21"; $env:Path="D:\java21\bin;" + $env:Path; mvn clean test
  ```
* **REST API Testing**: Use [librarix-api.http](file:///e:/LIBRARIX/librarix-api.http) inside VS Code / IntelliJ.
