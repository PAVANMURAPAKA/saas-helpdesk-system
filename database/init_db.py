"""
Database initialization script for SQLite.
Creates database/helpdesk.db, executes schema.sql and seed_data.sql.
"""

import os
import sqlite3

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "helpdesk.db")
SCHEMA_PATH = os.path.join(BASE_DIR, "schema.sql")
SEED_PATH = os.path.join(BASE_DIR, "seed_data.sql")

def initialize_database():
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)
        print(f"Removed existing database at {DB_PATH}")

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    with open(SCHEMA_PATH, "r", encoding="utf-8") as f:
        schema_sql = f.read()
    cursor.executescript(schema_sql)
    print("Executed schema.sql successfully.")

    with open(SEED_PATH, "r", encoding="utf-8") as f:
        seed_sql = f.read()
    cursor.executescript(seed_sql)
    print("Executed seed_data.sql successfully.")

    conn.commit()
    conn.close()
    print(f"Database initialized and seeded at: {DB_PATH}")

if __name__ == "__main__":
    initialize_database()
