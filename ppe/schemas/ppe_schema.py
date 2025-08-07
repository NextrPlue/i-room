# schemas/ppe_schema.py
from pydantic import BaseModel, ConfigDict
from datetime import datetime
from typing import Optional

class PPEDetectionBase(BaseModel):
    workerId: Optional[int]
    ppe_id: Optional[int]
    imageURL: Optional[str]
    confidenceScore: Optional[float]
    helmet_on: int
    seatbelt_on: int

class PPEDetectionCreate(PPEDetectionBase):
    pass

class PPEDetectionResponse(PPEDetectionBase):
    id: int
    timestamp: datetime

    class Config:
        model_config = ConfigDict(from_attributes=True)
