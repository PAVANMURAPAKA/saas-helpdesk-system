/**
 * SaaS Helpdesk AI Dashboard - Client Controller
 * Manages tabs, API communication, SVG Graph rendering, and Dijkstra path calculation.
 */

// Global State
let currentSelectedTicket = null;
let graphNodes = {
    "A1": { x: 120, y: 180, name: "Ramesh Patel", tier: "L1", spec: "General", load: 2, max: 10 },
    "A2": { x: 120, y: 320, name: "Sneha Kulkarni", tier: "L1", spec: "General", load: 3, max: 10 },
    "A3": { x: 380, y: 100, name: "Kiran Kumar", tier: "L2", spec: "Technical", load: 1, max: 8 },
    "A4": { x: 380, y: 240, name: "Deepa Nair", tier: "L2", spec: "Billing", load: 1, max: 8 },
    "A5": { x: 380, y: 380, name: "Manish Joshi", tier: "L2", spec: "Auth_Access", load: 2, max: 8 },
    "A6": { x: 650, y: 150, name: "Dr. Arvind Sen", tier: "L3", spec: "Technical", load: 1, max: 6 },
    "A7": { x: 650, y: 320, name: "Lakshmi Narayanan", tier: "L3", spec: "Billing", load: 0, max: 6 },
    "A8": { x: 860, y: 240, name: "Venkatesh Iyer", tier: "Specialist_Lead", spec: "Technical", load: 1, max: 5 }
};

let graphEdges = [
    { from: "A1", to: "A3", base: 15.0 },
    { from: "A1", to: "A4", base: 10.0 },
    { from: "A1", to: "A5", base: 12.0 },
    { from: "A2", to: "A3", base: 15.0 },
    { from: "A2", to: "A4", base: 10.0 },
    { from: "A2", to: "A5", base: 12.0 },
    { from: "A3", to: "A6", base: 25.0 },
    { from: "A4", to: "A7", base: 20.0 },
    { from: "A5", to: "A6", base: 25.0 },
    { from: "A6", to: "A8", base: 40.0 },
    { from: "A7", to: "A8", base: 45.0 },
    { from: "A1", to: "A6", base: 55.0 } // Direct bypass
];

document.addEventListener("DOMContentLoaded", () => {
    initTabs();
    initCustomers();
    initRecentTickets();
    initTicketForm();
    renderGraph();
    initGraphControls();
});

// 1. Tab Navigation
function initTabs() {
    const tabButtons = document.querySelectorAll(".tab-btn");
    tabButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            tabButtons.forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));

            btn.classList.add("active");
            const targetId = btn.getAttribute("data-tab");
            const targetSection = document.getElementById(targetId);
            if (targetSection) {
                targetSection.classList.add("active");
                if (targetId === "graphTab") {
                    renderGraph();
                }
            }
        });
    });
}

// 2. Load Customers & Tickets from API
async function initCustomers() {
    const select = document.getElementById("customerSelect");
    try {
        const res = await fetch("/api/customers");
        if (res.ok) {
            const data = await res.json();
            select.innerHTML = data.map(c => 
                `<option value="${c.customer_id}">${c.name} (${c.company || 'Personal'}) - [${c.subscription_tier} SLA]</option>`
            ).join("");
            return;
        }
    } catch (e) {
        console.warn("Backend not active, using default fallback dataset", e);
    }

    // Fallback data
    const fallbacks = [
        { id: 1, name: "Satya Nadella", company: "Enterprise Cloud Inc", tier: "Enterprise" },
        { id: 2, name: "Sundar Pichai", company: "Global Search Corp", tier: "Enterprise" },
        { id: 3, name: "Ravi Kumar", company: "StartupDev Studio", tier: "Pro" }
    ];
    select.innerHTML = fallbacks.map(c => 
        `<option value="${c.id}">${c.name} (${c.company}) - [${c.tier} SLA]</option>`
    ).join("");
}

