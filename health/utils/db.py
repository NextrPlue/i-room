# /utils/db.py

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from health.db.orm_models import Base
import os
from dotenv import load_dotenv
from typing import Iterator

load_dotenv()   # 환경변수 로딩 (.env 파일의 환경변수를 로드)

# DB 연결 구성
DB_URL = (
    f"mysql+pymysql://{os.getenv('DB_USER')}:{os.getenv('DB_PASS')}"
    f"@{os.getenv('DB_HOST')}:{os.getenv('DB_PORT')}/{os.getenv('DB_NAME')}"
)
engine = create_engine(DB_URL)  # 엔진 생성 (DB 연결 객체)
SessionLocal = sessionmaker(    # 세션 팩토리 생성 -> 독립적인 DB 세션 객체 생성
    autocommit=False,   # 트랙잭션을 수동으로 제어
    autoflush=False,    # 쿼리 실행 전에 자동 flush 비활성화
    bind=engine
)

# 테이블 자동 생성 함수 (Base에 등록된 모든 ORM 모델들의 테이블을 DB에 생성, 앱 시작 시 한 번 실행)
def init_db():
    Base.metadata.create_all(bind=engine)

# 의존성 주입용 DB 세션 함수 -> FastAPI의 Depends(get_session)에서 사용됨, 요청마다 독립된 DB 세션을 생성해서 반환
def get_session() -> Iterator[Session]:
    db = SessionLocal()
    try:
        yield db    # dependency context manager 패턴
    finally:
        db.close()