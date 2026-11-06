from fastapi import Header, HTTPException
from app.config import settings

def require_api_key(x_api_key: str = Header(None, alias="X-API-Key")):
    if x_api_key != settings.api_key:
        raise HTTPException(status_code=401, detail="Invalid or missing API key")
    return x_api_key
