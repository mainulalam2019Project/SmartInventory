import os
import uuid
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import seaborn as sns
import pandas as pd
from app.config import settings

def _ensure_dir():
    os.makedirs(settings.reports_dir, exist_ok=True)

def generate_chart(report_type: str, data: dict, title: str = None) -> dict:
    _ensure_dir()
    fig, ax = plt.subplots(figsize=(8, 5))
    title = title or report_type.replace("_", " ").title()

    if report_type == "monthly_sales":
        months = data.get("months", [])
        if months:
            df = pd.DataFrame(months)
            sns.barplot(x="month", y="revenue", data=df, ax=ax, color="steelblue")
            ax.set_title(title)
            ax.set_xlabel("Month")
            ax.set_ylabel("Revenue")
            plt.xticks(rotation=45)
    elif report_type == "turnover":
        items = data.get("items", [])
        if items:
            df = pd.DataFrame(items)
            sns.barplot(x="product_name", y="turnover_ratio", data=df.head(15), ax=ax, color="teal")
            ax.set_title(title)
            ax.set_xlabel("Product")
            ax.set_ylabel("Turnover Ratio")
            plt.xticks(rotation=60, ha="right")
    elif report_type == "slow_moving":
        items = data.get("slow_moving", [])
        if items:
            df = pd.DataFrame(items)
            sns.barplot(x="product_name", y="quantity_on_hand", data=df.head(15), ax=ax, color="salmon")
            ax.set_title(title)
            ax.set_xlabel("Product")
            ax.set_ylabel("On Hand")
            plt.xticks(rotation=60, ha="right")
    elif report_type == "shortage_risk":
        items = data.get("items", [])
        if items:
            df = pd.DataFrame(items)
            sns.countplot(x="risk_level", data=df, ax=ax, palette="Set2")
            ax.set_title(title)
            ax.set_xlabel("Risk Level")
            ax.set_ylabel("Count")
    else:
        ax.text(0.5, 0.5, f"No chart template for {report_type}", ha="center", va="center")
        ax.set_title(title)

    plt.tight_layout()
    fname = f"{report_type}_{uuid.uuid4().hex[:8]}.png"
    path = os.path.join(settings.reports_dir, fname)
    fig.savefig(path, dpi=100)
    plt.close(fig)
    return {"file": fname, "path": path, "report_type": report_type}
