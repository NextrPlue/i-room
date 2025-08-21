# ppe/config.py
from functools import lru_cache
from pathlib import Path
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field

# 이 파일 기준으로 프로젝트 루트 계산
BASE_DIR = Path(__file__).resolve().parents[1]
ENV_PATH = BASE_DIR / ".env"   # <== 절대경로

class Settings(BaseSettings):
    RTSP_URL: str = Field(...)
    TURN_HOST: str = Field(...)
    TURN_REALM: str = Field("turn.local")
    TURN_REST_SECRET: str = Field(...)
    TURN_CRED_TTL: int = Field(600)

    ICE_FORCE_RELAY: bool = Field(True)
    STUN_URLS: str = Field("stun:stun.l.google.com:19302")

    APP_HOST: str = Field("127.0.0.1")
    APP_PORT: int = Field(8000)

    model_config = SettingsConfigDict(
        env_file=str(ENV_PATH),
        env_file_encoding="utf-8",
    )

@lru_cache()
def get_settings() -> Settings:
    return Settings()

settings = get_settings()
