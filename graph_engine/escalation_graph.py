"""
SaaS Helpdesk Escalation Graph Engine (ADSA Unit 2: Shortest Path & DAG Routing)
Implements Dijkstra's algorithm, Topological Sorting, and Dynamic Load Weighting.
"""

import heapq
from typing import Dict, List, Tuple, Optional, Any

class EscalationGraph:
    """
    Directed Acyclic Graph (DAG) representing multi-tier support agent hierarchy.
    Vertices = Support Agents / Specialist Tiers
    Edges = Allowed escalation transitions with dynamic cost weights
    Cost Weight = Base Resolution Time (minutes) * (1.0 + Current Agent Workload / Max Capacity)
    """

    def __init__(self):
        # Adjacency list: node_id -> List of (neighbor_id, base_latency_minutes)
        self.adj_list: Dict[str, List[Tuple[str, float]]] = {}
        # Node metadata: node_id -> details dict
        self.nodes: Dict[str, Dict[str, Any]] = {}

    def add_node(self, node_id: str, name: str, tier: str, specialization: str, current_load: int, max_capacity: int):
        self.nodes[node_id] = {
            "node_id": node_id,
            "name": name,
            "tier": tier,
            "specialization": specialization,
            "current_load": current_load,
            "max_capacity": max_capacity,
            "is_available": current_load < max_capacity
        }
        if node_id not in self.adj_list:
            self.adj_list[node_id] = []

    def add_edge(self, from_node: str, to_node: str, base_latency_min: float):
        if from_node not in self.nodes or to_node not in self.nodes:
            raise ValueError(f"Both nodes {from_node} and {to_node} must exist before adding edge.")
        self.adj_list[from_node].append((to_node, base_latency_min))

    def update_agent_load(self, node_id: str, delta: int):
        if node_id in self.nodes:
            new_load = max(0, self.nodes[node_id]["current_load"] + delta)
            self.nodes[node_id]["current_load"] = new_load
            self.nodes[node_id]["is_available"] = new_load < self.nodes[node_id]["max_capacity"]

    def compute_edge_weight(self, from_node: str, to_node: str, base_latency: float) -> float:
        """
        Calculates dynamic edge weight incorporating the destination agent's queue congestion.
        Formula: weight = base_latency * (1 + 1.5 * (target_load / max_capacity))
        """
        target = self.nodes[to_node]
        if not target["is_available"] or target["current_load"] >= target["max_capacity"]:
            return float('inf') # Penalty for saturated agents
        
        load_ratio = target["current_load"] / max(1, target["max_capacity"])
        dynamic_weight = base_latency * (1.0 + 1.5 * load_ratio)
        return round(dynamic_weight, 2)

    def is_dag(self) -> bool:
        """
        Validates if graph is a Directed Acyclic Graph (DAG) using Kahn's algorithm (Topological Sort).
        Returns True if no directed cycles exist.
        """
        in_degree = {u: 0 for u in self.nodes}
        for u in self.adj_list:
            for v, _ in self.adj_list[u]:
                in_degree[v] = in_degree.get(v, 0) + 1

        queue = [u for u, deg in in_degree.items() if deg == 0]
        visited_count = 0

        while queue:
            u = queue.pop(0)
            visited_count += 1
            for v, _ in self.adj_list[u]:
                in_degree[v] -= 1
                if in_degree[v] == 0:
                    queue.append(v)

        return visited_count == len(self.nodes)

    def dijkstra_optimal_escalation(self, start_node: str, target_specialization: Optional[str] = None) -> Dict[str, Any]:
        """
        Runs Dijkstra's shortest path algorithm from start_node.
        Finds the lowest cost path to an eligible specialist agent.
        """
        if start_node not in self.nodes:
            return {"success": False, "error": f"Start node {start_node} not found"}

        # min-priority queue storing tuples: (accumulated_cost, current_node, path)
        pq = [(0.0, start_node, [start_node])]
        visited = set()
        best_cost = {node: float('inf') for node in self.nodes}
        best_cost[start_node] = 0.0

        target_candidates = []

        while pq:
            curr_cost, u, path = heapq.heappop(pq)

            if u in visited:
                continue
            visited.add(u)

            # Check if this node is an eligible target (different tier from start, matches specialization if requested)
            if u != start_node:
                node_info = self.nodes[u]
                spec_match = (target_specialization is None) or (node_info["specialization"] == target_specialization)
                if spec_match and node_info["is_available"]:
                    target_candidates.append({
                        "destination_agent": u,
                        "agent_name": node_info["name"],
                        "tier": node_info["tier"],
                        "specialization": node_info["specialization"],
                        "total_latency_score": curr_cost,
                        "escalation_path": path,
                        "current_load": node_info["current_load"]
                    })

            for v, base_lat in self.adj_list.get(u, []):
                weight = self.compute_edge_weight(u, v, base_lat)
                if weight == float('inf'):
                    continue
                new_cost = curr_cost + weight
                if new_cost < best_cost[v]:
                    best_cost[v] = new_cost
                    heapq.heappush(pq, (new_cost, v, path + [v]))

        # Sort candidate routes by lowest total dynamic latency
        target_candidates.sort(key=lambda x: x["total_latency_score"])

        if not target_candidates:
            return {
                "success": False,
                "start_agent": start_node,
                "message": "No available escalation path found (all candidate agents saturated or unreachable)",
                "optimal_route": None
            }

        return {
            "success": True,
            "start_agent": start_node,
            "optimal_route": target_candidates[0],
            "all_evaluated_candidates": target_candidates
        }

