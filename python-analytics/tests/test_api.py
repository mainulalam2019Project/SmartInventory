import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)
HEADERS = {"X-API-Key": "dev-analytics-key"}

def test_health():
    r = client.get("/analytics/v1/health", headers=HEADERS)
    assert r.status_code == 200
    assert r.json()["status"] == "ok"

def test_unauthorized():
    r = client.get("/analytics/v1/health")
    assert r.status_code == 401

def test_forecast_insufficient():
    r = client.post("/analytics/v1/forecast", json={"history": [{"date": "2026-10-01", "quantity": 5}], "horizon": 3}, headers=HEADERS)
    assert r.status_code == 200
    assert "error" in r.json()

def test_inventory_performance():
    payload = {
        "inventory": [{"product_id": 1, "product_name": "Laptop", "quantity_on_hand": 25, "quantity_reserved": 3, "reorder_point": 10, "unit_cost": 800}],
        "sales": [{"product_id": 1, "quantity": 2, "unit_price": 100, "occurred_at": "2026-10-05"}],
        "period_days": 30,
    }
    r = client.post("/analytics/v1/inventory/performance", json=payload, headers=HEADERS)
    assert r.status_code == 200
    assert r.json()["summary"]["total_skus"] == 1