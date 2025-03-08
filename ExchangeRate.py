from dataclasses import dataclass
from typing import Dict


@dataclass
class ExchangeRate:
    result: str
    base_code: str
    conversion_rates: Dict[str, float]

