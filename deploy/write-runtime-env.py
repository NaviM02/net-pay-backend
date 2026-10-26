#!/usr/bin/env python3
"""Read one SSM SecureString JSON on stdin and create restricted Compose env files."""
import json
import os
import pathlib
import sys

allowed = {
    "DB_PASSWORD", "JWT_SECRET", "FRONTEND_BASE_URL", "CORS_ALLOWED_ORIGINS",
    "PASSWORD_RESET_EXPIRATION_MINUTES", "CLOUDINARY_CLOUD_NAME", "CLOUDINARY_API_KEY",
    "CLOUDINARY_API_SECRET", "CLOUDINARY_FOLDER", "CLOUDINARY_UPLOADS_FOLDER",
    "GMAIL_CLIENT_ID", "GMAIL_CLIENT_SECRET", "GMAIL_REFRESH_TOKEN", "GMAIL_FROM_EMAIL",
}
try:
    values = json.load(sys.stdin)
    if not isinstance(values, dict) or set(values) - allowed:
        raise ValueError
    if any(not isinstance(value, str) or "\x00" in value or "\n" in value or "\r" in value
           for value in values.values()):
        raise ValueError
    if len(values.get("DB_PASSWORD", "")) < 32 or len(values.get("JWT_SECRET", "")) < 32:
        raise ValueError
    if not values.get("FRONTEND_BASE_URL", "").startswith("https://"):
        raise ValueError
    origins = values.get("CORS_ALLOWED_ORIGINS", "").split(",")
    if not origins or any(not origin.startswith("https://") or "*" in origin for origin in origins):
        raise ValueError
except (ValueError, TypeError):
    sys.exit("Invalid SSM JSON: check allowed keys, required values and secret lengths.")

output_dir = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path("/etc/netpay")

def write_env(name: str, entries: dict[str, str]) -> None:
    target = output_dir / name
    temporary = target.with_suffix(".tmp")
    temporary.write_text("".join(f"{key}={json.dumps(value)}\n" for key, value in entries.items()))
    os.chmod(temporary, 0o600)
    temporary.replace(target)

write_env("db.env", {"POSTGRES_PASSWORD": values["DB_PASSWORD"]})
app_values = {key: value for key, value in values.items() if key != "DB_PASSWORD"}
app_values["DB_PASSWORD"] = values["DB_PASSWORD"]
write_env("app.env", app_values)
