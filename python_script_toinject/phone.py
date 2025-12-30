#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Объединённый OSINT‑скрипт по номеру телефона.
Работает на публичных веб‑страницах без API‑ключей.
"""
import re
import sys
import random
from pathlib import Path
import requests
import phonenumbers
from phonenumbers import carrier, timezone, geocoder  # Добавлено для исправления ошибки
from bs4 import BeautifulSoup

# Цвета для красивого вывода
GREEN = '\033[92m'
RED   = '\033[91m'
YELLOW= '\033[93m'
BLUE  = '\033[96m'
WHITE = '\033[97m'
RESET = '\033[0m'
BOLD  = '\033[1m'

# ====================== Общие вспомогательные функции ======================
def load_user_agents(file_name: str = "useragents.txt"):
    path = Path(file_name)
    if not path.exists():
        print(f"{RED}[ERR]{WHITE} Файл {file_name} не найден. Создайте его рядом со скриптом.")
        sys.exit(1)
    with path.open("r", encoding="utf-8", errors="ignore") as f:
        agents = [l.strip() for l in f if l.strip()]
    if not agents:
        print(f"{RED}[ERR]{WHITE} В {file_name} нет ни одной строки user-agent.")
        sys.exit(1)
    return agents

USER_AGENTS = None
def get_ua():
    global USER_AGENTS
    if USER_AGENTS is None:
        USER_AGENTS = load_user_agents()
    return random.choice(USER_AGENTS)

# ========================== Основной lookup =========================
def basic_number_info(phone_number: str) -> str:
    if not re.match(r'^\+[1-9]\d{1,14}$', phone_number):
        return f"{RED}Неверный формат номера. Пример: +441234567890{RESET}"

    try:
        parsed = phonenumbers.parse(phone_number)
    except phonenumbers.phonenumberutil.NumberParseException:
        return f"{RED}Не удалось распознать код страны.{RESET}"

    possible = phonenumbers.is_possible_number(parsed)
    valid    = phonenumbers.is_valid_number(parsed)
    country_code = str(parsed.country_code)
    region_code  = phonenumbers.region_code_for_number(parsed) or "N/A"
    operator = carrier.name_for_number(parsed, "en") or "N/A"
    timezones = timezone.time_zones_for_number(parsed)
    tz_str    = ", ".join(timezones) if timezones else "N/A"

    international = phonenumbers.format_number(parsed, phonenumbers.PhoneNumberFormat.INTERNATIONAL)

    block = f"""
{BOLD}{BLUE}Информация о номере {phone_number}{RESET}
┌──────────────────────────────────────────────────────┐
| Поле                 | Значение                             |
|──────────────────────────────────────────────────────|
| Возможный номер      | {possible}                           |
| Валидный номер       | {valid}                              |
| Код страны           | {country_code}                       |
| Код региона          | {region_code}                        |
| Оператор             | {operator}                           |
| Часовой пояс         | {tz_str}                             |
| Международный формат | {international}                      |
└──────────────────────────────────────────────────────┘

{YELLOW}Полезные ссылки и поисковые запросы{RESET}
  • Truecaller:     https://www.truecaller.com/search/{region_code.lower()}/{international.lstrip('+')}
  • IPQualityScore: https://www.ipqualityscore.com/reverse-phone-number-lookup/lookup/{region_code}/{international.lstrip('+')}
  • Google:         https://www.google.com/search?q={phone_number}
  • DuckDuckGo:     https://duckduckgo.com/?q={phone_number}
