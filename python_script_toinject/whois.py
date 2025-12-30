import requests
import json
import sys
from urllib.parse import quote

def query_rdap_bootstrap(base_url, resource_type, resource):
    url = f"{base_url}/{resource_type}/{quote(resource)}"
    headers = {"Accept": "application/rdap+json"}
    try:
        r = requests.get(url, headers=headers, timeout=15, allow_redirects=True)
        if r.status_code == 200:
            return r.json()
        else:
            print(f"{base_url} вернул код {r.status_code}")
    except Exception as e:
        print(f"Ошибка {base_url}: {e}")
    return None

def query_ripestat(resource):
    url = f"https://stat.ripe.net/data/whois/data.json?resource={quote(resource.upper())}"
    try:
        r = requests.get(url, timeout=15)
        if r.status_code == 200 and r.json().get("data", {}).get("records"):
            return r.json()
    except Exception as e:
        print(f"Ошибка RIPEstat: {e}")
    return None

def extract_readable(data_sources, resource_type, original_resource):
    info = {
        "Тип": resource_type.upper(),
        "Ресурс": original_resource.upper(),
        "Диапазон/Сеть": "Не указано",
        "Организация/Регистратор": "Не указано",
        "Страна": "Не указано",
        "Владелец/Регистрант": "Не указано",
        "Дата регистрации": "Не указано",
        "Дата обновления": "Не указано",
        "Дата истечения": "Не указано",
        "Статусы": [],
        "Nameservers": [],
        "Abuse контакт": "Не указано",
    }

    events = {}
    entities = []

    # Основной парсинг из RDAP (rdap.org или rdap.net)
    for key in data_sources:
        if "rdap" in key.lower():
            d = data_sources[key]

            # События (даты)
            for e in d.get("events", []):
                action = e.get("eventAction")
                date = e.get("eventDate", "")[:10] if e.get("eventDate") else ""
                if action:
                    events[action] = date

            # Сущности (registrar, registrant, abuse и т.д.)
            for ent in d.get("entities", []):
                roles = ent.get("roles", [])
                vcard = ent.get("vcardArray", [[]])[1] if ent.get("vcardArray") else []
                name = "Не указано"
                email = "Не указано"
                for item in vcard:
                    if item[0] == "fn":
                        name = item[3]
                    if item[0] == "email":
                        email = item[3]

                if "registrar" in roles:
                    info["Организация/Регистратор"] = name
                if "registrant" in roles:
                    info["Владелец/Регистрант"] = name or "Скрыто (privacy/GDPR)"
                if "abuse" in roles:
                    info["Abuse контакт"] = f"{email} ({name})" if email != "Не указано" else name

            # Страна, статусы, nameservers
            info["Страна"] = d.get("country", info["Страна"])

            if resource_type == "domain":
                info["Статусы"] = d.get("status", [])
                info["Nameservers"] = [ns.get("ldhName", "") for ns in d.get("nameservers", []) if ns.get("ldhName")]

            if resource_type in ["ip", "autnum"]:
                info["Диапазон/Сеть"] = f"{d.get('startAddress','')} - {d.get('endAddress','')}".strip(" -") or d.get("name", "Не указано")

    # Даты из событий
    info["Дата регистрации"] = events.get("registration", events.get("initial registration", "Не указано"))
    info["Дата обновления"] = events.get("last changed", events.get("last update", "Не указано"))
    info["Дата истечения"] = events.get("expiration", events.get("expiry", "Не указано"))

    # Дополнение из RIPEstat (если есть)
    if "ripestat" in data_sources:
        for group in data_sources["ripestat"].get("data", {}).get("records", []):
            rec = {item["key"]: item["value"] for item in group if isinstance(item, dict)}
            info["Организация/Регистратор"] = rec.get("org-name", rec.get("descr", info["Организация/Регистратор"]))
            info["Страна"] = rec.get("country", info["Страна"])
            if resource_type in ["ip", "autnum"]:
                info["Диапазон/Сеть"] = rec.get("inetnum", rec.get("inet6num", info["Диапазон/Сеть"]))

    return info

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Использование: python whois.py <IP | ASN | domain> [--history]")
        sys.exit(1)

    resource = sys.argv[1].strip().lower()
    history = "--history" in sys.argv

    # Определение типа ресурса
    if resource.startswith("as"):
        type_ = "autnum"
        clean_resource = resource[2:]
    elif "." in resource and not all(part.isdigit() or part == "" for part in resource.split(".")):
        type_ = "domain"
        clean_resource = resource
    else:
        type_ = "ip"
        clean_resource = resource

    print(f"Поиск данных для {resource.upper()}...\n")

    data_sources = {}
    raw_data = {}  # Для сохранения сырых ответов

    # 1. rdap.org — основной и самый надёжный
    rdap = query_rdap_bootstrap("https://rdap.org", type_, clean_resource)
    if rdap:
        data_sources["rdap_org"] = rdap
        raw_data["rdap.org"] = rdap

    # 2. rdap.net — фолбек
    if not rdap:
        rdap_net = query_rdap_bootstrap("https://www.rdap.net", type_, clean_resource)
        if rdap_net:
            data_sources["rdap_net"] = rdap_net
            raw_data["rdap.net"] = rdap_net

    # 3. RIPEstat — только для IP и ASN
    if type_ != "domain":
        ripe = query_ripestat(resource)
        if ripe:
            data_sources["ripestat"] = ripe
            raw_data["RIPEstat"] = ripe

    if not data_sources:
        print("Не удалось получить данные ни из одного источника.")
        sys.exit(1)

    result = extract_readable(data_sources, type_, resource)

    # === Красивый вывод ===
    print("=== WHOIS / RDAP информация ===")
    for key, value in result.items():
        if key == "Статусы" and value:
            print(f"{key}: {', '.join(value)}")
        elif key == "Nameservers" and value:
            print(f"{key}:")
            for ns in value:
                print(f"   - {ns}")
        elif key != "Дополнительно":
            print(f"{key}: {value}")

    print(f"\nИсточники: {', '.join(raw_data.keys())}")

    # === Сырые данные в JSON ===
    print("\n" + "="*50)
    print("=== Сырые данные (raw JSON) ===")
    print("="*50)
    for source_name, raw_json in raw_data.items():
        print(f"\n--- {source_name} ---")
        print(json.dumps(raw_json, indent=2, ensure_ascii=False))

    if history and type_ != "domain":
        print("\n\nИстория доступна только для IP/ASN в регионе APNIC — при необходимости могу добавить.")