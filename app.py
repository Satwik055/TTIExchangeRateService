import time
from dataclasses import dataclass
from typing import Dict
import requests
from supabase import *
import schedule
import logging


@dataclass
class ExchangeRate:
    result: str
    base_code: str
    conversion_rates: Dict[str, float]


formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")

handler = logging.StreamHandler()
handler.setLevel(logging.INFO)
handler.setFormatter(formatter)

logger = logging.getLogger("my_logger")
logger.setLevel(logging.INFO)
logger.addHandler(handler)

SUPABASE_ACCESS_TOKEN = "sbp_6a882e10989b5ec87bc36f286c7c69044eca8d29"
SUPABASE_URL = 'https://fqsnwalptczelvhiwohd.supabase.co'
SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZxc253YWxwdGN6ZWx2aGl3b2hkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mzg2NzI4MTYsImV4cCI6MjA1NDI0ODgxNn0.TzD0KcPnEJz0DvLYxUmK68PeDuNy47sU0jRlyhAls-I'


def get_exchange_rates() -> ExchangeRate:
    API_KEY = '9ca0a8eb16bea042cd93cdf7'
    url = f'https://v6.exchangerate-api.com/v6/{API_KEY}/latest/INR'

    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        exchange_rate = ExchangeRate(
            result=data.get('result'),
            base_code=data.get('base_code'),
            conversion_rates=data.get('conversion_rates')
        )
        return exchange_rate
    else:
        print(f'Failed to retrieve data. Status code: {response.status_code}')
        print(f'Response: {response.text}')


def update_tti_rates():
    supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
    exchange_rates = get_exchange_rates()

    usd = "{:.2f}".format(1 / exchange_rates.conversion_rates['USD'])
    eur = "{:.2f}".format(1 / exchange_rates.conversion_rates['EUR'])
    cad = "{:.2f}".format(1 / exchange_rates.conversion_rates['CAD'])
    aud = "{:.2f}".format(1 / exchange_rates.conversion_rates['AUD'])
    gbp = "{:.2f}".format(1 / exchange_rates.conversion_rates['GBP'])

    rates = {
        'EUR': eur,
        'GBP': gbp,
        'AUD': aud,
        'CAD': cad,
        'USD': usd,
        'BANK': usd
    }

    for row_id, new_price in rates.items():
        try:
            response = supabase.table('exchange_rate').update({'tti': new_price}).eq('currency_name', row_id).execute()
            if response.data:
                logger.info(f"Successfully updated {row_id} with new price {new_price}")
            else:
                logger.error(f"No rows were updated for row {row_id}. Check if the row exists.")

        except Exception as e:
            logger.error(f"Error updating row {row_id}: {str(e)}")


schedule.every(30).minutes.do(update_tti_rates)

if __name__ == "__main__":
    while True:
        logger.info("Heartbeat")
        schedule.run_pending()
        time.sleep(1)
