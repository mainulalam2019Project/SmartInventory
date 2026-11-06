import pandas as pd
import numpy as np
from typing import List, Dict, Any

def to_inv_df(items: List[Dict[str, Any]]) -> pd.DataFrame:
    if not items:
        return pd.DataFrame()
    df = pd.DataFrame(items)
    for col in ["quantity_on_hand", "quantity_reserved", "quantity_in_transit", "reorder_point", "safety_stock"]:
        if col in df.columns:
            df[col] = pd.to_numeric(df[col], errors="coerce").fillna(0).astype(int)
    if "unit_cost" in df.columns:
        df["unit_cost"] = pd.to_numeric(df["unit_cost"], errors="coerce").fillna(0.0)
    df["available"] = df.get("quantity_on_hand", 0) - df.get("quantity_reserved", 0)
    df["inventory_value"] = df.get("quantity_on_hand", 0) * df.get("unit_cost", 0.0)
    return df

def to_sales_df(items: List[Dict[str, Any]]) -> pd.DataFrame:
    if not items:
        return pd.DataFrame()
    df = pd.DataFrame(items)
    df["quantity"] = pd.to_numeric(df.get("quantity", 0), errors="coerce").fillna(0).astype(int)
    # normalize date column
    date_col = None
    for c in ["occurred_at", "order_date", "date"]:
        if c in df.columns:
            date_col = c
            break
    if date_col:
        df["date"] = pd.to_datetime(df[date_col], errors="coerce")
    else:
        df["date"] = pd.NaT
    if "unit_price" in df.columns:
        df["unit_price"] = pd.to_numeric(df["unit_price"], errors="coerce").fillna(0.0)
        df["revenue"] = df["quantity"] * df["unit_price"]
    else:
        df["revenue"] = 0.0
    return df

def inventory_performance(inventory, sales, period_days=30):
    inv = to_inv_df([i.model_dump() if hasattr(i, "model_dump") else i for i in inventory])
    sls = to_sales_df([s.model_dump() if hasattr(s, "model_dump") else s for s in sales])

    total_skus = len(inv)
    total_on_hand = int(inv["quantity_on_hand"].sum()) if not inv.empty else 0
    total_reserved = int(inv["quantity_reserved"].sum()) if not inv.empty else 0
    total_available = int(inv["available"].sum()) if not inv.empty else 0
    total_value = float(inv["inventory_value"].sum()) if not inv.empty else 0.0
    low_stock_count = int((inv["quantity_on_hand"] <= inv["reorder_point"]).sum()) if not inv.empty else 0

    # sales aggregates
    total_sold = int(sls["quantity"].sum()) if not sls.empty else 0
    total_revenue = float(sls["revenue"].sum()) if not sls.empty else 0.0

    # per-product turnover
    turnover = []
    if not inv.empty:
        sold_by_product = sls.groupby("product_id")["quantity"].sum() if (not sls.empty and "product_id" in sls.columns) else pd.Series(dtype=int)
        for _, row in inv.iterrows():
            sold = int(sold_by_product.get(row.get("product_id"), 0)) if isinstance(sold_by_product, pd.Series) else 0
            avg_stock = (row["quantity_on_hand"] + 0) / 2 if row["quantity_on_hand"] else 0
            to_ratio = (sold / avg_stock) if avg_stock > 0 else 0.0
            days_supply = (avg_stock / (sold / period_days)) if sold > 0 else None
            turnover.append({
                "product_id": int(row.get("product_id")),
                "product_name": row.get("product_name"),
                "sku": row.get("sku"),
                "quantity_on_hand": int(row["quantity_on_hand"]),
                "sold_in_period": sold,
                "turnover_ratio": round(to_ratio, 4),
                "days_of_supply": (round(days_supply, 2) if days_supply is not None else None),
            })

    return {
        "summary": {
            "total_skus": total_skus,
            "total_on_hand": total_on_hand,
            "total_reserved": total_reserved,
            "total_available": total_available,
            "total_inventory_value": round(total_value, 2),
            "low_stock_skus": low_stock_count,
            "total_sold_in_period": total_sold,
            "total_revenue_in_period": round(total_revenue, 2),
            "period_days": period_days,
        },
        "turnover": turnover,
    }

def stock_turnover(inventory, sales, period_days=30):
    res = inventory_performance(inventory, sales, period_days)
    return {"period_days": period_days, "items": res["turnover"]}

