from fastapi import FastAPI

app = FastAPI() # FastAPI 인스턴스 생성

@app.get("/")
async def root():
    return {"message": "Hello World"}