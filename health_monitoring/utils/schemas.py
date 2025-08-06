# /utils/schemas.py

from pydantic import BaseModel
from datetime import datetime

class IncidentResponse(BaseModel):
    incidentId: str
    workerId: str
    workerLatitude: float
    workerLongitude: float
    incidentType: str
    incidentDescription: str
    occurredAt: datetime

    class Config:
        orm_mode = True