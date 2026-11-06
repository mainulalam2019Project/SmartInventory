from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field

class InventoryItem(BaseModel):
    product_id: int
    product_name: Optional[str] = None
    sku: Optional[str] = None
    warehouse_id: Optional[int] = None
    warehouse_code: Optional[str] = None
    quantity_on_hand: int = 0
    quantity_reserved: int = 0
    quantity_in_transit: int = 0
    reorder_point: int = 0
    safety_stock: int = 0
    unit_cost: float = 0.0

class SalesItem(BaseModel):
    product_id: Optional[int] = None
    quantity: int = 0
    unit_price: float = 0.0
    occurred_at: Optional[str] = None
    order_date: Optional[str] = None

class HistoryItem(BaseModel):
    product_id: Optional[int] = None
    date: str
    quantity: float

class InventoryPerformanceRequest(BaseModel):
    inventory: List[InventoryItem] = Field(default_factory=list)
    sales: List[SalesItem] = Field(default_factory=list)
    period_days: int = 30

class TurnoverRequest(BaseModel):
    inventory: List[InventoryItem] = Field(default_factory=list)
    sales: List[SalesItem] = Field(default_factory=list)
    period_days: int = 30

class SlowMovingRequest(BaseModel):
    inventory: List[InventoryItem] = Field(default_factory=list)
    sales: List[SalesItem] = Field(default_factory=list)
    days_threshold: int = 90
    period_days: int = 90

class MonthlySalesRequest(BaseModel):
    sales: List[SalesItem] = Field(default_factory=list)

class ReorderLevelRequest(BaseModel):
    history: List[HistoryItem] = Field(default_factory=list)
    lead_time_days: int = 7
    service_level: float = 0.95
    period_days: int = 30

class ShortageRiskRequest(BaseModel):
    inventory: List[InventoryItem] = Field(default_factory=list)
    demand_forecast: List[Dict[str, Any]] = Field(default_factory=list)
    horizon_days: int = 30

class ForecastRequest(BaseModel):
    history: List[HistoryItem] = Field(default_factory=list)
    horizon: int = 7
    model: str = "linear"  # linear | moving_average | random_forest
    test_size: float = 0.2

class ReportRequest(BaseModel):
    report_type: str = "inventory_performance"
    data: Dict[str, Any] = Field(default_factory=dict)
    title: Optional[str] = None
