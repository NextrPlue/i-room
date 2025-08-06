# /utils/db.py

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from db.orm_models import Base
import os
from dotenv import load_dotenv

load_dotenv()

DB_URL = (
    f"mysql+pymysql://{os.getenv('DB_USER')}:{os.getenv('DB_PASS')}"
    f"@{os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}/{os.getenv('DB_NAME')}"
)