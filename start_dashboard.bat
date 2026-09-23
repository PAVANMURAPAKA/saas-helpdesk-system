@echo off
title ResolveDesk AI - Enterprise SaaS Helpdesk Platform
echo =====================================================================
echo           ResolveDesk AI - SaaS Helpdesk & Escalation Platform
echo =====================================================================
echo.
echo [1/3] Initializing SQLite database and verifying schemas...
python database\init_db.py
echo.
echo [2/3] Launching web dashboard in your browser...
start http://127.0.0.1:8080
echo.
echo [3/3] Starting Python API & Static Server on port 8080...
python web_dashboard\server.py
pause