def build_default_saas_hierarchy() -> EscalationGraph:
    """Builds the canonical enterprise support escalation topology."""
    g = EscalationGraph()

    # Tier L1 General Triage
    g.add_node("A1", "Ramesh Patel (L1 General)", "L1", "General", current_load=3, max_capacity=10)
    g.add_node("A2", "Sneha Kulkarni (L1 General)", "L1", "General", current_load=5, max_capacity=10)

    # Tier L2 Domain Specialists
    g.add_node("A3", "Kiran Kumar (L2 Tech)", "L2", "Technical", current_load=2, max_capacity=8)
    g.add_node("A4", "Deepa Nair (L2 Billing)", "L2", "Billing", current_load=1, max_capacity=8)
    g.add_node("A5", "Manish Joshi (L2 Auth)", "L2", "Auth_Access", current_load=4, max_capacity=8)

    # Tier L3 Senior Staff
    g.add_node("A6", "Dr. Arvind Sen (L3 Tech Staff)", "L3", "Technical", current_load=2, max_capacity=6)
    g.add_node("A7", "Lakshmi Narayanan (L3 Finance)", "L3", "Billing", current_load=1, max_capacity=6)

    # Tier Specialist Lead / Architect
    g.add_node("A8", "Venkatesh Iyer (Principal Architect)", "Specialist_Lead", "Technical", current_load=1, max_capacity=5)

    # Directed Escalation Edges: (From -> To, base_resolution_minutes)
    # L1 -> L2
    g.add_edge("A1", "A3", base_latency_min=15.0) # L1 to L2 Tech
    g.add_edge("A1", "A4", base_latency_min=10.0) # L1 to L2 Billing
    g.add_edge("A1", "A5", base_latency_min=12.0) # L1 to L2 Auth
    g.add_edge("A2", "A3", base_latency_min=15.0)
    g.add_edge("A2", "A4", base_latency_min=10.0)
    g.add_edge("A2", "A5", base_latency_min=12.0)

    # L2 -> L3
    g.add_edge("A3", "A6", base_latency_min=25.0) # L2 Tech to L3 Tech
    g.add_edge("A4", "A7", base_latency_min=20.0) # L2 Billing to L3 Billing
    g.add_edge("A5", "A6", base_latency_min=25.0) # L2 Auth to L3 Tech

    # L3 -> Specialist Lead
    g.add_edge("A6", "A8", base_latency_min=40.0)
    g.add_edge("A7", "A8", base_latency_min=45.0)

    # Fast-track edge for emergency bypass
    g.add_edge("A1", "A6", base_latency_min=60.0) # Direct L1 to L3 bypass under critical priority

    return g

if __name__ == "__main__":
    graph = build_default_saas_hierarchy()
    print("Graph built. Is DAG (Acyclic)?", graph.is_dag())

    print("\n--- Test 1: Escalating Technical Issue from L1 Agent A1 ---")
    res_tech = graph.dijkstra_optimal_escalation(start_node="A1", target_specialization="Technical")
    print("Optimal route:", res_tech["optimal_route"])

    print("\n--- Test 2: Escalating Billing Dispute from L1 Agent A2 ---")
    res_bill = graph.dijkstra_optimal_escalation(start_node="A2", target_specialization="Billing")
    print("Optimal route:", res_bill["optimal_route"])

    print("\n--- Test 3: Simulating Congestion on A3 (L2 Tech) by adding +6 load ---")
    graph.update_agent_load("A3", delta=6)
    res_congested = graph.dijkstra_optimal_escalation(start_node="A1", target_specialization="Technical")
    print("New optimal route after congestion:", res_congested["optimal_route"])
