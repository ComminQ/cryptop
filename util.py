import json


def read_config_file() -> dict | None:
  file = "config.json"
  with open(file, "r") as f:
    # Load json
    data = json.load(f)
    return data
  return None