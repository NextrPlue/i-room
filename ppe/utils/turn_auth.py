# ppe/utils/turn_auth.py
import time, hmac, hashlib, base64
from ppe.config import get_settings

def make_turn_credential(prefix: str = "web"):
    s = get_settings()
    secret = s.TURN_REST_SECRET
    if not secret:
        # 원인 메시지를 명확히
        raise RuntimeError("TURN_REST_SECRET is empty. Check .env path & value.")

    expiry = int(time.time()) + int(s.TURN_CRED_TTL)
    username = f"{expiry}:{prefix}"
    digest = hmac.new(secret.encode(), username.encode(), hashlib.sha1).digest()
    credential = base64.b64encode(digest).decode()

    return {
        "username": username,
        "credential": credential,
        "ttl": s.TURN_CRED_TTL,
        "urls": [
            f"turn:{s.TURN_HOST}:3478?transport=udp",
            f"turn:{s.TURN_HOST}:3478?transport=tcp",
        ],
    }
