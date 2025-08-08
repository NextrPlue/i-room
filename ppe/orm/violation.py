from sqlalchemy import Column, BigInteger, Integer, Text, Float, DateTime, ForeignKey, Double
from ppe.database import Base
import datetime

class Violation(Base):
    __tablename__ = "violation"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    worker_id = Column(Integer, ForeignKey("watch.worker_id", ondelete="CASCADE"), nullable=False)
    # ppe_id = Column(BigInteger, nullable=True)
    image_url = Column(Text, nullable=False)
    latitude = Column(Double, nullable=False)
    longitude = Column(Double, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow, nullable=False)
    helmet_on_count = Column(Integer, default=0)
    seatbelt_on_count = Column(Integer, default=0)
