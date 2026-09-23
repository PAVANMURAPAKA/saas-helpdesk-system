"""
Discrete Mathematics & Graph Theory (DMGT Unit 2) Mathematical Verification Engine
Verifies:
1. Poset (Partially Ordered Set) properties on the Support Agent Hierarchy:
   - Reflexivity: (a, a) in R
   - Antisymmetry: (a, b) in R and (b, a) in R => a == b
   - Transitivity: (a, b) in R and (b, c) in R => (a, c) in R
2. Equivalence Relations on Ticket Partitioning:
   - Equivalence class: [T_i] = { T_j | Category(T_j) == Category(T_i) }
   - Verifies Reflexive, Symmetric, Transitive, and fundamental partition property.
"""

from typing import Set, Tuple, List, Dict, Any

class DMGTVerifier:
    def __init__(self):
        # Support Tier Domain S = {L1, L2, L3, Specialist_Lead}
        self.tiers = {"L1", "L2", "L3", "Specialist_Lead"}
        self.tier_ranks = {
            "L1": 1,
            "L2": 2,
            "L3": 3,
            "Specialist_Lead": 4
        }

    def generate_hierarchy_relation(self) -> Set[Tuple[str, str]]:
        """
        Defines the canonical escalation reachability relation R:
        (a, b) in R  <=>  Rank(a) <= Rank(b)
        """
        relation = set()
        for a in self.tiers:
            for b in self.tiers:
                if self.tier_ranks[a] <= self.tier_ranks[b]:
                    relation.add((a, b))
        return relation

    def verify_reflexivity(self, domain: Set[Any], relation: Set[Tuple[Any, Any]]) -> Tuple[bool, str]:
        for a in domain:
            if (a, a) not in relation:
                return False, f"Reflexivity violated: ({a}, {a}) is missing from relation R."
        return True, "Reflexive property holds: For all a in S, (a, a) in R."

    def verify_antisymmetry(self, relation: Set[Tuple[Any, Any]]) -> Tuple[bool, str]:
        for (a, b) in relation:
            if a != b and (b, a) in relation:
                return False, f"Antisymmetry violated: Both ({a}, {b}) and ({b}, {a}) exist, but {a} != {b}."
        return True, "Antisymmetric property holds: For all a, b in S, ((a, b) in R and (b, a) in R) implies a == b."

    def verify_transitivity(self, relation: Set[Tuple[Any, Any]]) -> Tuple[bool, str]:
        for (a, b) in relation:
            for (c, d) in relation:
                if b == c: # (a, b) and (b, d)
                    if (a, d) not in relation:
                        return False, f"Transitivity violated: ({a}, {b}) and ({b}, {d}) in R, but ({a}, {d}) is missing."
        return True, "Transitive property holds: For all a, b, c in S, ((a, b) in R and (b, c) in R) implies (a, c) in R."

    def verify_symmetry(self, relation: Set[Tuple[Any, Any]]) -> Tuple[bool, str]:
        for (a, b) in relation:
            if (b, a) not in relation:
                return False, f"Symmetry does not hold: ({a}, {b}) in R, but ({b}, {a}) is missing."
        return True, "Symmetric property holds: For all (a, b) in R, (b, a) in R."

    def verify_poset(self) -> Dict[str, Any]:
        """
        Validates whether the escalation hierarchy forms a Poset (Reflexive, Antisymmetric, Transitive).
        """
        R = self.generate_hierarchy_relation()
        ref_ok, ref_msg = self.verify_reflexivity(self.tiers, R)
        anti_ok, anti_msg = self.verify_antisymmetry(R)
        trans_ok, trans_msg = self.verify_transitivity(R)
        is_poset = ref_ok and anti_ok and trans_ok

        return {
            "relation_name": "Tier Escalation Hierarchy Relation (<=)",
            "domain": sorted(list(self.tiers), key=lambda x: self.tier_ranks[x]),
            "relation_cardinality": len(R),
            "is_reflexive": ref_ok,
            "reflexive_proof": ref_msg,
            "is_antisymmetric": anti_ok,
            "antisymmetric_proof": anti_msg,
            "is_transitive": trans_ok,
            "transitive_proof": trans_msg,
            "is_poset": is_poset,
            "hasse_diagram_edges": [
                ("L1", "L2"),
                ("L2", "L3"),
                ("L3", "Specialist_Lead")
            ]
        }

    def verify_ticket_equivalence_partitioning(self, tickets: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Demonstrates Equivalence Relation:
        T_i ~ T_j <=> Category(T_i) == Category(T_j)
        Constructs Equivalence Classes and proves they partition the set of tickets.
        """
        # Relation on ticket IDs
        ticket_ids = {t["ticket_id"] for t in tickets}
        equiv_relation = set()
        for t1 in tickets:
            for t2 in tickets:
                if t1["category"] == t2["category"]:
                    equiv_relation.add((t1["ticket_id"], t2["ticket_id"]))

        ref_ok, _ = self.verify_reflexivity(ticket_ids, equiv_relation)
        sym_ok, _ = self.verify_symmetry(equiv_relation)
        trans_ok, _ = self.verify_transitivity(equiv_relation)
        is_equiv = ref_ok and sym_ok and trans_ok

        # Build equivalence classes [T]
        equiv_classes: Dict[str, List[int]] = {}
        for t in tickets:
            cat = t["category"]
            if cat not in equiv_classes:
                equiv_classes[cat] = []
            equiv_classes[cat].append(t["ticket_id"])

        # Check partition disjointness and completeness
        all_partition_members = []
        for cat, ids in equiv_classes.items():
            all_partition_members.extend(ids)

        is_mutually_disjoint = len(all_partition_members) == len(set(all_partition_members))
        is_exhaustive = set(all_partition_members) == ticket_ids

        return {
            "relation_definition": "T_i ~ T_j <=> Category(T_i) == Category(T_j)",
            "is_reflexive": ref_ok,
            "is_symmetric": sym_ok,
            "is_transitive": trans_ok,
            "is_equivalence_relation": is_equiv,
            "equivalence_classes": equiv_classes,
            "is_valid_partition": is_mutually_disjoint and is_exhaustive
        }

if __name__ == "__main__":
    verifier = DMGTVerifier()
    poset_results = verifier.verify_poset()
    print("=== DMGT Unit 2: Poset Verification ===")
    print(f"Is Poset? {poset_results['is_poset']}")
    print(f"1. {poset_results['reflexive_proof']}")
    print(f"2. {poset_results['antisymmetric_proof']}")
    print(f"3. {poset_results['transitive_proof']}")
    print(f"Hasse Diagram Cover Edges: {poset_results['hasse_diagram_edges']}")

    sample_tickets = [
        {"ticket_id": 101, "category": "Billing"},
        {"ticket_id": 102, "category": "Technical"},
        {"ticket_id": 103, "category": "Billing"},
        {"ticket_id": 104, "category": "Auth_Access"},
        {"ticket_id": 105, "category": "Technical"}
    ]

    equiv_results = verifier.verify_ticket_equivalence_partitioning(sample_tickets)
    print("\n=== DMGT Unit 2: Ticket Equivalence Partitioning ===")
    print(f"Is Equivalence Relation? {equiv_results['is_equivalence_relation']}")
    print(f"Equivalence Classes: {equiv_results['equivalence_classes']}")
    print(f"Partitions Disjoint & Exhaustive? {equiv_results['is_valid_partition']}")
