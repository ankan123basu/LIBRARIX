package com.librarix.graph;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Library Multi-Pickup Route Optimizer — Dijkstra's Shortest Path + Nearest-Neighbor TSP Heuristic
 *
 * DSA: Dijkstra's algorithm (min-heap priority queue), weighted undirected graph, greedy TSP approximation.
 *
 * Problem: When a student has 4 books to pick up from different shelf locations
 * (e.g., SHELF-A1, SHELF-C3, SHELF-D2, SHELF-B1), what is the shortest walking route?
 *
 * Algorithm:
 *   1. Model the library as a weighted undirected graph (nodes = shelf zones, edges = walking distances in meters).
 *   2. Run Dijkstra from each pickup location to every other → pairwise distance matrix.
 *   3. Apply nearest-neighbor heuristic on the distance matrix to produce a near-optimal route.
 *   4. Return the ordered route + total walking distance.
 *
 * Time Complexity:
 *   - Dijkstra per source: O((V + E) log V) with min-heap
 *   - Pairwise for k pickups: O(k × (V + E) log V)
 *   - Nearest-neighbor: O(k²)
 *   - Total: O(k × (V + E) log V) where V = shelf zones, E = adjacency edges, k = pickup count
 */
@Slf4j
@Service
public class LibraryRouteOptimizer {

    // Adjacency list: graph[node] = list of (neighbor, distance_meters)
    private final Map<String, List<Edge>> graph = new HashMap<>();

    private record Edge(String to, double distance) {}

    public LibraryRouteOptimizer() {
        buildLibraryGraph();
    }

    /**
     * Build a realistic library floor plan graph.
     * 12 shelf zones arranged in 2 rows of 6, with entrance at ENTRANCE.
     *
     * Layout:
     *   ENTRANCE
     *     |
     *   A -- B -- C -- D -- E -- F    (Row 1, front)
     *   |    |    |    |    |    |
     *   G -- H -- I -- J -- K -- L    (Row 2, back)
     *
     * Horizontal distance between adjacent zones: 3.0m
     * Vertical distance between rows: 4.0m
     * Entrance to Row 1: 2.0m
     */
    private void buildLibraryGraph() {
        String[] row1 = {"SHELF-A", "SHELF-B", "SHELF-C", "SHELF-D", "SHELF-E", "SHELF-F"};
        String[] row2 = {"SHELF-G", "SHELF-H", "SHELF-I", "SHELF-J", "SHELF-K", "SHELF-L"};

        // Horizontal edges within Row 1
        for (int i = 0; i < row1.length - 1; i++) {
            addEdge(row1[i], row1[i + 1], 3.0);
        }
        // Horizontal edges within Row 2
        for (int i = 0; i < row2.length - 1; i++) {
            addEdge(row2[i], row2[i + 1], 3.0);
        }
        // Vertical edges between rows
        for (int i = 0; i < row1.length; i++) {
            addEdge(row1[i], row2[i], 4.0);
        }
        // Entrance connects to first zone in each row
        addEdge("ENTRANCE", "SHELF-A", 2.0);
        addEdge("ENTRANCE", "SHELF-G", 5.0); // Diagonal path to back row
    }