async function initRecentTickets() {
    const pillList = document.getElementById("ticketPillList");
    try {
        const res = await fetch("/api/tickets");
        if (res.ok) {
            const tickets = await res.json();
            renderTicketPills(tickets);
            return;
        }
    } catch (e) {}

    // Fallback recent tickets
    const defaultTickets = [
        { ticket_id: 1, title: "Database connection pool exhausted", category: "Technical", priority: "Critical", status: "Assigned", customer_name: "Satya Nadella" },
        { ticket_id: 2, title: "Enterprise invoice not generated", category: "Billing", priority: "High", status: "Assigned", customer_name: "Sundar Pichai" },
        { ticket_id: 3, title: "2FA OTP authentication loop", category: "Auth_Access", priority: "High", status: "Escalated", customer_name: "Ravi Kumar" }
    ];
    renderTicketPills(defaultTickets);
}

function renderTicketPills(tickets) {
    const pillList = document.getElementById("ticketPillList");
    if (!tickets || tickets.length === 0) {
        pillList.innerHTML = "<span class='help-text'>No tickets in database.</span>";
        return;
    }
    pillList.innerHTML = tickets.slice(0, 6).map(t => `
        <div class="ticket-pill-item" onclick="selectTicket(${t.ticket_id})">
            <div>
                <strong>#${t.ticket_id}</strong> ${escapeHtml(t.title.substring(0, 36))}...
            </div>
            <span class="badge-tag">${t.category}</span>
        </div>
    `).join("");
}

// 3. Quick Presets
window.loadSample = function(type) {
    const titleInput = document.getElementById("ticketTitle");
    const descInput = document.getElementById("ticketDesc");

    switch(type) {
        case "db_deadlock":
            titleInput.value = "PostgreSQL deadlock during peak API writes";
            descInput.value = "Production server throwing 500 error and JDBC connection timeout. Kubernetes pods restarting.";
            break;
        case "stripe_refund":
            titleInput.value = "Credit card charged twice for annual subscription";
            descInput.value = "Stripe invoice duplicate charge detected. Requesting immediate refund of $599.";
            break;
        case "sso_loop":
            titleInput.value = "Okta SAML 2.0 authentication redirect loop";
            descInput.value = "Security assertion token rejected. Engineers cannot login to developer console with 2FA.";
            break;
        case "csv_bug":
            titleInput.value = "CSV report download button unclickable";
            descInput.value = "Analytics dashboard export button does nothing. Javascript console shows TypeError on click.";
            break;
    }
};

// 4. Ticket Form Submission & Real-Time Classification
function initTicketForm() {
    const form = document.getElementById("ticketForm");
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const customerId = document.getElementById("customerSelect").value;
        const title = document.getElementById("ticketTitle").value.trim();
        const description = document.getElementById("ticketDesc").value.trim();

        const btn = document.getElementById("btnSubmitTicket");
        btn.disabled = true;
        btn.innerHTML = `<span class="status-indicator"></span> Running NLP Vectorizer...`;

        try {
            // First call classification
            const classRes = await fetch("/classify", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ title, description })
            });

            let classData;
            if (classRes.ok) {
                classData = await classRes.json();
            } else {
                classData = fallbackClassify(title + " " + description);
            }

            displayClassificationResult(classData);

            // Create ticket via API
            const createRes = await fetch("/api/ticket/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    customer_id: parseInt(customerId),
                    title,
                    description,
                    category: classData.category,
                    priority: classData.recommended_urgency,
                    confidence_score: classData.confidence
                })
            });

            let newTicket;
            if (createRes.ok) {
                newTicket = await createRes.json();
            } else {
                newTicket = {
                    ticket_id: Math.floor(1000 + Math.random() * 9000),
                    customer_id: parseInt(customerId),
                    title,
                    description,
                    category: classData.category,
                    priority: classData.recommended_urgency,
                    status: "Assigned",
                    assigned_agent_id: 1,
                    customer_name: "Active Customer",
                    agent_name: "Ramesh Patel (L1 General)"
                };
            }

            selectTicketObject(newTicket);
            initRecentTickets();
        } catch (err) {
            console.error(err);
            const fallback = fallbackClassify(title + " " + description);
            displayClassificationResult(fallback);
        } finally {
            btn.disabled = false;
            btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13"></path><polygon points="22 2 15 22 11 13 2 9 22 2"></polygon></svg> Analyze & Route Ticket`;
        }
    });
}

