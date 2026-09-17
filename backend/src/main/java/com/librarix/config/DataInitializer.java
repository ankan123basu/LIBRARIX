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

        seedResourceIfMissing("res-104", "621.381 OSC", "Rigol DS1054Z 50MHz Digital Storage Oscilloscope", "Rigol Technologies",
                "4-channel digital oscilloscope with 1GSa/s sampling rate, 12Mpts memory depth, and SPI/I2C protocol decoder module.",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 2, 2, "LAB-ROOM-204",
                List.of("hardware", "electronics", "oscilloscope"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-105", "621.395 FPGA", "Xilinx Artix-7 FPGA Nexys A7 Trainer Kit", "Digilent / Xilinx",
                "FPGA development board with 101,440 logic cells, 240 DSP slices, 15.8 Mb BRAM, PMOD expanders, and VGA output.",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 3, 0, "LAB-ROOM-204",
                List.of("lab-kit", "fpga", "verilog"), ResourceStatus.BORROWED);

        seedResourceIfMissing("res-106", "371.330 SEM", "Capstone Seminar Room 402B (8-Person Capacity)", "Campus Library Facilities",
                "Acoustically dampened seminar room equipped with 4K interactive touchscreen display, dual whiteboards, and HDMI presentation hub.",
                "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "FLOOR-4",
                List.of("seminar-room", "collaboration", "study"), ResourceStatus.AVAILABLE);

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

        seedResourceIfMissing("res-113", "621.398 RPI", "Raspberry Pi 5 (8GB) Edge Computing Starter Kit", "Raspberry Pi Foundation",
                "Quad-core ARM Cortex-A76 board with active cooler, NVMe M.2 HAT, 64GB microSD, and camera module for IoT edge AI.",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 4, 3, "LAB-ROOM-204",
                List.of("hardware", "raspberry-pi", "iot", "edge-ai"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-114", "621.399 ARD", "Arduino Mega 2560 Sensor & Mechatronics Kit", "Arduino Official",
                "ATmega2560 board with 54 digital I/O pins, 16 analog inputs, stepper motor drivers, ultrasonic sensors, and LCD shield.",
                "https://images.unsplash.com/photo-1553406830-ef2513450d76?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 5, 4, "LAB-ROOM-204",
                List.of("lab-kit", "arduino", "mechatronics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-115", "621.382 DMM", "Keysight 34461A 6½ Digit Precision Multimeter", "Keysight Technologies",
                "Truevolt benchtop digital multimeter with 0.0035% DC accuracy, LAN/USB connectivity, and trend chart graphing display.",
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 2, 1, "LAB-ROOM-205",
                List.of("hardware", "multimeter", "precision"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-116", "371.331 ROB", "Robotics Lab Project Pod 104 (12-Person Capacity)", "Campus Engineering Facilities",
                "High-clearance robotics development bay equipped with overhead power drop cords, soldering station, 3D printer, and Ethernet drops.",
                "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "BUILDING-4",
                List.of("seminar-room", "robotics", "workshop"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-117", "005.131 CSAPP", "Computer Systems: A Programmer's Perspective", "Randal E. Bryant & David R. O'Hallaron",
                "Machine-level code execution, processor architecture, virtual memory, dynamic memory allocation, and concurrent programming.",
                "https://covers.openlibrary.org/b/isbn/9780134092669-L.jpg", ResourceType.BOOK, 7, 5, "SHELF-D1",
                List.of("cs-systems", "c-programming", "memory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-118", "006.312 PDS", "Python Data Science Handbook (2nd Ed)", "Jake VanderPlas",
                "Essential tools for working with data in Python: IPython, NumPy, Pandas, Matplotlib, and Scikit-Learn pipelines.",
                "https://covers.openlibrary.org/b/isbn/9781098115784-L.jpg", ResourceType.BOOK, 6, 4, "SHELF-D2",
                List.of("python", "data-science", "pandas"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-119", "621.388 LOG", "Saleae Logic Pro 16 USB Logic Analyzer Kit", "Saleae Inc",
                "16-channel high-speed logic analyzer with 500MS/s digital sampling, analog recording, and SPI/I2C/CAN bus decoding.",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 3, 2, "LAB-ROOM-205",
                List.of("hardware", "logic-analyzer", "electronics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-120", "006.800 VR", "Meta Quest 3 Spatial Computing & VR Dev Kit", "Meta Reality Labs",
                "Mixed-reality spatial computing head-mounted display with dual RGB pass-through cameras, Snapdragon XR2 Gen 2, and Unity/Unreal SDKs.",
                "https://images.unsplash.com/photo-1593508512255-86ab42a8e620?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 2, 1, "LAB-ROOM-301",
                List.of("lab-kit", "vr", "spatial-computing", "unity"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-121", "005.430 OS", "Operating System Concepts (10th Edition)", "Abraham Silberschatz, Peter B. Galvin",
                "Process synchronization, virtual memory management, file systems, I/O subsystems, virtual machines, and security protection.",
                "https://covers.openlibrary.org/b/isbn/9781118063330-L.jpg", ResourceType.BOOK, 8, 4, "SHELF-D3",
                List.of("operating-systems", "kernel", "memory"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-122", "005.453 CMP", "Compilers: Principles, Techniques, and Tools", "Alfred V. Aho, Monica S. Lam",
                "Lexical analysis, syntax-directed translation, intermediate code generation, run-time environments, and code optimization.",
                "https://covers.openlibrary.org/b/isbn/9780321486813-L.jpg", ResourceType.BOOK, 5, 3, "SHELF-E1",
                List.of("compilers", "parsing", "optimization"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-123", "005.740 SQL", "High Performance MySQL (4th Edition)", "Silvia Botros & Jeremy Tinley",
                "Optimization, backups, replication, scaling, indexing, InnoDB engine internals, and query execution profiling.",
                "https://covers.openlibrary.org/b/isbn/9781492080519-L.jpg", ResourceType.BOOK, 4, 2, "SHELF-E2",
                List.of("database", "mysql", "sql-tuning"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-124", "006.310 PRML", "Pattern Recognition and Machine Learning", "Christopher M. Bishop",
                "Comprehensive introduction to Bayesian methods, linear classification, neural networks, kernel methods, and graphical models.",
                "https://covers.openlibrary.org/b/isbn/9780387310732-L.jpg", ResourceType.BOOK, 6, 3, "SHELF-E3",
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
                List.of("computer-architecture", "hardware", "processors"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-141", "621.381 FUN", "Siglent SDG1032X 30MHz Function Generator", "Siglent Technologies",
                "Dual-channel arbitrary waveform generator with 150MSa/s sampling rate, 14-bit vertical resolution, and sweep burst functions.",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 3, 2, "LAB-ROOM-205",
                List.of("hardware", "signal-generator", "electronics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-142", "621.398 ESP", "ESP32-WROOM-32D Dual-Core Wi-Fi & Bluetooth IoT Kit", "Espressif Systems",
                "Dual-core 240MHz microcontroller with integrated Wi-Fi, BLE, capacitive touch sensors, Hall sensors, and FreeRTOS support.",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 8, 6, "LAB-ROOM-204",
                List.of("lab-kit", "esp32", "iot", "freertos"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-143", "621.399 STM", "STM32F407G-DISC1 ARM Cortex-M4 Discovery Kit", "STMicroelectronics",
                "ARM Cortex-M4 168MHz MCU with FPU, ST-LINK/V2-1 debugger, MEMS motion sensor, digital microphone, and audio DAC.",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 4, 3, "LAB-ROOM-204",
                List.of("lab-kit", "stm32", "arm", "embedded"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-144", "621.382 PWR", "Korad KA3005D Programmable DC Power Supply (30V 5A)", "Korad Technology",
                "Precision regulated DC bench power supply with 10mV/1mA resolution, constant current/voltage modes, and OVP/OCP protection.",
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 4, 3, "LAB-ROOM-205",
                List.of("hardware", "power-supply", "bench-equipment"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-145", "621.389 SLD", "Weller WE1010NA Digital Soldering Station & ESD Kit", "Weller Soldering",
                "70W digital soldering station with temperature lock, automatic standby, ESD safe iron handle, and brass wire tip cleaner.",
                "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 5, 4, "LAB-ROOM-206",
                List.of("lab-kit", "soldering", "workbench"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-146", "621.395 ANK", "Anker 737 Power Bank 140W Portable Lab Charger", "Anker Innovations",
                "24,000mAh portable power bank with USB Power Delivery 3.1 140W output, smart digital display, and multi-device charging.",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 6, 5, "LAB-ROOM-204",
                List.of("hardware", "power-bank", "lab-accessories"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-147", "621.381 PROB", "Tektronix TPP0200 200MHz 10X Passive Probes (Set of 4)", "Tektronix Instrumentation",
                "200MHz bandwidth passive voltage probes with 10X attenuation, 300V CAT II safety rating, and grounding alligator clips.",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 4, 3, "LAB-ROOM-205",
                List.of("hardware", "probes", "oscilloscope"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-148", "621.399 SENS", "SunFounder 37-in-1 Sensor Modules Kit", "SunFounder Education",
                "Comprehensive sensor kit including ultrasonic distance, DHT11 temp/humidity, GY-521 gyro/accel, IR obstacle, and relay modules.",
                "https://images.unsplash.com/photo-1553406830-ef2513450d76?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 6, 5, "LAB-ROOM-204",
                List.of("lab-kit", "sensors", "electronics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-149", "621.388 SMR", "Hakko FA-400 Benchtop Smoke Absorber Fume Extractor", "Hakko Corporation",
                "Compact ESD-safe benchtop soldering fume extractor with activated carbon filter for removing soldering smoke flux toxins.",
                "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 4, 4, "LAB-ROOM-206",
                List.of("lab-kit", "safety", "soldering"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-150", "621.395 FLIR", "FLIR E4 Compact Thermal Imaging Camera with MSX", "FLIR Systems",
                "80x60 thermal infrared camera resolution with MSX image enhancement for inspecting PCB thermal hot spots and component shorts.",
                "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 2, 1, "LAB-ROOM-205",
                List.of("hardware", "thermal-camera", "pcb-diagnostics"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-151", "371.330 SEM1", "Capstone Seminar Room 401A (6-Person Capacity)", "Campus Facilities",
                "Acoustic glass seminar room equipped with 55-inch 4K screen, HDMI input, whiteboards, and quiet study ventilation.",
                "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "BUILDING-3",
                List.of("seminar-room", "group-study"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-152", "371.330 SEM3", "Capstone Seminar Room 403C (10-Person Capacity)", "Campus Facilities",
                "Large teamwork room with modular conference tables, dual whiteboards, 4K screen with wireless AirPlay/Miracast casting.",
                "https://images.unsplash.com/photo-1517502884422-41eaead166d4?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "BUILDING-3",
                List.of("seminar-room", "presentation"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-153", "371.330 SEM4", "AI Research Lab Conference Suite 501 (16-Person Capacity)", "Engineering Faculty Facilities",
                "High-tier conference room with dual laser projectors, surround audio, ceiling mic array, and video conferencing hub.",
                "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "BUILDING-5",
                List.of("seminar-room", "conference", "faculty"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-154", "371.331 VR2", "Spatial Computing VR Sandbox Pod 202 (8-Person Capacity)", "Interactive Media Department",
                "Padded floor VR testing bay equipped with ceiling lighthouse motion trackers, high-end GPU workstations, and spatial audio.",
                "https://images.unsplash.com/photo-1593508512255-86ab42a8e620?auto=format&fit=crop&w=600&q=80", ResourceType.SEMINAR_ROOM, 1, 1, "BUILDING-2",
                List.of("seminar-room", "vr-sandbox", "media"), ResourceStatus.AVAILABLE);

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

        seedResourceIfMissing("res-159", "621.398 CAN", "PEAK PCAN-USB CAN Bus Adapter Kit for Automotive & Robotics", "PEAK-System Technik",
                "High-speed CAN bus 2.0A/B to USB interface adapter with galvanic isolation, DB9 connector, and PCAN-View monitoring software.",
                "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=600&q=80", ResourceType.HARDWARE, 3, 2, "LAB-ROOM-205",
                List.of("hardware", "can-bus", "robotics", "automotive"), ResourceStatus.AVAILABLE);

        seedResourceIfMissing("res-160", "621.399 RTL", "RTL-SDR Blog V4 Software Defined Radio Receiver Kit", "RTL-SDR Blog",
                "SMA software-defined radio receiver with 500kHz - 1.7GHz tuning range, TCXO 1PPM precision clock, and dipole antenna set.",
                "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80", ResourceType.LAB_KIT, 4, 3, "LAB-ROOM-204",
                List.of("lab-kit", "sdr", "rf", "wireless"), ResourceStatus.AVAILABLE);

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
