from fastapi import APIRouter, Depends
from app.models.schemas import (
    InventoryPerformanceRequest, TurnoverRequest, SlowMovingRequest,
    MonthlySalesRequest, ReorderLevelRequest, ShortageRiskRequest,
    ForecastRequest, ReportRequest,
)
from app.services.inventory_analytics import (
    inventory_performance, stock_turnover, slow_moving, monthly_sales,
    reorder_levels, shortage_risk,
)
from app.ml.forecasting import forecast
from app.services.reporting import generate_chart
from app.utils.security import require_api_key

router = APIRouter(prefix="/analytics/v1", tags=["analytics"], dependencies=[Depends(require_api_key)])

@router.get("/health")
def health():
    return {"status": "ok", "service": "smart-inventory-analytics"}

@router.post("/inventory/performance")
def inv_perf(req: InventoryPerformanceRequest):
    inv = [i.model_dump() for i in req.inventory]
    sls = [s.model_dump() for s in req.sales]
    return inventory_performance(inv, sls, req.period_days)

@router.post("/turnover")
def turnover(req: TurnoverRequest):
    inv = [i.model_dump() for i in req.inventory]
    sls = [s.model_dump() for s in req.sales]
    return stock_turnover(inv, sls, req.period_days)

@router.post("/slow-moving")
def slow(req: SlowMovingRequest):
    inv = [i.model_dump() for i in req.inventory]
    sls = [s.model_dump() for s in req.sales]
    return slow_moving(inv, sls, req.days_threshold, req.period_days)

@router.post("/sales/monthly")
def monthly(req: MonthlySalesRequest):
    sls = [s.model_dump() for s in req.sales]
    return monthly_sales(sls)

@router.post("/reorder-levels")
def reorder(req: ReorderLevelRequest):
    hist = [h.model_dump() for h in req.history]
    return reorder_levels(hist, req.lead_time_days, req.service_level, req.period_days)

@router.post("/shortage-risk")
def shortage(req: ShortageRiskRequest):
    inv = [i.model_dump() for i in req.inventory]
    return shortage_risk(inv, req.demand_forecast, req.horizon_days)

@router.post("/forecast")
def fc(req: ForecastRequest):
    hist = [h.model_dump() for h in req.history]
    return forecast(hist, req.horizon, req.model, req.test_size)

@router.post("/reports/generate")
def report(req: ReportRequest):
    chart = generate_chart(req.report_type, req.data, req.title)
    return {"report_type": req.report_type, "chart": chart}
