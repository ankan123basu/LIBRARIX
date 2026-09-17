package com.librarix.config;

import com.librarix.model.*;
import com.librarix.model.enums.*;
import com.librarix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    @Override
    public void run(String... args) {
        log.info("Checking LIBRARIX MongoDB dataset seeding status (60 items)...");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 1. Seed Initial Default Users if DB empty
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("admin@librarix.edu")
                    .password(encoder.encode("AdminPass123!"))
                    .fullName("Dr. Marcus Vance (Admin)")
                    .role(Role.ROLE_ADMIN)
                    .userTier(UserTier.FACULTY)
                    .build());

            userRepository.save(User.builder()
                    .email("librarian@librarix.edu")
                    .password(encoder.encode("LibPass123!"))
                    .fullName("Sarah Jenkins (Chief Librarian)")
                    .role(Role.ROLE_LIBRARIAN)
                    .userTier(UserTier.FACULTY)
                    .build());

            userRepository.save(User.builder()
                    .email("alice@librarix.edu")
                    .password(encoder.encode("StudentPass123!"))
                    .fullName("Alice Chen (Senior Capstone)")
                    .role(Role.ROLE_MEMBER)
                    .userTier(UserTier.CAPSTONE)
                    .build());

            userRepository.save(User.builder()
                    .email("bob@librarix.edu")
                    .password(encoder.encode("StudentPass123!"))
                    .fullName("Bob Smith (Undergrad Member)")
                    .role(Role.ROLE_MEMBER)
                    .userTier(UserTier.REGULAR)
                    .build());

            log.info("Seeded 4 default users in MongoDB: Admin, Librarian, Alice, Bob.");
        }

        // 2. Seed 60 Campus Technical Dataset Items into MongoDB
        seedResourceIfMissing("res-101", "005.133 LIB", "Designing Data-Intensive Applications", "Martin Kleppmann",
                "The definitive guide to distributed systems, storage engines, consensus protocols, and stream processing architectures.",
                "https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-A1",
                List.of("programming", "distributed-systems", "architecture"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-102", "005.741 MON", "MongoDB: The Definitive Guide (3rd Ed)", "Shannon Bradshaw & Eoin Brazil",
                "Comprehensive guide to document data modeling, aggregation pipelines, replica sets, and sharded cluster management.",
                "https://covers.openlibrary.org/b/isbn/9781491954249-L.jpg", ResourceType.BOOK, 4, 0, "SHELF-A2",
                List.of("database", "nosql", "mongodb"), ResourceStatus.BORROWED);

        seedResourceIfMissing("res-103", "005.276 SPR", "Spring Boot 3 in Action", "Craig Walls",
                "Building microservices, Spring Security JWT authentication, WebFlux reactive streams, and REST API controllers.",
                "https://covers.openlibrary.org/b/isbn/9781617292545-L.jpg", ResourceType.BOOK, 3, 1, "SHELF-A3",
                List.of("java", "spring-boot", "backend"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-104", "005.430 OS1", "Operating System Concepts (10th Ed)", "Abraham Silberschatz & Peter B. Galvin",
                "Process synchronization, virtual memory management, file systems, I/O subsystems, and kernel architecture.",
                "https://covers.openlibrary.org/b/isbn/9781118063330-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-A4",
                List.of("operating-systems", "kernel", "cs-core"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-105", "005.133 SICP", "Structure and Interpretation of Computer Programs", "Harold Abelson & Gerald Jay Sussman",
                "Lisp, functional programming, data abstraction, metalinguistic abstraction, and register machine execution.",
                "https://covers.openlibrary.org/b/isbn/9780262510875-L.jpg", ResourceType.BOOK, 4, 1, "SHELF-A5",
                List.of("programming-theory", "lisp", "cs-classic"), ResourceStatus.BORROWED);

        seedResourceIfMissing("res-106", "005.740 DBC", "Database System Concepts (7th Ed)", "Silberschatz, Korth & Sudarshan",
                "Relational algebra, SQL, query optimization, transaction processing, concurrency control, and index structures.",
                "https://covers.openlibrary.org/b/isbn/9780078022159-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-A6",
                List.of("databases", "sql", "transactions"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-107", "005.117 CLN", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin (Uncle Bob)",
                "Principles, patterns, and practices of writing clean, readable, refactorable, and maintainable software code.",
                "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-B1",
                List.of("clean-code", "refactoring", "agile"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-108", "005.300 SYS", "System Design Interview — An Insider's Guide", "Alex Xu",
                "Step-by-step framework for designing large-scale web systems: rate limiters, key-value stores, distributed caches, and newsfeed algorithms.",
                "https://images-na.ssl-images-amazon.com/images/I/71u9i8S2GGL.jpg", ResourceType.BOOK, 4, 1, "SHELF-B2",
                List.of("system-design", "interview", "scalability"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-109", "005.100 ALG", "Introduction to Algorithms (CLRS 4th Edition)", "Thomas H. Cormen, Charles E. Leiserson",
                "The standard algorithm textbook covering dynamic programming, graph algorithms, NP-completeness, and B-trees.",
                "https://covers.openlibrary.org/b/isbn/9780262046305-L.jpg", ResourceType.BOOK, 8, 5, "SHELF-B3",
                List.of("algorithms", "data-structures", "cs-theory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-110", "004.600 NET", "Computer Networking: A Top-Down Approach (8th Ed)", "James Kurose & Keith Ross",
                "Top-down layer approach to computer networks: HTTP/3, TCP/UDP sockets, BGP routing protocols, and Wi-Fi security.",
                "https://covers.openlibrary.org/b/isbn/9780133594140-L.jpg", ResourceType.BOOK, 5, 2, "SHELF-C1",
                List.of("networking", "protocols", "tcp-ip"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-111", "006.300 ART", "Artificial Intelligence: A Modern Approach (4th Ed)", "Stuart Russell & Peter Norvig",
                "The leading textbook in AI covering probabilistic reasoning, search algorithms, reinforcement learning, and NLP.",
                "https://covers.openlibrary.org/b/isbn/9780134610993-L.jpg", ResourceType.BOOK, 6, 0, "SHELF-C2",
                List.of("ai", "machine-learning", "python"), ResourceStatus.BORROWED);

        seedResourceIfMissing("res-112", "006.310 DEE", "Deep Learning (Adaptive Computation & ML Series)", "Ian Goodfellow, Yoshua Bengio, Aaron Courville",
                "Mathematical foundation of deep neural networks, backpropagation, CNNs, Transformers, and Generative Adversarial Networks.",
                "https://covers.openlibrary.org/b/isbn/9780262035613-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-C3",
                List.of("deep-learning", "neural-networks", "pytorch"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-113", "621.398 RISCV", "Computer Organization & Design: RISC-V Edition", "David A. Patterson & John L. Hennessy",
                "Hardware/software interface, RISC-V instruction set architecture, pipelined datapath design, and cache memory hierarchy.",
                "https://covers.openlibrary.org/b/isbn/9780128122754-L.jpg", ResourceType.BOOK, 4, 3, "SHELF-C4",
                List.of("architecture", "risc-v", "hardware-design"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-114", "005.100 TAOCP", "The Art of Computer Programming (Vol 1-4)", "Donald E. Knuth",
                "Fundamental algorithms, information structures, seminumerical algorithms, sorting, searching, and combinatorial algorithms.",
                "https://covers.openlibrary.org/b/isbn/9780321751041-L.jpg", ResourceType.BOOK, 5, 4, "SHELF-C5",
                List.of("algorithms", "knuth", "cs-classic"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-115", "005.453 DRG", "Compilers: Principles, Techniques & Tools (Dragon Book)", "Aho, Lam, Sethi & Ullman",
                "Lexical analysis, LL/LR parsing, syntax-directed translation, intermediate code generation, and instruction-level optimization.",
                "https://covers.openlibrary.org/b/isbn/9780321486813-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-C6",
                List.of("compilers", "parsing", "programming-languages"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-116", "004.360 DIS", "Distributed Systems: Principles and Paradigms", "Andrew S. Tanenbaum & Maarten Van Steen",
                "Architectures, processes, communication, naming, synchronization, consistency models, fault tolerance, and security in distributed systems.",
                "https://covers.openlibrary.org/b/isbn/9781543057386-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-D1",
                List.of("distributed-systems", "fault-tolerance", "consensus"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-117", "005.131 CSAPP", "Computer Systems: A Programmer's Perspective", "Randal E. Bryant & David R. O'Hallaron",
                "Machine-level code execution, processor architecture, virtual memory, dynamic memory allocation, and concurrent programming.",
                "https://covers.openlibrary.org/b/isbn/9780134092669-L.jpg", ResourceType.BOOK, 7, 5, "SHELF-D2",
                List.of("cs-systems", "c-programming", "memory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-118", "006.312 PDS", "Python Data Science Handbook (2nd Ed)", "Jake VanderPlas",
                "Essential tools for working with data in Python: IPython, NumPy, Pandas, Matplotlib, and Scikit-Learn pipelines.",
                "https://covers.openlibrary.org/b/isbn/9781098115784-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-D3",
                List.of("python", "data-science", "pandas"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-119", "005.100 DAA", "Algorithms Unlocked (MIT Press)", "Thomas H. Cormen",
                "Gentle introduction to computer algorithms: searching, sorting, graph algorithms, string processing, and basic cryptography.",
                "https://covers.openlibrary.org/b/isbn/9780262518802-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-D4",
                List.of("algorithms", "intro-cs", "mit-press"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-120", "005.133 HASK", "Learn You a Haskell for Great Good!", "Miran Lipovača",
                "Fun and accessible introduction to functional programming, purity, immutability, monads, functors, and typeclasses in Haskell.",
                "https://covers.openlibrary.org/b/isbn/9781593272838-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-D5",
                List.of("haskell", "functional-programming", "types"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-121", "005.430 OS2", "Operating System Concepts (10th Edition)", "Abraham Silberschatz, Peter B. Galvin",
                "Process synchronization, virtual memory management, file systems, I/O subsystems, virtual machines, and security protection.",
                "https://covers.openlibrary.org/b/isbn/9781118063330-L.jpg", ResourceType.BOOK, 8, 4, "SHELF-E1",
                List.of("operating-systems", "kernel", "memory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-122", "005.453 CMP2", "Compilers: Principles, Techniques, and Tools", "Alfred V. Aho, Monica S. Lam",
                "Lexical analysis, syntax-directed translation, intermediate code generation, run-time environments, and code optimization.",
                "https://covers.openlibrary.org/b/isbn/9780321486813-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-E2",
                List.of("compilers", "parsing", "optimization"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-123", "005.740 SQL", "High Performance MySQL (4th Edition)", "Silvia Botros & Jeremy Tinley",
                "Optimization, backups, replication, scaling, indexing, InnoDB engine internals, and query execution profiling.",
                "https://covers.openlibrary.org/b/isbn/9781492080519-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-E3",
                List.of("database", "mysql", "sql-tuning"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-124", "006.310 PRML", "Pattern Recognition and Machine Learning", "Christopher M. Bishop",
                "Comprehensive introduction to Bayesian methods, linear classification, neural networks, kernel methods, and graphical models.",
                "https://covers.openlibrary.org/b/isbn/9780387310732-L.jpg", ResourceType.BOOK, 6, 3, "SHELF-E4",
                List.of("machine-learning", "statistics", "bayesian"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-125", "005.133 CPP", "The C++ Programming Language (4th Edition)", "Bjarne Stroustrup",
                "Definitive resource on C++11 core language features, standard library containers, move semantics, and generic programming.",
                "https://covers.openlibrary.org/b/isbn/9780321563842-L.jpg", ResourceType.BOOK, 7, 5, "SHELF-F1",
                List.of("cpp", "programming", "stl"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-126", "005.133 RUST", "The Rust Programming Language", "Steve Klabnik & Carol Nichols",
                "Memory safety without garbage collection: ownership, borrowing, lifetimes, cargo package manager, and concurrency.",
                "https://covers.openlibrary.org/b/isbn/9781593278281-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-F2",
                List.of("rust", "systems-programming", "memory-safety"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-127", "005.754 RED", "Redis in Action", "Josiah L. Carlson",
                "In-memory data structure store: key-value caching, Pub/Sub messaging, geospatial indexes, Lua scripting, and Sentinel cluster HA.",
                "https://covers.openlibrary.org/b/isbn/9781617290855-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-F3",
                List.of("redis", "caching", "nosql"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-128", "004.620 KAF", "Kafka: The Definitive Guide (2nd Edition)", "Gwen Shapira, Todd Palino",
                "Distributed event streaming platform: partition architecture, consumer groups, Kafka Connect, Schema Registry, and Exactly-Once Semantics.",
                "https://covers.openlibrary.org/b/isbn/9781492043089-L.jpg", ResourceType.BOOK, 6, 3, "SHELF-G1",
                List.of("kafka", "event-streaming", "distributed-systems"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-129", "005.800 SEC", "The Web Application Hacker's Handbook (2nd Ed)", "Dafydd Stuttard & Marcus Pinto",
                "Attacking and defending web applications: SQL injection, XSS, CSRF, authentication bypass, session hijacking, and API security.",
                "https://covers.openlibrary.org/b/isbn/9781118026472-L.jpg", ResourceType.BOOK, 5, 2, "SHELF-G2",
                List.of("cybersecurity", "web-security", "pentesting"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-130", "006.370 CV", "Computer Vision: Algorithms and Applications", "Richard Szeliski",
                "Image formation, feature detection, motion estimation, structure from motion, 3D reconstruction, and deep learning vision models.",
                "https://covers.openlibrary.org/b/isbn/9781848829343-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-G3",
                List.of("computer-vision", "opencv", "image-processing"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-131", "006.380 RL", "Reinforcement Learning: An Introduction (2nd Ed)", "Richard S. Sutton & Andrew G. Barto",
                "Markov decision processes, dynamic programming, Monte Carlo methods, Temporal-Difference learning, Q-learning, and policy gradients.",
                "https://covers.openlibrary.org/b/isbn/9780262039246-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-H1",
                List.of("reinforcement-learning", "ai", "q-learning"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-132", "005.140 TST", "Test Driven Development: By Example", "Kent Beck",
                "Red-Green-Refactor workflow, writing clean automated unit tests, mock objects, and test-driven architecture design patterns.",
                "https://covers.openlibrary.org/b/isbn/9780321146533-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-H2",
                List.of("tdd", "testing", "software-engineering"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-133", "005.730 DS", "Data Structures and Algorithm Analysis in C++", "Mark Allen Weiss",
                "AVL trees, Splay trees, Priority Queues (Heaps), Disjoint Set data structure, Graph algorithms (Dijkstra, Kruskal), and amortized analysis.",
                "https://covers.openlibrary.org/b/isbn/9780132847377-L.jpg", ResourceType.BOOK, 7, 4, "SHELF-H3",
                List.of("data-structures", "algorithms", "cpp"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-134", "004.350 PAR", "An Introduction to Parallel Programming", "Peter Pacheco",
                "MPI (Message Passing Interface), Pthreads, OpenMP multi-threading, GPU parallel computing, and performance scalability tuning.",
                "https://covers.openlibrary.org/b/isbn/9780123742605-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-I1",
                List.of("parallel-computing", "mpi", "openmp"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-135", "005.300 K8S", "Kubernetes Up & Running (3rd Edition)", "Brendan Burns, Joe Beda, Kelsey Hightower",
                "Container orchestration: Pods, Services, Ingress controllers, ConfigMaps, Secrets, Horizontal Pod Autoscaling, and Helm charts.",
                "https://covers.openlibrary.org/b/isbn/9781098110208-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-I2",
                List.of("kubernetes", "devops", "docker"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-136", "005.133 GO", "The Go Programming Language", "Alan A. A. Donovan & Brian W. Kernighan",
                "Goroutines, channels, interfaces, reflection, low-level unsafe pointer operations, and building concurrent cloud services in Go.",
                "https://covers.openlibrary.org/b/isbn/9780134190440-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-I3",
                List.of("golang", "concurrency", "cloud-native"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-137", "005.133 PY", "Fluent Python (2nd Edition)", "Luciano Ramalho",
                "Pythonic data models, generator functions, coroutines, asyncio event loops, type hinting, decorators, and meta-programming.",
                "https://covers.openlibrary.org/b/isbn/9781491946008-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-J1",
                List.of("python", "advanced-python", "asyncio"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-138", "006.310 PYT", "Programming PyTorch for Deep Learning", "Ian Pointer",
                "Building, training, and deploying neural networks: CNNs, Transfer Learning, Recurrent Networks, TensorBoard, and PyTorch Lightning.",
                "https://covers.openlibrary.org/b/isbn/9781492045359-L.jpg", ResourceType.BOOK, 5, 2, "SHELF-J2",
                List.of("pytorch", "deep-learning", "ai"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-139", "005.741 CAS", "Cassandra: The Definitive Guide (3rd Edition)", "Jeff Carpenter & Eben Hewitt",
                "Distributed NoSQL database: peer-to-peer architecture, tuneable consistency levels, CQL, SSTables, and cross-datacenter replication.",
                "https://covers.openlibrary.org/b/isbn/9781492055624-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-J3",
                List.of("cassandra", "nosql", "distributed-database"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-140", "004.220 ARCH", "Computer Architecture: A Quantitative Approach (6th Ed)", "John L. Hennessy & David A. Patterson",
                "Instruction-level parallelism, speculative execution, cache hierarchies, domain-specific accelerators, and GPU SIMD architectures.",
                "https://covers.openlibrary.org/b/isbn/9780128119051-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-K1",
                List.of("computer-architecture", "hardware-theory", "processors"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-141", "530.12 QUANT", "Principles of Quantum Mechanics (2nd Ed)", "R. Shankar",
                "Mathematical foundations of quantum mechanics: vector spaces, Dirac notation, harmonic oscillator, spin, and perturbation theory.",
                "https://covers.openlibrary.org/b/isbn/9780306447908-L.jpg", ResourceType.BOOK, 4, 3, "SHELF-K2",
                List.of("quantum-mechanics", "physics", "mathematics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-142", "512.2 GROUPS", "Abstract Algebra (3rd Edition)", "David S. Dummit & Richard M. Foote",
                "Group theory, ring theory, Galois theory, module theory, vector spaces, and commutative algebra.",
                "https://covers.openlibrary.org/b/isbn/9780471433347-L.jpg", ResourceType.BOOK, 5, 4, "SHELF-K3",
                List.of("abstract-algebra", "mathematics", "group-theory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-143", "515.35 DEQ", "Differential Equations & Linear Algebra (4th Ed)", "C. Henry Edwards & David E. Penney",
                "First-order equations, linear systems, eigenvalues, Laplace transforms, Fourier series, and boundary value problems.",
                "https://covers.openlibrary.org/b/isbn/9780134491431-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-K4",
                List.of("differential-equations", "linear-algebra", "math"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-144", "519.5 PROB", "Probability & Random Processes for Electrical Engineering", "Alberto Leon-Garcia",
                "Probability spaces, random variables, Markov chains, spectral density, stationary processes, and queueing theory.",
                "https://covers.openlibrary.org/b/isbn/9780201500882-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-K5",
                List.of("probability", "stochastic", "math"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-145", "621.381 CKT", "Microelectronic Circuits (8th Edition)", "Adel S. Sedra & Kenneth C. Smith",
                "Operational amplifiers, MOSFETs, BJTs, frequency response, feedback amplifiers, and integrated circuit design.",
                "https://covers.openlibrary.org/b/isbn/9780190853464-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-K6",
                List.of("circuits", "electronics", "engineering"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-146", "621.382 SIG", "Signals and Systems (2nd Edition)", "Alan V. Oppenheim & Alan S. Willsky",
                "Continuous-time and discrete-time signals, Fourier transforms, Laplace transforms, z-transforms, and linear time-invariant systems.",
                "https://covers.openlibrary.org/b/isbn/9780138147570-L.jpg", ResourceType.BOOK, 7, 5, "SHELF-L1",
                List.of("signals-systems", "fourier", "dsp"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-147", "621.389 DSP", "Discrete-Time Signal Processing (3rd Edition)", "Alan V. Oppenheim & Ronald W. Schafer",
                "Sampling theory, z-transform analysis, FIR/IIR filter design, Fast Fourier Transform (FFT) algorithms, and multirate DSP.",
                "https://covers.openlibrary.org/b/isbn/9780131988422-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-L2",
                List.of("dsp", "signal-processing", "fft"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-148", "629.8 CTL", "Modern Control Engineering (5th Edition)", "Katsuhiko Ogata",
                "State-space representation, root locus analysis, Bode plots, Nyquist stability criterion, and PID controller design.",
                "https://covers.openlibrary.org/b/isbn/9780136156734-L.jpg", ResourceType.BOOK, 4, 3, "SHELF-L3",
                List.of("control-systems", "robotics", "engineering"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-149", "511.5 GRAPH", "Introduction to Graph Theory (2nd Edition)", "Douglas B. West",
                "Trees, matchings, planarity, colorings, Eulerian and Hamiltonian circuits, network flows, and random graphs.",
                "https://covers.openlibrary.org/b/isbn/9780130144003-L.jpg", ResourceType.BOOK, 5, 4, "SHELF-L4",
                List.of("graph-theory", "discrete-math", "algorithms"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-150", "512.5 LA", "Linear Algebra and Its Applications (6th Edition)", "David C. Lay, Steven R. Lay",
                "Matrix algebra, vector spaces, linear transformations, eigenvalues, singular value decomposition (SVD), and least squares.",
                "https://covers.openlibrary.org/b/isbn/9780135851159-L.jpg", ResourceType.BOOK, 8, 6, "SHELF-L5",
                List.of("linear-algebra", "matrices", "math"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-151", "005.1 DP", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma, Richard Helm (Gang of Four)",
                "The classic Gang of Four reference: Creational, Structural, and Behavioral OOP design patterns.",
                "https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-M1",
                List.of("design-patterns", "gof", "oop"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-152", "005.1 REF", "Refactoring: Improving the Design of Existing Code (2nd Ed)", "Martin Fowler",
                "Catalog of refactorings, code smells, testing techniques, and object-oriented architectural redesign.",
                "https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-M2",
                List.of("refactoring", "clean-code", "fowler"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-153", "005.1 DDD", "Domain-Driven Design: Tackling Complexity in Software", "Eric Evans",
                "Bounded contexts, ubiquitous language, aggregates, entities, value objects, and domain event architectures.",
                "https://covers.openlibrary.org/b/isbn/9780321125217-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-M3",
                List.of("ddd", "architecture", "domain-modeling"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-154", "005.1 ARCH1", "Clean Architecture: A Craftsman's Guide to Software Structure", "Robert C. Martin (Uncle Bob)",
                "Solid design principles, component boundaries, dependency inversion, entity rules, and decoupled framework architectures.",
                "https://covers.openlibrary.org/b/isbn/9780134494166-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-M4",
                List.of("clean-architecture", "solid-principles", "software-design"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-155", "006.310 NLP", "Natural Language Processing with Transformers", "Lewis Tunstall, Leandro von Werra",
                "Hugging Face Transformers library: BERT, GPT, T5, fine-tuning sequence classification, text generation, and RAG pipelines.",
                "https://covers.openlibrary.org/b/isbn/9781098103248-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-K2",
                List.of("nlp", "transformers", "huggingface"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-156", "006.310 GNN", "Graph Representation Learning", "William L. Hamilton",
                "Graph Neural Networks (GNNs), node embeddings, GraphSAGE, Graph Attention Networks (GAT), and link prediction algorithms.",
                "https://covers.openlibrary.org/b/isbn/9781681739632-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-K3",
                List.of("gnn", "graph-learning", "ai"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-157", "005.133 TS", "Programming TypeScript", "Boris Cherny",
                "Advanced type system: generic constraints, mapped types, conditional types, algebraic data types, and strict compiler configurations.",
                "https://covers.openlibrary.org/b/isbn/9781491999837-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-L1",
                List.of("typescript", "javascript", "type-system"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-158", "005.276 REACT", "Learning React (2nd Edition)", "Alex Banks & Eve Porcello",
                "Modern React 18: functional components, custom hooks, context state management, server components, and performance optimization.",
                "https://covers.openlibrary.org/b/isbn/9781492051725-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-L2",
                List.of("react", "frontend", "javascript"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-159", "005.1 SRE", "Site Reliability Engineering: How Google Runs Production Systems", "Betsy Beyer, Chris Jones",
                "SLOs, SLIs, error budgets, monitoring, incident management, automation, postmortems, and distributed system reliability.",
                "https://covers.openlibrary.org/b/isbn/9781491929124-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-M5",
                List.of("sre", "devops", "google"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-160", "005.1 BPO", "Building Microservices (2nd Edition)", "Sam Newman",
                "Microservice decomposition, API gateways, service mesh, saga transactions, distributed logging, and continuous deployment.",
                "https://covers.openlibrary.org/b/isbn/9781492034025-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-M6",
                List.of("microservices", "architecture", "distributed-systems"), ResourceStatus.AVAILABLE);

        log.info("LIBRARIX MongoDB 60-items dataset initialization complete. Total items in DB: {}", resourceRepository.count());
    }

    private void seedResourceIfMissing(String id, String barcode, String title, String authorOrBrand,
                                        String description, String coverImageUrl, ResourceType type,
                                        int totalQuantity, int availableQuantity, String location,
                                        List<String> tags, ResourceStatus status) {
        if (!resourceRepository.existsByBarcode(barcode)) {
            Resource r = Resource.builder()
                    .id(id)
                    .barcode(barcode)
                    .title(title)
                    .authorOrBrand(authorOrBrand)
                    .description(description)
                    .coverImageUrl(coverImageUrl)
                    .type(type)
                    .totalQuantity(totalQuantity)
                    .availableQuantity(availableQuantity)
                    .location(location)
                    .tags(tags)
                    .status(status)
                    .createdAt(Instant.now())
                    .build();
            resourceRepository.save(r);
            log.info("Inserted new dataset item into MongoDB: [{}] {}", barcode, title);
        }
    }
}
