from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# Docker mysql info
DATABASE_URL = "mysql+pymysql://root:password@localhost:3306/iroom_ppe"
# DB engine create
engine = create_engine(DATABASE_URL, echo=True)
# Session management
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
# ORM Base
Base = declarative_base()

# DB Session dependencies insertion
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Table auto increment
def create_tables():
    Base.metadata.create_all(bind=engine)
