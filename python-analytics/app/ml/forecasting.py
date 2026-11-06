import pandas as pd
import numpy as np
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error
from typing import List, Dict, Any

def _to_df(history):
    items = [h.model_dump() if hasattr(h, "model_dump") else h for h in history]
    df = pd.DataFrame(items)
    df["quantity"] = pd.to_numeric(df["quantity"], errors="coerce").fillna(0.0)
    df["date"] = pd.to_datetime(df["date"], errors="coerce")
    df = df.dropna(subset=["date"]).sort_values("date").reset_index(drop=True)
    # aggregate by date
    df = df.groupby("date", as_index=False)["quantity"].sum()
    return df

def _make_features(df):
    df = df.copy()
    df["t"] = np.arange(len(df))
    df["dayofweek"] = df["date"].dt.dayofweek
    df["month"] = df["date"].dt.month
    # lag features
    df["lag1"] = df["quantity"].shift(1)
    df["lag7"] = df["quantity"].shift(7)
    df["roll7"] = df["quantity"].rolling(7, min_periods=1).mean()
    return df

def forecast(history, horizon=7, model="linear", test_size=0.2):
    df = _to_df(history)
    if len(df) < 5:
        return {
            "error": "Insufficient history (need at least 5 observations)",
            "n_observations": len(df),
        }

    feat = _make_features(df).dropna().reset_index(drop=True)
    # ensure enough rows after feature engineering
    if len(feat) < 5:
        # fallback: use simple index feature only
        feat = _make_features(df)
        feat["lag1"] = feat["lag1"].bfill()
        feat["lag7"] = feat["lag7"].bfill()
        feat["roll7"] = feat["roll7"].bfill()
        feat = feat.fillna(0)

    feature_cols = ["t", "dayofweek", "month", "lag1", "lag7", "roll7"]
    X = feat[feature_cols].values
    y = feat["quantity"].values

    # time-based split
    split_idx = max(1, int(len(X) * (1 - test_size)))
    split_idx = min(split_idx, len(X) - 1)
    X_train, X_test = X[:split_idx], X[split_idx:]
    y_train, y_test = y[:split_idx], y[split_idx:]

    if model == "moving_average":
        # predict using rolling mean of last 7
        preds_test = np.array([feat["roll7"].iloc[i] for i in range(split_idx, len(feat))])
        mdl = None
    elif model == "random_forest":
        mdl = RandomForestRegressor(n_estimators=100, random_state=42)
        mdl.fit(X_train, y_train)
        preds_test = mdl.predict(X_test)
    else:
        mdl = LinearRegression()
        mdl.fit(X_train, y_train)
        preds_test = mdl.predict(X_test)

    # metrics
    mae = float(mean_absolute_error(y_test, preds_test)) if len(y_test) > 0 else 0.0
    rmse = float(np.sqrt(mean_squared_error(y_test, preds_test))) if len(y_test) > 0 else 0.0
    # MAPE (avoid div by zero)
    denom = np.where(y_test == 0, 1, y_test)
    mape = float(np.mean(np.abs((y_test - preds_test) / denom)) * 100) if len(y_test) > 0 else 0.0

    # baseline: naive (last observed value)
    naive_pred = np.full_like(y_test, fill_value=y_train[-1] if len(y_train) else 0, dtype=float)
    baseline_mae = float(mean_absolute_error(y_test, naive_pred)) if len(y_test) > 0 else 0.0

    # future forecast
    future_dates = pd.date_range(start=df["date"].max() + pd.Timedelta(days=1), periods=horizon, freq="D")
    # build features iteratively
    history_qty = list(df["quantity"].values)
    future_preds = []
    for i in range(horizon):
        t = len(df) + i
        d = future_dates[i]
        lag1 = history_qty[-1] if history_qty else 0.0
        lag7 = history_qty[-7] if len(history_qty) >= 7 else (history_qty[0] if history_qty else 0.0)
        roll7 = float(np.mean(history_qty[-7:])) if history_qty else 0.0
        row = np.array([[t, d.dayofweek, d.month, lag1, lag7, roll7]])
        if model == "moving_average":
            pred = roll7
        else:
            if mdl is None:
                pred = roll7
            else:
                pred = float(mdl.predict(row)[0])
        pred = max(0.0, pred)
        future_preds.append(pred)
        history_qty.append(pred)

    future = [{"date": str(d.date()), "predicted_quantity": round(float(p), 2)} for d, p in zip(future_dates, future_preds)]

    return {
        "model": model,
        "n_observations": len(df),
        "train_size": int(len(X_train)),
        "test_size": int(len(X_test)),
        "metrics": {
            "mae": round(mae, 4),
            "rmse": round(rmse, 4),
            "mape_percent": round(mape, 4),
        },
        "baseline_naive_mae": round(baseline_mae, 4),
        "improvement_vs_naive_percent": round((1 - mae / baseline_mae) * 100, 2) if baseline_mae > 0 else None,
        "future_forecast": future,
        "note": "Metrics evaluated on a held-out temporal test split. Forecast accuracy is not guaranteed; use as a planning aid.",
    }
