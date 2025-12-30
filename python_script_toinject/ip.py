import requests
import ipaddress
import concurrent.futures
from collections import defaultdict

def get_public_ip():
    try:
        response = requests.get('https://api.ipify.org?format=json', timeout=10)
        response.raise_for_status()
        return response.json()['ip']
    except Exception:
        return None

# ==================== Запросы к сервисам ====================

def fetch_ip_api(ip):
    try:
        response = requests.get(f'http://ip-api.com/json/{ip}', timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception:
        return None

def fetch_freeipapi(ip):
    try:
        response = requests.get(f'https://freeipapi.com/api/json/{ip}', timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception:
        return None

def fetch_ipapi_co(ip):
    try:
        response = requests.get(f'https://ipapi.co/{ip}/json/', timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception:
        return None

def fetch_ipwhois(ip):
    try:
        response = requests.get(f'https://ipwhois.io/json/{ip}', timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception:
        return None

# ==================== Локальные проверки ====================

def format_local_checks(ip_str):
    try:
        ip = ipaddress.ip_address(ip_str)
        print("Локальные проверки (модуль ipaddress):")
        print(f"   IP адрес:       {ip_str}")
        print(f"   Версия:         IPv{ip.version}")
        print(f"   Приватный:      {'Да' if ip.is_private else 'Нет'}")
        print(f"   Loopback:       {'Да' if ip.is_loopback else 'Нет'}")
        print(f"   Зарезервирован: {'Да' if ip.is_reserved else 'Нет'}")
        print(f"   Multicast:      {'Да' if ip.is_multicast else 'Нет'}")
        print(f"   Неопределён:    {'Да' if ip.is_unspecified else 'Нет'}")
    except Exception as e:
        print(f"Ошибка локальных проверок: {e}")

# ==================== Основная логика ====================

def main(target_ip=None):
    if target_ip is None:
        print("Определяем ваш публичный IP...")
        target_ip = get_public_ip()
        if not target_ip:
            print("❌ Не удалось определить публичный IP и получить данные от гео-сервисов.")
            return
        print(f"Ваш публичный IP: {target_ip}\n")
    else:
        print(f"Анализируем IP: {target_ip}\n")

    # Локальные проверки
    format_local_checks(target_ip)
    print("\n" + "="*60 + "\n")

    # Параллельно запрашиваем все сервисы
    functions = [fetch_ip_api, fetch_freeipapi, fetch_ipapi_co, fetch_ipwhois]
    results = []

    with concurrent.futures.ThreadPoolExecutor() as executor:
        future_to_func = {executor.submit(func, target_ip): func.__name__ for func in functions}
        for future in concurrent.futures.as_completed(future_to_func):
            data = future.result()
            if data and data.get('status') != 'fail':  # ip-api.com возвращает status: fail при ошибке
                results.append(data)

    if not results:
        print("❌ Не удалось получить геолокационные данные ни от одного сервиса.")
        print("\n" + "="*60)
        return

    # Собираем уникальные значения по ключам
    collected = defaultdict(set)

    for data in results:
        # Страна
        country = data.get('country') or data.get('countryName')
        country_code = data.get('countryCode') or data.get('country_code')
        if country:
            if country_code:
                collected['country'].add(f"{country} ({country_code})")
            else:
                collected['country'].add(country)

        # Регион
        region_name = (data.get('regionName') or data.get('region_name') or
                       data.get('regionName') or data.get('region'))
        region_code = data.get('region') or data.get('regionCode') or data.get('region_code')
        if region_name:
            if region_code:
                collected['region'].add(f"{region_name} ({region_code})")
            else:
                collected['region'].add(region_name)

        # Город
        city = data.get('city') or data.get('cityName')
        if city:
            collected['city'].add(city)

        # Почтовый индекс
        zip_code = data.get('zip') or data.get('zipCode') or data.get('postal')
        if zip_code:
            collected['zip'].add(zip_code)

        # Координаты
        lat = data.get('lat') or data.get('latitude')
        lon = data.get('lon') or data.get('longitude')
        if lat is not None and lon is not None:
            collected['coords'].add(f"{float(lat):.4f}, {float(lon):.4f}")

        # Часовой пояс
        timezone = data.get('timezone') or data.get('time_zone')
        if timezone:
            collected['timezone'].add(timezone)

        # Провайдер / Организация / ISP
        isp = (data.get('isp') or data.get('org') or data.get('asnOrganization') or
               data.get('organization') or data.get('asname'))
        if isp:
            collected['provider'].add(isp.strip())

        # AS
        as_num = data.get('as') or data.get('asn')
        if as_num and 'AS' in str(as_num):
            collected['as'].add(as_num)

        # Прокси/VPN
        proxy = (data.get('isProxy') or data.get('proxy') or
                 data.get('vpn') or data.get('tor'))
        if proxy:
            collected['proxy'].add("Да")

    # Выводим объединённые данные
    print("🌍 Геолокация по IP (данные от нескольких источников):")
    print(f"   Страна:         {', '.join(sorted(collected['country']) or ['—'])}")
    print(f"   Регион:         {', '.join(sorted(collected['region']) or ['—'])}")
    print(f"   Город:          {', '.join(sorted(collected['city']) or ['—'])}")
    print(f"   Почтовый индекс:{', '.join(sorted(collected['zip']) or ['—'])}")
    print(f"   Координаты:     {', '.join(sorted(collected['coords']) or ['—'])}")
    print(f"   Часовой пояс:   {', '.join(sorted(collected['timezone']) or ['—'])}")
    print(f"   Провайдер:      {', '.join(sorted(collected['provider']) or ['—'])}")
    if collected['as']:
        print(f"   AS:             {', '.join(sorted(collected['as']))}")
    if 'Да' in collected['proxy']:
        print("   Прокси/VPN/Tor: Обнаружен (по данным некоторых источников)")

    print("\n" + "="*60)
    print("Анализ завершён.")

if __name__ == "__main__":
    # main()  # для вашего текущего IP
    main("8.8.8.8")  # пример