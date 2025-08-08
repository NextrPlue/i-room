from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class WatchCreate(BaseModel):
    workerId: int
    latitude: float
    longitude: float
    timestamp: Optional[datetime] = None  # 기본값은 서버 시간, 전달 받는 시간

    class Config:
        from_attributes = True
