from sqlalchemy import Column, Integer, Float, DateTime
import datetime
from database import Base

class Watch(Base):
    __tablename__ = "watch"

    id = Column(Integer, primary_key=True, index=True)
    workerId = Column(Integer, nullable=False)
    latitude = Column(Float, nullable=False)
    longitude = Column(Float, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