"""
    return block

# ========================== Репутация и спам =========================
def reputation_check(phone_number: str) -> str:
    result_lines = []

    # Tellows
    url = f"https://www.tellows.fr/num/%2B{phone_number.lstrip('+')}"
    try:
        r = requests.get(url, headers={"User-Agent": get_ua()}, timeout=15)
        if r.status_code == 200:
            soup = BeautifulSoup(r.text, "html.parser")
            block = soup.find("div", {"class": "col-lg-9"})
            h1s = block.find_all("h1") if block else []
            rep = h1s[0].text.strip() if h1s else "нет данных"
            result_lines.append(f"{GREEN}Репутация (tellows): {rep}{RESET}")
    except:
        pass

    # Spamcalls
    num = phone_number.lstrip("+")
    url = f"https://spamcalls.net/en/number/{num}"
    try:
        r = requests.get(url, headers={"User-Agent": get_ua()}, timeout=15)
        if r.status_code == 200:
            soup = BeautifulSoup(r.text, "html.parser")
            if soup.find("div", class_="report-body"):
                result_lines.append(f"{RED}Отмечен как СПАМ (spamcalls.net){RESET}")
            else:
                result_lines.append(f"{GREEN}Явных признаков спама не найдено (spamcalls.net){RESET}")
    except:
        pass

    # Free-lookup.net
    url = f"https://free-lookup.net/{num}"
    try:
        r = requests.get(url, headers={"User-Agent": get_ua()}, timeout=15)
        if r.status_code == 200:
            soup = BeautifulSoup(r.text, "html.parser")
            ul = soup.find("ul", class_="report-summary__list")
            if ul:
                divs = ul.find_all("div")
                info = {}
                for k, v in zip(divs[::2], divs[1::2]):
                    key = k.get_text(strip=True)
                    val = v.get_text(strip=True) or "Not found"
                    info[key] = val

                for key, val in info.items():
                    if val == "Not found":
                        continue
                    # Специальная обработка просмотров
                    if "Views count" in key or "Views" in key:
                        result_lines.append(f"{YELLOW}Интересовались ⚆: {val} раз{RESET}")
                    else:
                        result_lines.append(f"{GREEN}{key}: {val}{RESET}")
    except:
        pass

    if not result_lines:
        return f"{YELLOW}Дополнительная информация о репутации не найдена{RESET}"

    return "\n".join(result_lines)

# =================== WhatsApp lookup =================
def whatsapp_lookup(phone_number: str) -> str:
    try:
        from selenium import webdriver
        from selenium.webdriver.chrome.options import Options
        from selenium.webdriver.common.by import By
        from selenium.webdriver.support.ui import WebDriverWait
        from selenium.webdriver.support import expected_conditions as EC
        import time
    except ImportError:
        return f"{YELLOW}Selenium не установлен — проверка WhatsApp пропущена{RESET}"

    base_dir = Path(__file__).resolve().parent
    chrome_data_dir = base_dir / "ChromeData"
    chrome_data_dir.mkdir(exist_ok=True)

    options = Options()
    options.add_argument(f"user-data-dir={chrome_data_dir}")
    options.add_argument("--disable-infobars")
    options.add_argument("--window-size=1200,800")
    options.add_argument("--headless=new")

    try:
        driver = webdriver.Chrome(options=options)
    except Exception as e:
        return f"{RED}Не удалось запустить Chrome: {e}{RESET}"

    try:
        driver.get("https://web.whatsapp.com/")
        time.sleep(25)  # Время на сканирование QR при первом запуске

        num = phone_number.lstrip("+")
        driver.get(f"https://wa.me/{num}")

        try:
            btn = WebDriverWait(driver, 15).until(
                EC.element_to_be_clickable((By.XPATH, '//*[@id="action-button"]/span')))
            btn.click()
            time.sleep(2)
        except:
            pass

        try:
            header = WebDriverWait(driver, 20).until(
                EC.presence_of_element_located(
                    (By.XPATH, "//header//span[contains(@class,'selectable-text') or contains(@class,'_am_')]")
                )
            )
            name = header.text.strip()
            if name:
                return f"{GREEN}Имя в WhatsApp: {name}{RESET}"
            else:
                return f"{YELLOW}Контакт найден, но имя не указано{RESET}"
        except:
            return f"{YELLOW}Номер не зарегистрирован в WhatsApp или имя недоступно{RESET}"
    finally:
        driver.quit()

# ========================== Финальная агрегация ============================
def combined_lookup(phone_number: str, use_whatsapp: bool = False):
    print(f"{BLUE}[INFO]{WHITE} OSINT-поиск по номеру: {phone_number}{RESET}\n")

    print(basic_number_info(phone_number))
    print(f"\n{BOLD}{BLUE}Репутация и данные из открытых источников{RESET}")
    print(reputation_check(phone_number))

    if use_whatsapp:
        print(f"\n{BOLD}{BLUE}Проверка в WhatsApp{RESET}")
        print(whatsapp_lookup(phone_number))

    print(f"\n{GREEN}[DONE]{WHITE} Поиск завершён.{RESET}")

# =============================== CLI-интерфейс =============================
def main():
    if len(sys.argv) < 2:
        print(f"Использование: {sys.argv[0]} +<номер> [--wa]")
        print("Пример: python3 phone_osint.py +33612345678")
        print("Флаг --wa включает проверку имени через WhatsApp Web")
        sys.exit(1)

    number = sys.argv[1].strip()
    use_wa = "--wa" in sys.argv[2:]

    combined_lookup(number, use_whatsapp=use_wa)

if __name__ == "__main__":
    main()