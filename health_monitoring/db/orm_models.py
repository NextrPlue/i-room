# /db/orm_models.py

from sqlalchemy import Column, String, Float, DateTime
from sqlalchemy.ext.declarative import declarative_base

Base = declarative_base()

class Incident(Base):
    __tablename__ = "incidents"

    IncidentId = Column(String, primary_key=True)
    workerId = Column(String)
    workerLatitude = Column(Float)
    workerLongitude = Column(Float)
    incidentType = Column(String)
    incidentDescription = Column(String)
    occurredAt = Column(DateTime)