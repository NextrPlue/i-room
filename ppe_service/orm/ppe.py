from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
import datetime
from database import Base

class PPEDetection(Base):
    __tablename__ = "ppe"

    id = Column(Integer, primary_key=True, index=True)
    workerId = Column(Integer, ForeignKey("watch.workerId"))
    ppe_id = Column(Integer, nullable=True)
    imageURL = Column(String(255))
    confidenceScore = Column(Float)
    helmet_on = Column(Integer, default=0)
    seatbelt_on = Column(Integer, default=0)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
