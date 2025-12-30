import requests
import concurrent.futures
import re
from mac_vendor_lookup import MacLookup  # ← Правильный импорт!
from collections import defaultdict

# Инициализируем локальный lookup
local_lookup = MacLookup()

def fetch_maclookup_app(mac):
    """Запрос к публичному API MACLookup.app"""
    try:
        # Убираем все разделители и приводим к верхнему регистру
        clean_mac = re.sub(r'[^0-9A-Fa-f]', '', mac.upper())
        if len(clean_mac) != 12:
            return None
        url = f"https://api.maclookup.app/v2/macs/{clean_mac}"
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            data = response.json()
            if data.get('found', False):
                return data
        return None
    except Exception:
        return None

def local_mac_lookup(mac):
    """Локальный поиск вендора через mac-vendor-lookup"""
    try:
        vendor = local_lookup.lookup(mac)
        return {"vendor": vendor, "source": "local"}
    except Exception:
        return None

def normalize_and_validate_mac(mac_str):
    """Нормализация и валидация MAC без внешних библиотек"""
    # Принимаем форматы: XX:XX:XX:XX:XX:XX, XX-XX-XX-XX-XX-XX, XXXX.XXXX.XXXX, без разделителей
    mac_str = mac_str.strip()
    clean = re.sub(r'[^0-9A-Fa-f]', '', mac_str.upper())
    if len(clean) != 12:
        return None

    # Нормализуем к формату XX:XX:XX:XX:XX:XX
    normalized = ':'.join(clean[i:i+2] for i in range(0, 12, 2))

    # Проверки битов
    first_octet = int(clean[0:2], 16)
    is_multicast = (first_octet & 1) == 1
    is_local = (first_octet & 2) == 2  # второй бит = локально администрируемый

    return normalized, is_local, is_multicast

def main(target_mac=None):
    if target_mac is None:
        print("Введите MAC-адрес для анализа (пример: 44:38:39:ff:ef:57)")
        target_mac = input("MAC: ").strip()
        if not target_mac:
            print("MAC-адрес не введён.")
            return

    print(f"Анализируем MAC-адрес: {target_mac}\n")

    result = normalize_and_validate_mac(target_mac)
    if not result:
        print("❌ Некорректный формат MAC-адреса (должно быть 12 hex-символов).")
        return

    normalized_mac, is_local, is_multicast = result

    print("Локальные проверки:")
    print(f"   MAC-адрес:      {normalized_mac}")
    print(f"   OUI (первые 3 октета): {normalized_mac[:8]}")
    print(f"   Тип:            {'Локально администрируемый' if is_local else 'Глобально уникальный'}")
    print(f"   Multicast:      {'Да' if is_multicast else 'Нет'}")

    # Попробуем получить вендора локально (из OUI)
    try:
        local_vendor = local_lookup.lookup(normalized_mac)
        print(f"   Производитель (OUI, локально): {local_vendor}")
    except:
        print("   Производитель (OUI, локально): Неизвестен")

    print("\n" + "="*60 + "\n")

    # Параллельный сбор данных
    results = []

    with concurrent.futures.ThreadPoolExecutor() as executor:
        future_api = executor.submit(fetch_maclookup_app, target_mac)
        future_local = executor.submit(local_mac_lookup, normalized_mac)

        api_data = future_api.result()
        if api_data:
            results.append(api_data)

        local_data = future_local.result()
        if local_data:
            results.append(local_data)

    if not results:
        print("❌ Не удалось получить дополнительную информацию о производителе.")
        print("\n" + "="*60)
        return

    # Собираем данные
    collected = defaultdict(set)

    for data in results:
        if 'vendor' in data:
            collected['vendor'].add(data['vendor'])
        if 'company' in data:
            collected['company'].add(data['company'])
        if 'address' in data:
            address = data['address'].replace('\n', ', ')
            collected['address'].add(address)
        if 'country' in data:
            collected['country'].add(data['country'])
        if 'type' in data:
            collected['type'].add(data['type'])
        if 'block_start' in data and 'block_end' in data:
            collected['block'].add(f"{data['block_start']} — {data['block_end']}")

    print("Производитель и регистрация MAC (по данным источников):")
    vendors = sorted(collected['vendor'] or collected['company'] or ['—'])
    print(f"   Производитель:  {', '.join(vendors)}")

    if collected['address']:
        print(f"   Адрес:          {', '.join(sorted(collected['address']))}")
    if collected['country']:
        print(f"   Страна:         {', '.join(sorted(collected['country']))}")
    if collected['block']:
        print(f"   Диапазон OUI:   {', '.join(sorted(collected['block']))}")
    if collected['type']:
        print(f"   Тип блока:      {', '.join(sorted(collected['type']))}")

    print("\n" + "="*60)
    print("Анализ завершён.")

if __name__ == "__main__":
    main()  # интерактивный ввод
    # Или напрямую: main("44:38:39:ff:ef:57")