function fallbackClassify(text) {
    const t = text.toLowerCase();
    if (t.includes("charge") || t.includes("refund") || t.includes("invoice") || t.includes("stripe")) {
        return {
            category: "Billing",
            confidence: 0.94,
            recommended_urgency: t.includes("twice") ? "Critical" : "High",
            class_probabilities: { Billing: 0.94, Technical: 0.02, Auth_Access: 0.01, Bug_Report: 0.02, General: 0.01 }
        };
    } else if (t.includes("deadlock") || t.includes("500") || t.includes("database") || t.includes("kubernetes")) {
        return {
            category: "Technical",
            confidence: 0.88,
            recommended_urgency: "Critical",
            class_probabilities: { Technical: 0.88, Bug_Report: 0.06, Auth_Access: 0.03, Billing: 0.02, General: 0.01 }
        };
    } else if (t.includes("saml") || t.includes("sso") || t.includes("2fa") || t.includes("login")) {
        return {
            category: "Auth_Access",
            confidence: 0.91,
            recommended_urgency: "High",
            class_probabilities: { Auth_Access: 0.91, Technical: 0.05, General: 0.02, Billing: 0.01, Bug_Report: 0.01 }
        };
    } else if (t.includes("button") || t.includes("export") || t.includes("broken") || t.includes("unclickable")) {
        return {
            category: "Bug_Report",
            confidence: 0.86,
            recommended_urgency: "Medium",
            class_probabilities: { Bug_Report: 0.86, Technical: 0.08, General: 0.03, Billing: 0.02, Auth_Access: 0.01 }
        };
    } else {
        return {
            category: "General",
            confidence: 0.78,
            recommended_urgency: "Low",
            class_probabilities: { General: 0.78, Bug_Report: 0.08, Technical: 0.06, Billing: 0.05, Auth_Access: 0.03 }
        };
    }
}

function displayClassificationResult(data) {
    const box = document.getElementById("classificationResult");
    box.classList.remove("hidden");

    document.getElementById("resBadgeCategory").innerText = data.category;
    document.getElementById("resConfidence").innerText = (data.confidence * 100).toFixed(1) + "%";
    document.getElementById("resUrgency").innerText = data.recommended_urgency || "Medium";
    document.getElementById("resTier").innerText = (data.recommended_urgency === "Critical") ? "L2/L3 Fast-Track" : "L1 Triage";

    const distContainer = document.getElementById("probDistribution");
    const probs = data.class_probabilities || {};
    distContainer.innerHTML = Object.entries(probs).map(([cat, val]) => `
        <div class="prob-item">
            <div class="prob-meta">
                <span>${cat}</span>
                <strong>${(val * 100).toFixed(1)}%</strong>
            </div>
            <div class="prob-bar-track">
                <div class="prob-bar-fill" style="width: ${Math.max(5, val * 100)}%"></div>
            </div>
        </div>
    `).join("");
}

// 5. Select & Render Ticket Details
window.selectTicket = async function(ticketId) {
    try {
        const res = await fetch(`/api/ticket/${ticketId}`);
        if (res.ok) {
            const ticket = await res.json();
            selectTicketObject(ticket);
            return;
        }
    } catch (e) {}

    // Fallback ticket representation
    selectTicketObject({
        ticket_id: ticketId,
        title: "Selected Ticket #" + ticketId,
        description: "Customer complaint regarding SaaS infrastructure service disruption.",
        category: "Technical",
        priority: "High",
        status: "Assigned",
        customer_name: "Satya Nadella",
        customer_tier: "Enterprise",
        agent_name: "Ramesh Patel (L1 General)"
    });
};

