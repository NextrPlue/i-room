from sqlalchemy import Column, BigInteger, Integer, String, Text, DateTime
from database import Base
import datetime

class PPE(Base):
    __tablename__ = "ppe"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    ppe_id = Column(BigInteger, unique=True, nullable=False)
    image_url = Column(Text)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow, nullable=False)
    helmet_on_count = Column(Integer, default=0)
    seatbelt_on_count = Column(Integer, default=0)