def slow_moving(inventory, sales, days_threshold=90, period_days=90):
    inv = to_inv_df([i.model_dump() if hasattr(i, "model_dump") else i for i in inventory])
    sls = to_sales_df([s.model_dump() if hasattr(s, "model_dump") else s for s in sales])

    sold_by_product = {}
    last_sale = {}
    if not sls.empty and "product_id" in sls.columns:
        sold_by_product = sls.groupby("product_id")["quantity"].sum().to_dict()
        if "date" in sls.columns:
            last_sale = sls.groupby("product_id")["date"].max().to_dict()

    rows = []
    for _, row in inv.iterrows():
        pid = int(row.get("product_id"))
        sold = int(sold_by_product.get(pid, 0))
        ls = last_sale.get(pid)
        days_since = None
        if ls is not None and pd.notna(ls):
            days_since = (pd.Timestamp.now() - pd.Timestamp(ls)).days
        is_slow = sold == 0 or (days_since is not None and days_since >= days_threshold)
        rows.append({
            "product_id": pid,
            "product_name": row.get("product_name"),
            "sku": row.get("sku"),
            "quantity_on_hand": int(row["quantity_on_hand"]),
            "sold_in_period": sold,
            "days_since_last_sale": days_since,
            "is_slow_moving": bool(is_slow),
        })
    slow = [r for r in rows if r["is_slow_moving"]]
    return {
        "days_threshold": days_threshold,
        "period_days": period_days,
        "total_products": len(rows),
        "slow_moving_count": len(slow),
        "slow_moving": slow,
        "all": rows,
    }

def monthly_sales(sales):
    sls = to_sales_df([s.model_dump() if hasattr(s, "model_dump") else s for s in sales])
    if sls.empty or "date" not in sls.columns or sls["date"].isna().all():
        return {"months": []}
    sls["month"] = sls["date"].dt.to_period("M").astype(str)
    g = sls.groupby("month").agg(
        quantity=("quantity", "sum"),
        revenue=("revenue", "sum"),
        orders=("quantity", "count"),
    ).reset_index().sort_values("month")
    months = []
    for _, r in g.iterrows():
        months.append({
            "month": str(r["month"]),
            "quantity_sold": int(r["quantity"]),
            "revenue": round(float(r["revenue"]), 2),
            "line_items": int(r["orders"]),
        })
    return {"months": months, "total_revenue": round(float(sls["revenue"].sum()), 2), "total_quantity": int(sls["quantity"].sum())}

def reorder_levels(history, lead_time_days=7, service_level=0.95, period_days=30):
    # history: list of {product_id, date, quantity} representing demand per period
    if not history:
        return {"items": []}
    df = pd.DataFrame([h.model_dump() if hasattr(h, "model_dump") else h for h in history])
    df["quantity"] = pd.to_numeric(df["quantity"], errors="coerce").fillna(0)
    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    results = []
    for pid, g in df.groupby("product_id"):
        daily = g.set_index("date")["quantity"].resample("D").sum() if "date" in g.columns else g["quantity"]
        avg_daily = float(g["quantity"].sum()) / max(period_days, 1)
        std_daily = float(g["quantity"].std()) if len(g) > 1 else 0.0
        # z for service level (approx)
        from scipy.stats import norm
        z = float(norm.ppf(service_level)) if 0 < service_level < 1 else 1.645
        safety_stock = z * std_daily * np.sqrt(lead_time_days)
        reorder_point = avg_daily * lead_time_days + safety_stock
        results.append({
            "product_id": int(pid),
            "avg_daily_demand": round(avg_daily, 4),
            "std_daily_demand": round(std_daily, 4),
            "lead_time_days": lead_time_days,
            "service_level": service_level,
            "safety_stock": int(np.ceil(safety_stock)),
            "reorder_point": int(np.ceil(reorder_point)),
        })
    return {"items": results}

def shortage_risk(inventory, demand_forecast, horizon_days=30):
    inv = to_inv_df([i.model_dump() if hasattr(i, "model_dump") else i for i in inventory])
    # build forecast lookup
    forecast_by_product = {}
    for f in demand_forecast:
        f = f if not hasattr(f, "model_dump") else f.model_dump()
        pid = f.get("product_id")
        forecast_by_product[pid] = forecast_by_product.get(pid, 0) + f.get("quantity", 0)

    rows = []
    for _, row in inv.iterrows():
        pid = int(row.get("product_id"))
        demand = forecast_by_product.get(pid, row.get("reorder_point", 0))
        on_hand = int(row["quantity_on_hand"])
        available = int(row["available"])
        risk = "LOW"
        if available <= 0 or available < demand * 0.5:
            risk = "HIGH"
        elif available < demand:
            risk = "MEDIUM"
        rows.append({
            "product_id": pid,
            "product_name": row.get("product_name"),
            "sku": row.get("sku"),
            "available": available,
            "forecast_demand": int(demand),
            "risk_level": risk,
        })
    return {"horizon_days": horizon_days, "items": rows}