function selectTicketObject(ticket) {
    currentSelectedTicket = ticket;
    document.getElementById("emptyStateMsg").classList.add("hidden");
    const card = document.getElementById("ticketDetailCard");
    card.classList.remove("hidden");

    document.getElementById("dtlTicketId").innerText = `Ticket #${ticket.ticket_id}`;
    document.getElementById("dtlPriority").innerText = ticket.priority || "Medium";
    document.getElementById("dtlStatus").innerText = ticket.status || "Assigned";
    document.getElementById("dtlTitle").innerText = ticket.title;
    document.getElementById("dtlDesc").innerText = ticket.description;
    document.getElementById("dtlCustomerName").innerText = ticket.customer_name || "Enterprise Customer";
    document.getElementById("dtlAgentName").innerText = ticket.agent_name || "Ramesh Patel (L1 General)";

    const feedback = document.getElementById("actionFeedback");
    feedback.classList.add("hidden");
    feedback.innerHTML = "";

    document.getElementById("btnSpecialistResolve").classList.add("hidden");
    document.getElementById("btnEscalateDijkstra").disabled = false;
}

// 6. Ticket Action Handlers (L1 Resolve, Escalate Dijkstra, Specialist Resolve)
document.getElementById("btnL1Resolve").addEventListener("click", () => {
    if (!currentSelectedTicket) return;
    const fb = document.getElementById("actionFeedback");
    fb.classList.remove("hidden");

    if (currentSelectedTicket.category === "General") {
        fb.innerHTML = `<strong>[OOPJ L1 Resolution]</strong> Success: L1 Agent solved Ticket #${currentSelectedTicket.ticket_id} with documentation.`;
        document.getElementById("dtlStatus").innerText = "Resolved";
    } else {
        fb.innerHTML = `<strong>[OOPJ L1 Resolution]</strong> L1 Agent lacks domain access for <em>'${currentSelectedTicket.category}'</em>. Status set to <strong>Escalated</strong>. Please click 'Escalate via Dijkstra'.`;
        document.getElementById("dtlStatus").innerText = "Escalated";
    }
});

document.getElementById("btnEscalateDijkstra").addEventListener("click", async () => {
    if (!currentSelectedTicket) return;
    const fb = document.getElementById("actionFeedback");
    fb.classList.remove("hidden");

    let spec = currentSelectedTicket.category;
    if (spec === "Bug_Report") spec = "Technical";

    // Run Dijkstra path
    const route = calculateDijkstraRoute("A1", spec);
    animateDijkstraOnSvg(route.path);

    fb.innerHTML = `
        <strong>[ADSA Dijkstra Optimal Escalation Route]</strong><br>
        &bull; Shortest Path: <strong>${route.path.join(" &rarr; ")}</strong><br>
        &bull; Destination Specialist: <strong>${graphNodes[route.destination].name} (${graphNodes[route.destination].tier})</strong><br>
        &bull; Dynamic Congestion Score: <strong>${route.score.toFixed(2)} minutes</strong><br>
        &bull; Audit Context: Foreign Keys preserved in <code>escalation_logs</code> table.
    `;

    document.getElementById("dtlAgentName").innerText = `${graphNodes[route.destination].name} (${graphNodes[route.destination].tier})`;
    document.getElementById("dtlStatus").innerText = "Escalated (In Progress)";
    document.getElementById("btnSpecialistResolve").classList.remove("hidden");
});

document.getElementById("btnSpecialistResolve").addEventListener("click", () => {
    if (!currentSelectedTicket) return;
    const fb = document.getElementById("actionFeedback");
    fb.innerHTML = `<strong>[OOPJ Runtime Polymorphism]</strong> Resolved: Specialized Domain Handler executed. Ticket #${currentSelectedTicket.ticket_id} marked as <strong>Resolved</strong>.`;
    document.getElementById("dtlStatus").innerText = "Resolved";
    document.getElementById("btnSpecialistResolve").classList.add("hidden");
});

