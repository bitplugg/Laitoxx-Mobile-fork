# -*- coding: utf-8 -*-
"""
LAITOXX Extended OSINT Tools
Additional tools from original LAITOXX for Android
"""

import requests
import json
import re
import hashlib
from urllib.parse import urlparse, quote
from bs4 import BeautifulSoup
import concurrent.futures
from collections import defaultdict


class ExtendedOSINT:
    """Extended OSINT tools from original LAITOXX"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def gmail_osint(self, email_prefix):
        """
        Gmail OSINT - get information about Gmail account
        Based on original LAITOXX gmail_osint tool
        """
        try:
            # Remove @gmail.com if present
            if '@' in email_prefix:
                email_prefix = email_prefix.split('@')[0]

            result = {
                'email_prefix': email_prefix,
                'full_email': f'{email_prefix}@gmail.com',
                'google_profile': None,
                'gaia_id': None,
                'last_edit': None,
                'has_custom_avatar': False,
                'maps_profile': None,
                'calendar_public': False
            }

            # Try Gmail OSINT API (если доступен)
            try:
                api_url = f'https://gmail-osint.activetk.jp/{email_prefix}'
                response = self.session.get(api_url, timeout=10)

                if response.status_code == 200:
                    data = response.json()
                    if 'gaia_id' in data:
                        result['gaia_id'] = data.get('gaia_id')
                        result['last_edit'] = data.get('last_edit')
                        result['has_custom_avatar'] = data.get('has_avatar', False)
            except:
                pass

            # Try Google profile search
            try:
                search_url = f'https://www.google.com/search?q={email_prefix}@gmail.com'
                response = self.session.get(search_url, timeout=10)

                if 'plus.google.com' in response.text or 'profiles.google.com' in response.text:
                    result['google_profile'] = True
            except:
                pass

            # Generate possible social media links
            result['social_media_links'] = {
                'google_plus': f'https://plus.google.com/+{email_prefix}',
                'google_photos': f'https://photos.google.com/u/{email_prefix}@gmail.com',
                'google_maps': f'https://www.google.com/maps/contrib/{email_prefix}',
                'youtube': f'https://www.youtube.com/@{email_prefix}'
            }

            return json.dumps(result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def mac_address_lookup(self, mac_address):
        """
        Enhanced MAC Address Lookup - find manufacturer with multiple sources
        Based on original LAITOXX mac_lookup tool with improvements
        """
        try:
            # Normalize and validate MAC address
            mac_str = mac_address.strip()
            clean = re.sub(r'[^0-9A-Fa-f]', '', mac_str.upper())

            if len(clean) != 12:
                return json.dumps({'error': 'Invalid MAC address format (must be 12 hex characters)'})

            # Normalize to XX:XX:XX:XX:XX:XX format
            normalized_mac = ':'.join(clean[i:i+2] for i in range(0, 12, 2))

            # Check MAC address type bits
            first_octet = int(clean[0:2], 16)
            is_multicast = (first_octet & 1) == 1
            is_local = (first_octet & 2) == 2  # Locally administered

            # Local checks
            local_info = {
                'mac_address': normalized_mac,
                'oui': normalized_mac[:8],
                'type': 'Locally Administered' if is_local else 'Globally Unique',
                'multicast': is_multicast
            }

            # Fetch from multiple API sources in parallel
            def fetch_maclookup_app(mac):
                try:
                    clean_mac = re.sub(r'[^0-9A-Fa-f]', '', mac.upper())
                    url = f"https://api.maclookup.app/v2/macs/{clean_mac}"
                    response = requests.get(url, timeout=10)
                    if response.status_code == 200:
                        data = response.json()
                        if data.get('found', False):
                            return data
                    return None
                except Exception:
                    return None

            def fetch_macvendors(mac):
                try:
                    url = f'https://api.macvendors.com/{mac}'
                    response = requests.get(url, timeout=10)
                    if response.status_code == 200:
                        return {'vendor': response.text, 'source': 'macvendors.com'}
                    return None
                except Exception:
                    return None

            results = []
            with concurrent.futures.ThreadPoolExecutor() as executor:
                future_maclookup = executor.submit(fetch_maclookup_app, normalized_mac)
                future_macvendors = executor.submit(fetch_macvendors, normalized_mac)

                api_data = future_maclookup.result()
                if api_data:
                    results.append(api_data)

                vendor_data = future_macvendors.result()
                if vendor_data:
                    results.append(vendor_data)

            # Collect data from all sources
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
                if 'blockStart' in data and 'blockEnd' in data:
                    collected['block'].add(f"{data['blockStart']} - {data['blockEnd']}")

            # Compile final result
            final_result = {
                'mac_address': normalized_mac,
                'local_info': local_info,
                'vendor_info': {
                    'vendor': ', '.join(sorted(collected['vendor'] or collected['company'])) if (collected['vendor'] or collected['company']) else 'Unknown',
                    'address': ', '.join(sorted(collected['address'])) if collected['address'] else None,
                    'country': ', '.join(sorted(collected['country'])) if collected['country'] else None,
                    'oui_block': ', '.join(sorted(collected['block'])) if collected['block'] else None,
                    'block_type': ', '.join(sorted(collected['type'])) if collected['type'] else None
                }
            }

            return json.dumps(final_result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def username_checker(self, username):
        """
        Check username availability across multiple platforms
        Based on original LAITOXX user_checker tool (29 platforms)
        """
        try:
            platforms = {
                # Social Media
                'Twitter': f'https://twitter.com/{username}',
                'Instagram': f'https://www.instagram.com/{username}',
                'Facebook': f'https://www.facebook.com/{username}',
                'TikTok': f'https://www.tiktok.com/@{username}',
                'VKontakte': f'https://vk.com/{username}',

                # Developer Platforms
                'GitHub': f'https://github.com/{username}',
                'GitLab': f'https://gitlab.com/{username}',
                'Pastebin': f'https://pastebin.com/u/{username}',

                # Streaming
                'Twitch': f'https://www.twitch.tv/{username}',
                'YouTube': f'https://www.youtube.com/@{username}',
                'Vimeo': f'https://vimeo.com/{username}',
                'Dailymotion': f'https://www.dailymotion.com/{username}',

                # Music
                'Spotify': f'https://open.spotify.com/user/{username}',
                'SoundCloud': f'https://soundcloud.com/{username}',
                'Last.fm': f'https://www.last.fm/user/{username}',

                # Gaming
                'Roblox': f'https://www.roblox.com/users/profile?username={username}',
                'Steam': f'https://steamcommunity.com/id/{username}',

                # Other Platforms
                'Reddit': f'https://www.reddit.com/user/{username}',
                'Pinterest': f'https://www.pinterest.com/{username}',
                'LinkedIn': f'https://www.linkedin.com/in/{username}',
                'Snapchat': f'https://www.snapchat.com/add/{username}',
                'Flickr': f'https://www.flickr.com/people/{username}',
                'Medium': f'https://medium.com/@{username}',
                'Patreon': f'https://www.patreon.com/{username}',
                'Tumblr': f'https://{username}.tumblr.com',
                'WordPress': f'https://{username}.wordpress.com',
                'DeviantArt': f'https://www.deviantart.com/{username}',
                'Keybase': f'https://keybase.io/{username}',
                'Dribbble': f'https://dribbble.com/{username}',
                'Behance': f'https://www.behance.net/{username}',
                'Bandcamp': f'https://{username}.bandcamp.com',
                'Telegram': f'https://t.me/{username}'
            }

            results = {
                'username': username,
                'checked': len(platforms),
                'found': [],
                'not_found': [],
                'unknown': []
            }

            for platform, url in platforms.items():
                try:
                    response = self.session.get(url, timeout=5, allow_redirects=True)

                    if response.status_code == 200:
                        # Additional check for some platforms
                        if platform == 'GitHub' and 'Not Found' in response.text:
                            results['not_found'].append(platform)
                        else:
                            results['found'].append({
                                'platform': platform,
                                'url': url
                            })
                    elif response.status_code == 404:
                        results['not_found'].append(platform)
                    else:
                        results['unknown'].append({
                            'platform': platform,
                            'status': response.status_code
                        })
                except:
                    results['unknown'].append({
                        'platform': platform,
                        'status': 'timeout/error'
                    })

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def google_dork_generator(self, keyword, operators):
        """
        Google Dork Query Generator
        Based on original LAITOXX google_osint tool

        operators: dict with operator:value pairs
        Example: {'site': 'example.com', 'filetype': 'pdf', 'intext': 'password'}
        """
        try:
            if isinstance(operators, str):
                operators = json.loads(operators)

            dork_parts = []

            # Add keyword if provided
            if keyword:
                dork_parts.append(keyword)

            # Add operators
            operator_map = {
                'site': lambda v: f'site:{v}',
                'inurl': lambda v: f'inurl:{v}',
                'intext': lambda v: f'intext:{v}',
                'intitle': lambda v: f'intitle:{v}',
                'filetype': lambda v: f'filetype:{v}',
                'ext': lambda v: f'ext:{v}',
                'allinurl': lambda v: f'allinurl:{v}',
                'allintitle': lambda v: f'allintitle:{v}',
                'allintext': lambda v: f'allintext:{v}',
                'cache': lambda v: f'cache:{v}',
                'link': lambda v: f'link:{v}',
                'related': lambda v: f'related:{v}',
                'info': lambda v: f'info:{v}',
                'define': lambda v: f'define:{v}',
                'stocks': lambda v: f'stocks:{v}',
                'map': lambda v: f'map:{v}',
                'movie': lambda v: f'movie:{v}',
                'weather': lambda v: f'weather:{v}',
                'source': lambda v: f'source:{v}',
                'before': lambda v: f'before:{v}',
                'after': lambda v: f'after:{v}'
            }

            for operator, value in operators.items():
                if operator in operator_map and value:
                    dork_parts.append(operator_map[operator](value))

            dork_query = ' '.join(dork_parts)

            result = {
                'query': dork_query,
                'encoded_query': quote(dork_query),
                'search_urls': {
                    'google': f'https://www.google.com/search?q={quote(dork_query)}',
                    'bing': f'https://www.bing.com/search?q={quote(dork_query)}',
                    'duckduckgo': f'https://duckduckgo.com/?q={quote(dork_query)}',
                    'yandex': f'https://yandex.com/search/?text={quote(dork_query)}'
                },
                'common_dorks': {
                    'exposed_files': f'site:{operators.get("site", "target.com")} ext:sql | ext:dbf | ext:mdb',
                    'config_files': f'site:{operators.get("site", "target.com")} ext:xml | ext:conf | ext:cnf | ext:reg | ext:inf | ext:rdp | ext:cfg | ext:txt | ext:ora | ext:ini',
                    'database_files': f'site:{operators.get("site", "target.com")} ext:sql | ext:dbf | ext:mdb',
                    'log_files': f'site:{operators.get("site", "target.com")} ext:log',
                    'backup_files': f'site:{operators.get("site", "target.com")} ext:bkf | ext:bkp | ext:bak | ext:old | ext:backup',
                    'login_pages': f'site:{operators.get("site", "target.com")} inurl:login | inurl:signin | intitle:Login | intitle:"sign in"',
                    'sql_errors': f'site:{operators.get("site", "target.com")} intext:"sql syntax near" | intext:"syntax error has occurred" | intext:"incorrect syntax near" | intext:"unexpected end of SQL command"',
                    'phpinfo': f'site:{operators.get("site", "target.com")} ext:php intitle:phpinfo "published by the PHP Group"'
                }
            }

            return json.dumps(result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def web_crawler(self, start_url, max_pages=10):
        """
        Simple web crawler to discover pages
        Based on original LAITOXX web_crawler tool
        """
        try:
            if not start_url.startswith('http'):
                start_url = 'https://' + start_url

            visited = set()
            to_visit = {start_url}
            found_pages = []

            base_domain = urlparse(start_url).netloc

            while to_visit and len(visited) < max_pages:
                current_url = to_visit.pop()

                if current_url in visited:
                    continue

                try:
                    response = self.session.get(current_url, timeout=10, allow_redirects=True)

                    if response.status_code == 200:
                        visited.add(current_url)
                        found_pages.append({
                            'url': current_url,
                            'status': response.status_code,
                            'title': self._extract_title(response.text)
                        })

                        # Extract links
                        soup = BeautifulSoup(response.content, 'html.parser')
                        for link in soup.find_all('a', href=True):
                            href = link['href']

                            # Convert relative URLs to absolute
                            if href.startswith('/'):
                                href = f"{urlparse(current_url).scheme}://{base_domain}{href}"
                            elif not href.startswith('http'):
                                continue

                            # Only follow links from same domain
                            if urlparse(href).netloc == base_domain:
                                to_visit.add(href)
                except:
                    continue

            result = {
                'start_url': start_url,
                'pages_crawled': len(visited),
                'max_pages': max_pages,
                'pages': found_pages
            }

            return json.dumps(result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def _extract_title(self, html):
        """Extract page title from HTML"""
        try:
            soup = BeautifulSoup(html, 'html.parser')
            title = soup.find('title')
            return title.string if title else 'No title'
        except:
            return 'Error extracting title'

    def subdomain_enum(self, domain):
        """
        Enhanced subdomain enumeration
        Uses crt.sh and additional sources
        """
        try:
            subdomains = set()

            # Method 1: crt.sh (SSL certificates)
            try:
                url = f'https://crt.sh/?q=%.{domain}&output=json'
                response = self.session.get(url, timeout=15)

                if response.status_code == 200:
                    data = response.json()
                    for entry in data:
                        name = entry.get('name_value', '')
                        if name:
                            for subdomain in name.split('\n'):
                                subdomain = subdomain.strip().lower()
                                if subdomain.endswith(domain):
                                    subdomains.add(subdomain)
            except:
                pass

            # Method 2: HackerTarget API
            try:
                url = f'https://api.hackertarget.com/hostsearch/?q={domain}'
                response = self.session.get(url, timeout=10)

                if response.status_code == 200 and 'error' not in response.text.lower():
                    lines = response.text.strip().split('\n')
                    for line in lines:
                        if ',' in line:
                            subdomain = line.split(',')[0].strip()
                            if subdomain.endswith(domain):
                                subdomains.add(subdomain)
            except:
                pass

            result = {
                'domain': domain,
                'subdomains_found': len(subdomains),
                'subdomains': sorted(list(subdomains))
            }

            return json.dumps(result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})


# Wrapper functions
def gmail_osint(email_prefix):
    """Gmail OSINT wrapper"""
    tool = ExtendedOSINT()
    return tool.gmail_osint(email_prefix)


def mac_lookup(mac_address):
    """MAC Address lookup wrapper"""
    tool = ExtendedOSINT()
    return tool.mac_address_lookup(mac_address)


def username_check(username):
    """Username checker wrapper"""
    tool = ExtendedOSINT()
    return tool.username_checker(username)


def google_dork(keyword, operators):
    """Google Dork generator wrapper"""
    tool = ExtendedOSINT()
    return tool.google_dork_generator(keyword, operators)


def web_crawl(start_url, max_pages=10):
    """Web crawler wrapper"""
    tool = ExtendedOSINT()
    return tool.web_crawler(start_url, max_pages)


def subdomain_find(domain):
    """Enhanced subdomain finder wrapper"""
    tool = ExtendedOSINT()
    return tool.subdomain_enum(domain)


def whatsapp_check(phone_number):
    """
    WhatsApp account checker without Selenium
    Uses multiple methods to verify account existence
    """
    try:
        from whatsapp_checker import WhatsAppChecker
        checker = WhatsAppChecker()
        return checker.check_all_methods(phone_number)
    except ImportError:
        # Fallback if whatsapp_checker module not found
        import json
        num = phone_number.lstrip('+')
        return json.dumps({
            'phone_number': phone_number,
            'verification_links': {
                'wa_me': f'https://wa.me/{num}',
                'api_send': f'https://api.whatsapp.com/send?phone={num}',
                'whatsapp_protocol': f'whatsapp://send?phone={num}'
            },
            'note': 'Open links manually to verify WhatsApp account'
        }, ensure_ascii=False)