    private void addEdge(String from, String to, double distance) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, distance));
        graph.computeIfAbsent(to, k -> new ArrayList<>()).add(new Edge(from, distance));
    }

    /**
     * Compute the shortest walking route to pick up books from the given shelf locations.
     * Starts and ends at ENTRANCE.
     *
     * @param locations List of shelf zone IDs (e.g., ["SHELF-A", "SHELF-C", "SHELF-J"])
     * @return RouteResult with ordered stops and total distance
     */
    public RouteResult computeOptimalRoute(List<String> locations) {
        if (locations == null || locations.isEmpty()) {
            return new RouteResult(List.of(), 0.0);
        }

        // Validate all locations exist in the graph
        List<String> validLocations = locations.stream()
                .filter(graph::containsKey)
                .toList();

        if (validLocations.isEmpty()) {
            return new RouteResult(List.of(), 0.0);
        }

        // Add ENTRANCE as the starting point
        List<String> allStops = new ArrayList<>();
        allStops.add("ENTRANCE");
        allStops.addAll(validLocations);

        // Step 1: Compute pairwise shortest distances using Dijkstra
        Map<String, Map<String, Double>> pairwise = new HashMap<>();
        for (String stop : allStops) {
            pairwise.put(stop, dijkstra(stop));
        }

        // Step 2: Nearest-neighbor heuristic starting from ENTRANCE
        List<String> route = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String current = "ENTRANCE";
        route.add(current);
        visited.add(current);
        double totalDistance = 0.0;

        while (visited.size() < allStops.size()) {
            String nearest = null;
            double minDist = Double.MAX_VALUE;

            for (String stop : allStops) {
                if (!visited.contains(stop)) {
                    double dist = pairwise.getOrDefault(current, Map.of()).getOrDefault(stop, Double.MAX_VALUE);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = stop;
                    }
                }
            }

            if (nearest != null) {
                route.add(nearest);
                visited.add(nearest);
                totalDistance += minDist;
                current = nearest;
            } else {
                break; // Unreachable stop
            }
        }

        // Return to ENTRANCE
        double returnDist = pairwise.getOrDefault(current, Map.of()).getOrDefault("ENTRANCE", 0.0);
        totalDistance += returnDist;
        route.add("ENTRANCE");

        log.info("Route optimized: {} stops, total distance: {}m", validLocations.size(), String.format("%.1f", totalDistance));
        return new RouteResult(route, totalDistance);
    }

    /**
     * Dijkstra's Shortest Path from a source node to all reachable nodes.
     *
     * @param source Starting node
     * @return Map of node -> shortest distance from source
     */
    public Map<String, Double> dijkstra(String source) {
        Map<String, Double> dist = new HashMap<>();
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[0]));
        Map<String, Integer> nodeIndex = new HashMap<>();
        List<String> nodeList = new ArrayList<>(graph.keySet());
        for (int i = 0; i < nodeList.size(); i++) {
            nodeIndex.put(nodeList.get(i), i);
            dist.put(nodeList.get(i), Double.MAX_VALUE);
        }

        dist.put(source, 0.0);
        // pq entries: [distance, nodeIndex]
        pq.offer(new double[]{0.0, nodeIndex.getOrDefault(source, -1)});

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            double[] top = pq.poll();
            double d = top[0];
            int idx = (int) top[1];
            if (idx < 0 || idx >= nodeList.size()) continue;
            String u = nodeList.get(idx);

            if (visited.contains(u)) continue;
            visited.add(u);

            List<Edge> neighbors = graph.getOrDefault(u, List.of());
            for (Edge edge : neighbors) {
                double newDist = d + edge.distance;
                if (newDist < dist.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    dist.put(edge.to, newDist);
                    pq.offer(new double[]{newDist, nodeIndex.getOrDefault(edge.to, -1)});
                }
            }
        }

        return dist;
    }

    /**
     * Compute naive sequential route distance (alphabetical order).
     */
    public double computeNaiveRouteDistance(List<String> locations) {
        if (locations == null || locations.isEmpty()) return 0.0;

        List<String> sorted = new ArrayList<>(locations);
        Collections.sort(sorted);

        Map<String, Double> entranceDist = dijkstra("ENTRANCE");
        double total = entranceDist.getOrDefault(sorted.get(0), 0.0);

        for (int i = 0; i < sorted.size() - 1; i++) {
            Map<String, Double> dists = dijkstra(sorted.get(i));
            total += dists.getOrDefault(sorted.get(i + 1), 0.0);
        }

        Map<String, Double> lastDist = dijkstra(sorted.get(sorted.size() - 1));
        total += lastDist.getOrDefault("ENTRANCE", 0.0);

        return total;
    }

    public Set<String> getAvailableZones() {
        return graph.keySet();
    }

    public record RouteResult(List<String> route, double totalDistanceMeters) {}
}
