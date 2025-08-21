from sqlalchemy import Column, BigInteger, Integer, Text, DateTime, ForeignKey, Double, String
from ppe.database import Base
import datetime

class Violation(Base):
    __tablename__ = "violation"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    worker_id = Column(Integer, ForeignKey("watch.worker_id", ondelete="CASCADE"), nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow, nullable=False)
    incident_type = Column(Text, default = '보호구 미착용', nullable=False)
    incident_id = Column(Integer)
    latitude = Column(Double)
    longitude = Column(Double) 
    incident_description = Column(Text)
    image_url = Column(Text, nullable=False)