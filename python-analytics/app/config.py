import os

class Settings:
    api_key: str = os.getenv("ANALYTICS_API_KEY", "dev-analytics-key")
    host: str = os.getenv("HOST", "0.0.0.0")
    port: int = int(os.getenv("PORT", "8000"))
    reports_dir: str = os.getenv("REPORTS_DIR", "./reports")
    log_level: str = os.getenv("LOG_LEVEL", "info")

settings = Settings()
