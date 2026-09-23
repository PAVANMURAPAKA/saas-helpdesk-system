package com.helpdesk.graph;

import com.helpdesk.agent.Agent;

import java.util.*;

/**
 * ADSA Unit 2: Graph Engine implementing Dijkstra's Shortest Path Algorithm
 * and Directed Acyclic Graph (DAG) topological validation.
 */
public class EscalationGraph {

    public static class Edge {
        private final int toAgentId;
        private final double baseLatencyMinutes;

        public Edge(int toAgentId, double baseLatencyMinutes) {
            this.toAgentId = toAgentId;
            this.baseLatencyMinutes = baseLatencyMinutes;
        }

        public int getToAgentId() { return toAgentId; }
        public double getBaseLatencyMinutes() { return baseLatencyMinutes; }
    }

    public static class EscalationRoute {
        private final int destinationAgentId;
        private final double totalLatencyScore;
        private final List<Integer> path;

        public EscalationRoute(int destinationAgentId, double totalLatencyScore, List<Integer> path) {
            this.destinationAgentId = destinationAgentId;
            this.totalLatencyScore = totalLatencyScore;
            this.path = path;
        }

        public int getDestinationAgentId() { return destinationAgentId; }
        public double getTotalLatencyScore() { return totalLatencyScore; }
        public List<Integer> getPath() { return path; }

        @Override
        public String toString() {
            return String.format("Route -> Agent #%d | Score: %.2f mins | Path: %s", 
                    destinationAgentId, totalLatencyScore, path);
        }
    }

    private static class QueueNode implements Comparable<QueueNode> {
        final int agentId;
        final double cost;
        final List<Integer> path;

        QueueNode(int agentId, double cost, List<Integer> path) {
            this.agentId = agentId;
            this.cost = cost;
            this.path = path;
        }

        @Override
        public int compareTo(QueueNode other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    private final Map<Integer, Agent> agents = new HashMap<>();
    private final Map<Integer, List<Edge>> adjacencyList = new HashMap<>();

    public void addAgent(Agent agent) {
        agents.put(agent.getAgentId(), agent);
        adjacencyList.putIfAbsent(agent.getAgentId(), new ArrayList<>());
    }

    public void addEscalationEdge(int fromAgentId, int toAgentId, double baseLatencyMinutes) {
        if (!agents.containsKey(fromAgentId) || !agents.containsKey(toAgentId)) {
            throw new IllegalArgumentException("Both agents must be added to graph before linking edge.");
        }
        adjacencyList.get(fromAgentId).add(new Edge(toAgentId, baseLatencyMinutes));
    }

    /**
     * Dynamic weight calculation incorporating destination agent workload.
     * Dynamic Cost = baseLatency * (1.0 + 1.5 * (currentLoad / maxCapacity))
     */
    public double calculateDynamicWeight(Edge edge) {
        Agent target = agents.get(edge.getToAgentId());
        if (target == null || !target.canAcceptTicket()) {
            return Double.POSITIVE_INFINITY;
        }
        double loadRatio = (double) target.getCurrentLoad() / (double) Math.max(1, target.getMaxCapacity());
        return edge.getBaseLatencyMinutes() * (1.0 + 1.5 * loadRatio);
    }

    /**
     * Dijkstra's Algorithm for finding the optimal lowest latency route
     * to a specialized agent matching the required domain.
     */
    public Optional<EscalationRoute> findOptimalEscalationRoute(int startAgentId, String requiredSpecialization) {
        if (!agents.containsKey(startAgentId)) {
            return Optional.empty();
        }

        PriorityQueue<QueueNode> pq = new PriorityQueue<>();
        Map<Integer, Double> minCost = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        for (int id : agents.keySet()) {
            minCost.put(id, Double.POSITIVE_INFINITY);
        }

        minCost.put(startAgentId, 0.0);
        pq.offer(new QueueNode(startAgentId, 0.0, Collections.singletonList(startAgentId)));

        EscalationRoute bestRoute = null;
        double bestRouteScore = Double.POSITIVE_INFINITY;

        while (!pq.isEmpty()) {
            QueueNode current = pq.poll();
            int u = current.agentId;

            if (visited.contains(u)) continue;
            visited.add(u);

            // Check if this node is an eligible destination (different from start and matches specialization)
            if (u != startAgentId) {
                Agent agent = agents.get(u);
                boolean matchesSpec = (requiredSpecialization == null) || 
                                      requiredSpecialization.equalsIgnoreCase(agent.getSpecialization());
                if (matchesSpec && agent.canAcceptTicket()) {
                    if (current.cost < bestRouteScore) {
                        bestRouteScore = current.cost;
                        bestRoute = new EscalationRoute(u, current.cost, current.path);
                    }
                }
            }

            for (Edge edge : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                int v = edge.getToAgentId();
                double weight = calculateDynamicWeight(edge);
                if (Double.isInfinite(weight)) continue;

                double nextCost = current.cost + weight;
                if (nextCost < minCost.get(v)) {
                    minCost.put(v, nextCost);
                    List<Integer> newPath = new ArrayList<>(current.path);
                    newPath.add(v);
                    pq.offer(new QueueNode(v, nextCost, newPath));
                }
            }
        }

        return Optional.ofNullable(bestRoute);
    }

    /**
     * DAG cycle validation using Kahn's topological sort algorithm.
     */
    public boolean isDirectedAcyclicGraph() {
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int id : agents.keySet()) {
            inDegree.put(id, 0);
        }

        for (List<Edge> edges : adjacencyList.values()) {
            for (Edge e : edges) {
                inDegree.put(e.getToAgentId(), inDegree.getOrDefault(e.getToAgentId(), 0) + 1);
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        int count = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            count++;
            for (Edge e : adjacencyList.getOrDefault(u, Collections.emptyList())) {
                int v = e.getToAgentId();
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.offer(v);
                }
            }
        }

        return count == agents.size();
    }

    public Agent getAgent(int agentId) {
        return agents.get(agentId);
    }

    public Map<Integer, Agent> getAllAgents() {
        return Collections.unmodifiableMap(agents);
    }
}
