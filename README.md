# LAITOXX - Android Security & OSINT Toolkit

<div align="center">

![LAITOXX](https://img.shields.io/badge/LAITOXX-Security%20Toolkit-red?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-34-green?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple?style=for-the-badge&logo=kotlin)
![Python](https://img.shields.io/badge/Python-3.12.0-blue?style=for-the-badge&logo=python)

**Professional Android toolkit for security testing and OSINT investigations**

</div>

---

## 📋 Table of Contents


  - [About](#about)
  - [Features](#features)
  - [Requirements](#requirements)
  - [Installation](#installation)
  - [Tools](#tools)
  - [Legal Notice](#legal-notice)

  - [О проекте](#о-проекте)
  - [Возможности](#возможности)
  - [Требования](#требования-1)
  - [Установка](#установка)
  - [Инструменты](#инструменты)
  - [Правовая информация](#правовая-информация)

---


## 📱 About

LAITOXX is a professional Android application for information security specialists, OSINT researchers, and penetration testers. The application provides a modern and user-friendly interface for performing various security analysis tasks.

### ✨ Key Features

- 🎨 **Modern UI** - Material Design 3 with dark theme
- ⚡ **High Performance** - Jetpack Compose + Kotlin Coroutines
- 🐍 **Python Integration** - 25+ additional tools via Chaquopy
- 🛡️ **Security** - For legal and educational purposes only
- 📊 **Wide Functionality** - 19+ tools for various tasks
- 🔄 **Asynchronous** - All operations run without blocking UI

---

## 🛠️ Tools

### 🔍 OSINT Tools
- **IP Information** - Detailed IP address and domain information
- **Subdomain Finder** - Find subdomains via crt.sh
- **Email Validator** - Email address validation
- **Phone Lookup** - Phone number analysis
- **WHOIS Lookup** - Domain registration information (Python)
- **Username Checker** - Check username across 29+ platforms (Python)
- **Gmail OSINT** - Gmail account investigation (Python)
- **MAC Address Lookup** - Manufacturer lookup (Python)

### 🌐 Network Tools
- **Port Scanner** - Scan open ports on target hosts
- **DNS Lookup** - Resolve domain names to IP addresses
- **Ping** - Check host availability and latency
- **Advanced DNS Lookup** - All DNS record types (Python)

### 🔐 Web Security
- **URL Checker** - Check URL status and SSL certificates
- **Admin Finder** - Find admin panels on websites
- **SQL Injection Scanner** - Test for SQL injection (Python)
- **XSS Scanner** - Find XSS vulnerabilities (Python)
- **SSL Certificate Analyzer** - Detailed SSL analysis (Python)
- **Security Headers Checker** - Check security headers (Python)
- **Directory Fuzzing** - Find hidden directories (Python)

### 🔧 Utilities
- **Text Transformer** - Text transformations (uppercase, leet speak, URL encode)
- **Hash Generator** - Generate MD5, SHA-1, SHA-256 hashes
- **Base64 Encoder** - Base64 encoding/decoding
- **Password Generator** - Generate secure random passwords
- **Google Dork Generator** - Generate Google Dork queries (Python)
- **Web Crawler** - Crawl websites with link extraction (Python)

---

## 📋 Requirements

### Build Requirements

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** (Oracle JDK or OpenJDK)
- **Python 3.12.0** (for build-time compilation of Python modules)
- **Android SDK 34**
- **Minimum 8 GB RAM**
- **5 GB free disk space**

### Runtime Requirements

- **Minimum Android version**: 7.0 (API 24)
- **Target Android version**: 14 (API 34)
- **Active internet connection** (for most tools)

---

## 🚀 Installation

### 1. Install Prerequisites

#### Install JDK 17

**Windows:**
```bash
# Download from https://adoptium.net/
# Or use Chocolatey
choco install temurin17
```

**macOS:**
```bash
brew install openjdk@17
```

**Linux:**
```bash
sudo apt install openjdk-17-jdk
```

#### Install Python 3.12.0

**Windows:**
```bash
# Download from https://www.python.org/downloads/
# Or use Chocolatey
choco install python --version=3.12.0
```

**macOS:**
```bash
brew install python@3.12
```

**Linux:**
```bash
sudo apt install python3.12 python3.12-dev
```

### 2. Clone Repository

```bash
git clone https://github.com/yourusername/laitoxx-android.git
cd laitoxx-android
```

### 3. Open in Android Studio

1. Launch **Android Studio**
2. **File → Open** and select `laitoxx_android` folder
3. Wait for Gradle sync to complete (5-10 minutes on first run)

### 4. Build & Run

**Option A: Via Android Studio**
```
Run → Run 'app' (or Shift+F10)
```

**Option B: Via Command Line**

Windows:
```bash
.\gradlew assemblePy38Debug
```

Linux/macOS:
```bash
./gradlew assemblePy38Debug
```

APK location: `app/build/outputs/apk/py38/debug/app-py38-debug.apk`

---

## ⚠️ Legal Notice

### IMPORTANT: For Educational Purposes Only

This application is intended **EXCLUSIVELY** for:

- ✅ Educational purposes
- ✅ Authorized security testing
- ✅ Cybersecurity research
- ✅ Testing your own systems

### Prohibited Uses:

- ❌ Unauthorized system access
- ❌ Privacy violations
- ❌ Any illegal activity
- ❌ Attacks without explicit permission

**Users are fully responsible for the use of this application. Developers are not liable for any misuse.**

---

## 📞 Contact

- **Telegram**: t.me/laitoxx

---


## 📱 О проекте

LAITOXX - это профессиональное Android-приложение для специалистов по информационной безопасности, исследователей OSINT и пентестеров. Приложение предоставляет современный и удобный интерфейс для выполнения различных задач по анализу безопасности.

### ✨ Возможности

- 🎨 **Современный UI** - Material Design 3 с темной темой
- ⚡ **Высокая производительность** - Jetpack Compose + Kotlin Coroutines
- 🐍 **Интеграция Python** - 25+ дополнительных инструментов через Chaquopy
- 🛡️ **Безопасность** - Только для легальных и образовательных целей
- 📊 **Широкий функционал** - 19+ инструментов для различных задач
- 🔄 **Асинхронность** - Все операции выполняются без блокировки UI

---

## 🛠️ Инструменты

### 🔍 OSINT инструменты
- **IP Information** - Детальная информация об IP-адресах и доменах
- **Subdomain Finder** - Поиск поддоменов через crt.sh
- **Email Validator** - Валидация email-адресов
- **Phone Lookup** - Анализ телефонных номеров
- **WHOIS Lookup** - Регистрационная информация о домене (Python)
- **Username Checker** - Проверка username на 29+ платформах (Python)
- **Gmail OSINT** - Исследование Gmail аккаунтов (Python)
- **MAC Address Lookup** - Поиск производителя по MAC адресу (Python)

### 🌐 Сетевые инструменты
- **Port Scanner** - Сканирование открытых портов
- **DNS Lookup** - Разрешение доменных имен в IP
- **Ping** - Проверка доступности хоста
- **Advanced DNS Lookup** - Все типы DNS записей (Python)

### 🔐 Веб-безопасность
- **URL Checker** - Проверка статуса URL и SSL-сертификатов
- **Admin Finder** - Поиск админ-панелей
- **SQL Injection Scanner** - Тестирование на SQL injection (Python)
- **XSS Scanner** - Поиск уязвимостей XSS (Python)
- **SSL Certificate Analyzer** - Детальный анализ SSL (Python)
- **Security Headers Checker** - Проверка security заголовков (Python)
- **Directory Fuzzing** - Поиск скрытых директорий (Python)

### 🔧 Утилиты
- **Text Transformer** - Преобразование текста (uppercase, leet speak, URL encode)
- **Hash Generator** - Генерация MD5, SHA-1, SHA-256 хешей
- **Base64 Encoder** - Кодирование/декодирование Base64
- **Password Generator** - Генерация безопасных паролей
- **Google Dork Generator** - Генератор Google Dork запросов (Python)
- **Web Crawler** - Краулинг сайтов (Python)

---

## 📋 Требования

### Требования для сборки

- **Android Studio** Hedgehog (2023.1.1) или новее
- **JDK 17** (Oracle JDK или OpenJDK)
- **Python 3.12.0** (для компиляции Python модулей)
- **Android SDK 34**
- **Минимум 8 GB RAM**
- **5 GB свободного места** на диске

### Требования для работы

- **Минимальная версия Android**: 7.0 (API 24)
- **Целевая версия Android**: 14 (API 34)
- **Активное подключение к интернету** (для большинства инструментов)

---

## 🚀 Установка

### 1. Установите необходимое ПО

#### Установка JDK 17

**Windows:**
```bash
# Скачайте с https://adoptium.net/
# Или используйте Chocolatey
choco install temurin17
```

**macOS:**
```bash
brew install openjdk@17
```

**Linux:**
```bash
sudo apt install openjdk-17-jdk
```

#### Установка Python 3.12.0

**Windows:**
```bash
# Скачайте с https://www.python.org/downloads/
# Или используйте Chocolatey
choco install python --version=3.12.0
```

**macOS:**
```bash
brew install python@3.12
```

**Linux:**
```bash
sudo apt install python3.12 python3.12-dev
```

### 2. Клонирование репозитория

```bash
git clone https://github.com/yourusername/laitoxx-android.git
cd laitoxx-android
```

### 3. Открытие в Android Studio

1. Запустите **Android Studio**
2. **File → Open** и выберите папку `laitoxx_android`
3. Дождитесь синхронизации Gradle (5-10 минут при первом запуске)

### 4. Сборка и запуск

**Вариант А: Через Android Studio**
```
Run → Run 'app' (или Shift+F10)
```

**Вариант Б: Через командную строку**

Windows:
```bash
.\gradlew assemblePy38Debug
```

Linux/macOS:
```bash
./gradlew assemblePy38Debug
```

Расположение APK: `app/build/outputs/apk/py38/debug/app-py38-debug.apk`

---

## ⚠️ Правовая информация

### ВАЖНО: Только для образовательных целей

Это приложение предназначено **ИСКЛЮЧИТЕЛЬНО** для:

- ✅ Образовательных целей
- ✅ Авторизованного тестирования безопасности
- ✅ Исследований в области кибербезопасности
- ✅ Тестирования собственных систем

### Запрещено использовать для:

- ❌ Несанкционированного доступа к системам
- ❌ Нарушения конфиденциальности
- ❌ Любой незаконной деятельности
- ❌ Атак на системы без явного разрешения

**Пользователь несет полную ответственность за использование этого приложения. Разработчики не несут ответственности за любое неправомерное использование.**

---

## 📞 Контакты

- **Telegram**: t.me/laitoxx

---

<div align="center">

**Made with ❤️ for the Security Community**

⭐ Star this project if you find it useful!

</div>
