from fastapi import FastAPI
from app.config import settings
from app.routers.analytics import router

app = FastAPI(title="Smart Inventory Analytics", version="1.0.0")
app.include_router(router)

@app.get("/")
def root():
    return {"service": "smart-inventory-analytics", "docs": "/docs"}