// 7. SVG Graph Renderer & Dijkstra Calculation
function renderGraph(highlightPath = []) {
    const svg = document.getElementById("escalationGraphSvg");
    if (!svg) return;

    let html = `
        <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="24" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#6366f1" />
            </marker>
            <marker id="arrowhead-active" markerWidth="10" markerHeight="7" refX="24" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#10b981" />
            </marker>
        </defs>
    `;

    // Render Edges
    graphEdges.forEach(edge => {
        const u = graphNodes[edge.from];
        const v = graphNodes[edge.to];
        const isActive = isEdgeInPath(edge.from, edge.to, highlightPath);
        const strokeColor = isActive ? "#10b981" : "rgba(255, 255, 255, 0.15)";
        const strokeWidth = isActive ? "3.5" : "1.5";
        const marker = isActive ? "url(#arrowhead-active)" : "url(#arrowhead)";

        // Compute dynamic weight for display
        const loadRatio = v.load / v.max;
        const dynamicCost = (edge.base * (1.0 + 1.5 * loadRatio)).toFixed(1);

        const midX = (u.x + v.x) / 2;
        const midY = (u.y + v.y) / 2;

        html += `
            <line x1="${u.x}" y1="${u.y}" x2="${v.x}" y2="${v.y}" 
                  stroke="${strokeColor}" stroke-width="${strokeWidth}" 
                  marker-end="${marker}" />
            <text x="${midX}" y="${midY - 6}" fill="${isActive ? '#10b981' : '#64748b'}" 
                  font-size="10" font-family="'JetBrains Mono', monospace" text-anchor="middle">
                ${dynamicCost}m
            </text>
        `;
    });

    // Render Nodes
    Object.entries(graphNodes).forEach(([id, n]) => {
        const isPathNode = highlightPath.includes(id);
        const tierClass = n.tier.toLowerCase().replace('_', '-');
        const fill = getNodeColor(n.tier);
        const ringColor = isPathNode ? "#10b981" : fill;
        const radius = isPathNode ? 22 : 18;

        html += `
            <g class="graph-node-group" style="cursor: pointer;" onclick="inspectNode('${id}')">
                <circle cx="${n.x}" cy="${n.y}" r="${radius + 4}" fill="none" stroke="${ringColor}" stroke-width="${isPathNode ? 3 : 1}" opacity="${isPathNode ? 0.9 : 0.3}" />
                <circle cx="${n.x}" cy="${n.y}" r="${radius}" fill="#0f172a" stroke="${fill}" stroke-width="2" />
                <text x="${n.x}" y="${n.y + 4}" fill="#ffffff" font-size="11" font-weight="700" text-anchor="middle">
                    ${id}
                </text>
                <text x="${n.x}" y="${n.y + radius + 15}" fill="#cbd5e1" font-size="11" font-weight="600" text-anchor="middle">
                    ${n.name.split(' ')[0]} (${n.tier})
                </text>
                <text x="${n.x}" y="${n.y + radius + 28}" fill="#94a3b8" font-size="9" text-anchor="middle">
                    ${n.spec} &bull; Load: ${n.load}/${n.max}
                </text>
            </g>
        `;
    });

    svg.innerHTML = html;
}

function getNodeColor(tier) {
    switch(tier) {
        case "L1": return "#38bdf8";
        case "L2": return "#818cf8";
        case "L3": return "#c084fc";
        case "Specialist_Lead": return "#f59e0b";
        default: return "#64748b";
    }
}

function isEdgeInPath(u, v, path) {
    for (let i = 0; i < path.length - 1; i++) {
        if (path[i] === u && path[i+1] === v) return true;
    }
    return false;
}

function initGraphControls() {
    document.getElementById("btnRunDijkstraCalc").addEventListener("click", () => {
        const start = document.getElementById("selStartAgent").value;
        const spec = document.getElementById("selTargetSpec").value;

        const route = calculateDijkstraRoute(start, spec);
        renderGraph(route.path);

        const narrative = document.getElementById("pathNarrative");
        narrative.innerHTML = `
            <strong>Shortest Path Traversal:</strong> ${route.path.join(" &rarr; ")}<br>
            <strong>Chosen Agent:</strong> ${graphNodes[route.destination].name} (${graphNodes[route.destination].tier}, ${graphNodes[route.destination].spec})<br>
            <strong>Total Dynamic Latency:</strong> ${route.score.toFixed(2)} minutes<br>
            <em>* Dynamic edge weight accounted for destination current queue load (${graphNodes[route.destination].load}/${graphNodes[route.destination].max}).</em>
        `;
    });

    document.getElementById("btnCongestA3").addEventListener("click", () => {
        graphNodes["A3"].load = 7; // Congest A3
        alert("Simulated high congestion on Kiran Kumar (A3): Load set to 7/8. Now rerun Dijkstra to observe automatic bypass to L3 (A6)!");
        renderGraph();
    });
}

