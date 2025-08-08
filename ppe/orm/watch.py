from sqlalchemy import Column, Integer, Float, DateTime, Double
import datetime
from ppe.database import Base

class Watch(Base):
    __tablename__ = "watch"

    id = Column(Integer, primary_key=True, index=True)
    worker_id = Column(Integer, nullable=False, unique = True)
    latitude = Column(Double, nullable=False)
    longitude = Column(Double, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)


