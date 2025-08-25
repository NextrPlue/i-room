from sqlalchemy import Column, BigInteger, Integer, Text, DateTime, ForeignKey, Double, String
from ppe.database import Base
import datetime

class Violation(Base):
    __tablename__ = "violation"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    worker_id = Column(BigInteger, ForeignKey("watch.worker_id", ondelete="CASCADE"), nullable=False)
    timestamp = Column(DateTime)
    incident_type = Column(String(255), default = 'PPE_VIOLATION', nullable=False)
    incident_id = Column(BigInteger)
    latitude = Column(Double)
    longitude = Column(Double) 
    incident_description = Column(String(1000))
    image_url = Column(String(1000))