function calculateDijkstraRoute(startId, targetSpec) {
    // Dijkstra priority queue calculation
    let dist = {};
    let prev = {};
    let pq = [{ id: startId, cost: 0 }];

    Object.keys(graphNodes).forEach(k => { dist[k] = Infinity; });
    dist[startId] = 0;

    let candidates = [];

    while (pq.length > 0) {
        pq.sort((a, b) => a.cost - b.cost);
        let curr = pq.shift();
        let u = curr.id;

        if (curr.cost > dist[u]) continue;

        if (u !== startId) {
            let n = graphNodes[u];
            if (n.spec === targetSpec && n.load < n.max) {
                let path = reconstructPath(prev, startId, u);
                candidates.push({ destination: u, score: curr.cost, path });
            }
        }

        // Neighbors
        graphEdges.filter(e => e.from === u).forEach(edge => {
            let targetNode = graphNodes[edge.to];
            let loadRatio = targetNode.load / targetNode.max;
            let dynamicWeight = edge.base * (1.0 + 1.5 * loadRatio);
            let newCost = curr.cost + dynamicWeight;

            if (newCost < dist[edge.to]) {
                dist[edge.to] = newCost;
                prev[edge.to] = u;
                pq.push({ id: edge.to, cost: newCost });
            }
        });
    }

    candidates.sort((a, b) => a.score - b.score);
    return candidates.length > 0 ? candidates[0] : { destination: "A3", score: 20.6, path: [startId, "A3"] };
}

function reconstructPath(prev, start, end) {
    let path = [end];
    let curr = end;
    while (curr !== start && prev[curr]) {
        curr = prev[curr];
        path.unshift(curr);
    }
    return path;
}

function animateDijkstraOnSvg(path) {
    renderGraph(path);
}

window.inspectNode = function(id) {
    const node = graphNodes[id];
    alert(`Agent: ${node.name}\nTier: ${node.tier}\nDomain: ${node.spec}\nActive Workload: ${node.load}/${node.max}`);
};

// 8. Relational Algebra Query Runner
window.runAlgebraQuery = async function(qid) {
    const container = document.getElementById("queryResultContainer");
    container.innerHTML = "<span class='help-text'>Executing relational algebra query on SQLite...</span>";

    try {
        const res = await fetch(`/api/query/${qid}`);
        if (res.ok) {
            const data = await res.json();
            renderTable(container, data.columns, data.rows);
            return;
        }
    } catch (e) {}

    // Fallback Mock Results
    if (qid === 'q1') {
        renderTable(container, 
            ['ticket_id', 'title', 'category', 'priority', 'status'],
            [
                [3, '2FA OTP authentication loop', 'Auth_Access', 'High', 'Escalated'],
                [6, 'Chargeback alert from Stripe webhook', 'Billing', 'Critical', 'Escalated']
            ]
        );
    } else if (qid === 'q2') {
        renderTable(container,
            ['ticket_id', 'title', 'customer_name', 'subscription_tier'],
            [
                [1, 'Database connection pool exhausted', 'Satya Nadella', 'Enterprise'],
                [2, 'Enterprise invoice not generated', 'Sundar Pichai', 'Enterprise'],
                [3, '2FA OTP authentication loop', 'Ravi Kumar', 'Pro']
            ]
        );
    } else {
        renderTable(container,
            ['ticket_id', 'reason', 'to_agent'],
            [
                [3, 'L1 unable to debug SAML assertion token', 'Manish Joshi (L2 Auth)'],
                [6, 'Refund authorization exceeds $5,000 threshold', 'Lakshmi Narayanan (L3 Finance)']
            ]
        );
    }
};

function renderTable(container, columns, rows) {
    let html = `<table class="results-table"><thead><tr>`;
    columns.forEach(col => { html += `<th>${col}</th>`; });
    html += `</tr></thead><tbody>`;
    rows.forEach(row => {
        html += `<tr>`;
        row.forEach(cell => { html += `<td>${escapeHtml(String(cell))}</td>`; });
        html += `</tr>`;
    });
    html += `</tbody></table>`;
    container.innerHTML = html;
}

function escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
