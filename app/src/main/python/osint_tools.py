# -*- coding: utf-8 -*-
"""
LAITOXX OSINT Tools - Python Module
All advanced OSINT functionality from original LAITOXX
"""

import requests
import json
import whois
import socket
import dns.resolver
import phonenumbers
from phonenumbers import geocoder, carrier, timezone
from bs4 import BeautifulSoup
import validators
from urllib.parse import urlparse, urljoin
import re
import ipaddress
import concurrent.futures
from collections import defaultdict

class OSINTTools:
    """Advanced OSINT investigation tools"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def whois_lookup(self, domain):
        """Enhanced WHOIS/RDAP lookup for domains, IPs, and ASNs"""
        try:
            # Clean domain/resource
            resource = domain.replace('http://', '').replace('https://', '').split('/')[0].strip().lower()

            # Determine resource type
            if resource.startswith('as'):
                resource_type = 'autnum'
                clean_resource = resource[2:]
            elif '.' in resource and not all(part.isdigit() or part == '' for part in resource.split('.')):
                resource_type = 'domain'
                clean_resource = resource
            else:
                resource_type = 'ip'
                clean_resource = resource

            # Helper functions for RDAP queries
            def query_rdap_bootstrap(base_url, res_type, res):
                try:
                    from urllib.parse import quote
                    url = f"{base_url}/{res_type}/{quote(res)}"
                    headers = {"Accept": "application/rdap+json"}
                    response = requests.get(url, headers=headers, timeout=15, allow_redirects=True)
                    if response.status_code == 200:
                        return response.json()
                    return None
                except Exception:
                    return None

            def query_ripestat(res):
                try:
                    from urllib.parse import quote
                    url = f"https://stat.ripe.net/data/whois/data.json?resource={quote(res.upper())}"
                    response = requests.get(url, timeout=15)
                    if response.status_code == 200 and response.json().get("data", {}).get("records"):
                        return response.json()
                    return None
                except Exception:
                    return None

            # Query multiple sources in parallel
            data_sources = {}

            with concurrent.futures.ThreadPoolExecutor() as executor:
                future_rdap_org = executor.submit(query_rdap_bootstrap, "https://rdap.org", resource_type, clean_resource)
                future_rdap_net = executor.submit(query_rdap_bootstrap, "https://www.rdap.net", resource_type, clean_resource)
                future_ripe = None
                if resource_type != 'domain':
                    future_ripe = executor.submit(query_ripestat, resource)

                rdap_org = future_rdap_org.result()
                if rdap_org:
                    data_sources['rdap_org'] = rdap_org

                rdap_net = future_rdap_net.result()
                if rdap_net and not rdap_org:
                    data_sources['rdap_net'] = rdap_net

                if future_ripe:
                    ripe = future_ripe.result()
                    if ripe:
                        data_sources['ripestat'] = ripe

            if not data_sources:
                return json.dumps({'error': 'Failed to get data from any RDAP source'})

            # Extract readable information
            info = {
                'type': resource_type.upper(),
                'resource': resource.upper(),
                'network_range': None,
                'organization': None,
                'country': None,
                'registrant': None,
                'registration_date': None,
                'update_date': None,
                'expiration_date': None,
                'status': [],
                'nameservers': [],
                'abuse_contact': None
            }

            events = {}

            # Parse RDAP data
            for key in data_sources:
                if 'rdap' in key.lower():
                    d = data_sources[key]

                    # Events (dates)
                    for e in d.get('events', []):
                        action = e.get('eventAction')
                        date = e.get('eventDate', '')[:10] if e.get('eventDate') else ''
                        if action:
                            events[action] = date

                    # Entities (registrar, registrant, abuse, etc.)
                    for ent in d.get('entities', []):
                        roles = ent.get('roles', [])
                        vcard = ent.get('vcardArray', [[]])[1] if ent.get('vcardArray') else []
                        name = None
                        email = None
                        for item in vcard:
                            if item[0] == 'fn':
                                name = item[3]
                            if item[0] == 'email':
                                email = item[3]

                        if 'registrar' in roles and name:
                            info['organization'] = name
                        if 'registrant' in roles:
                            info['registrant'] = name or 'Hidden (GDPR/Privacy)'
                        if 'abuse' in roles:
                            info['abuse_contact'] = f"{email} ({name})" if email else name

                    # Country, status, nameservers
                    if d.get('country'):
                        info['country'] = d.get('country')

                    if resource_type == 'domain':
                        info['status'] = d.get('status', [])
                        info['nameservers'] = [ns.get('ldhName', '') for ns in d.get('nameservers', []) if ns.get('ldhName')]

                    if resource_type in ['ip', 'autnum']:
                        start = d.get('startAddress', '')
                        end = d.get('endAddress', '')
                        info['network_range'] = f"{start} - {end}".strip(' -') or d.get('name')

            # Dates from events
            info['registration_date'] = events.get('registration', events.get('initial registration'))
            info['update_date'] = events.get('last changed', events.get('last update'))
            info['expiration_date'] = events.get('expiration', events.get('expiry'))

            # Supplement from RIPEstat
            if 'ripestat' in data_sources:
                for group in data_sources['ripestat'].get('data', {}).get('records', []):
                    rec = {item['key']: item['value'] for item in group if isinstance(item, dict)}
                    if not info['organization']:
                        info['organization'] = rec.get('org-name', rec.get('descr'))
                    if not info['country']:
                        info['country'] = rec.get('country')
                    if resource_type in ['ip', 'autnum'] and not info['network_range']:
                        info['network_range'] = rec.get('inetnum', rec.get('inet6num'))

            return json.dumps(info, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def advanced_phone_lookup(self, phone_number):
        """Enhanced phone number analysis with reputation checking"""
        try:
            # Parse phone number
            parsed = phonenumbers.parse(phone_number, None)

            # Get information
            country = geocoder.description_for_number(parsed, 'en')
            carrier_name = carrier.name_for_number(parsed, 'en')
            timezones = timezone.time_zones_for_number(parsed)
            region_code = phonenumbers.region_code_for_number(parsed) or "N/A"

            # Number type
            number_type = phonenumbers.number_type(parsed)
            type_names = {
                0: 'FIXED_LINE',
                1: 'MOBILE',
                2: 'FIXED_LINE_OR_MOBILE',
                3: 'TOLL_FREE',
                4: 'PREMIUM_RATE',
                5: 'SHARED_COST',
                6: 'VOIP',
                7: 'PERSONAL_NUMBER',
                8: 'PAGER',
                9: 'UAN',
                10: 'VOICEMAIL'
            }

            international = phonenumbers.format_number(parsed, phonenumbers.PhoneNumberFormat.INTERNATIONAL)

            # Reputation check from multiple sources
            reputation_info = {}
            num_stripped = phone_number.lstrip("+")

            # Check Tellows (French site for phone reputation)
            def check_tellows():
                try:
                    url = f"https://www.tellows.fr/num/%2B{num_stripped}"
                    response = requests.get(url, timeout=10)
                    if response.status_code == 200:
                        from bs4 import BeautifulSoup
                        soup = BeautifulSoup(response.text, 'html.parser')
                        block = soup.find("div", {"class": "col-lg-9"})
                        if block:
                            h1s = block.find_all("h1")
                            if h1s:
                                return h1s[0].text.strip()
                    return None
                except Exception:
                    return None

            # Check Spamcalls
            def check_spamcalls():
                try:
                    url = f"https://spamcalls.net/en/number/{num_stripped}"
                    response = requests.get(url, timeout=10)
                    if response.status_code == 200:
                        from bs4 import BeautifulSoup
                        soup = BeautifulSoup(response.text, 'html.parser')
                        if soup.find("div", class_="report-body"):
                            return "Marked as SPAM"
                        else:
                            return "No spam reports"
                    return None
                except Exception:
                    return None

            # Check WhatsApp presence (alternative to Selenium)
            def check_whatsapp_simple():
                """
                Simple WhatsApp check without Selenium
                Uses wa.me redirect behavior
                """
                try:
                    wa_url = f"https://wa.me/{num_stripped}"
                    response = requests.head(wa_url, timeout=10, allow_redirects=False)

                    # If account exists, WhatsApp redirects
                    if response.status_code in [301, 302]:
                        return "Account likely exists"
                    elif response.status_code == 200:
                        return "Check manually via link"
                    else:
                        return "Unknown"
                except Exception:
                    return None

            # Run reputation checks in parallel
            with concurrent.futures.ThreadPoolExecutor() as executor:
                future_tellows = executor.submit(check_tellows)
                future_spam = executor.submit(check_spamcalls)
                future_whatsapp = executor.submit(check_whatsapp_simple)

                tellows_result = future_tellows.result()
                if tellows_result:
                    reputation_info['tellows'] = tellows_result

                spam_result = future_spam.result()
                if spam_result:
                    reputation_info['spamcalls'] = spam_result

                whatsapp_result = future_whatsapp.result()
                if whatsapp_result:
                    reputation_info['whatsapp'] = whatsapp_result

            # Useful links
            num_stripped = phone_number.lstrip("+")
            useful_links = {
                'truecaller': f'https://www.truecaller.com/search/{region_code.lower()}/{international.lstrip("+")}',
                'ipqualityscore': f'https://www.ipqualityscore.com/reverse-phone-number-lookup/lookup/{region_code}/{international.lstrip("+")}',
                'google_search': f'https://www.google.com/search?q={phone_number}',
                'duckduckgo_search': f'https://duckduckgo.com/?q={phone_number}',
                'whatsapp_web': f'https://wa.me/{num_stripped}',
                'whatsapp_check': f'https://api.whatsapp.com/send?phone={num_stripped}'
            }

            result = {
                'number': phone_number,
                'formatted': international,
                'country': country,
                'region_code': region_code,
                'carrier': carrier_name if carrier_name else 'Unknown',
                'timezones': list(timezones),
                'type': type_names.get(number_type, 'UNKNOWN'),
                'valid': phonenumbers.is_valid_number(parsed),
                'possible': phonenumbers.is_possible_number(parsed),
                'country_code': parsed.country_code,
                'national_number': parsed.national_number,
                'reputation': reputation_info,
                'useful_links': useful_links
            }

            return json.dumps(result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def dns_advanced(self, domain):
        """Advanced DNS lookup with multiple record types"""
        try:
            results = {
                'domain': domain,
                'records': {}
            }

            # Record types to query
            record_types = ['A', 'AAAA', 'MX', 'NS', 'TXT', 'CNAME', 'SOA']

            for record_type in record_types:
                try:
                    answers = dns.resolver.resolve(domain, record_type)
                    results['records'][record_type] = [str(rdata) for rdata in answers]
                except:
                    results['records'][record_type] = []

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def email_osint(self, email):
        """Email OSINT - gather information about email"""
        try:
            # Validate email
            if not validators.email(email):
                return json.dumps({'error': 'Invalid email format'})

            domain = email.split('@')[1]
            username = email.split('@')[0]

            results = {
                'email': email,
                'username': username,
                'domain': domain,
                'valid_format': True,
                'mx_records': [],
                'domain_exists': False,
                'common_patterns': []
            }

            # Check MX records
            try:
                mx_records = dns.resolver.resolve(domain, 'MX')
                results['mx_records'] = [str(r.exchange) for r in mx_records]
                results['domain_exists'] = True
            except:
                pass

            # Generate common email patterns
            if ' ' in username or '.' in username:
                name_parts = username.replace('.', ' ').split()
                if len(name_parts) >= 2:
                    first, last = name_parts[0], name_parts[-1]
                    results['common_patterns'] = [
                        f"{first}@{domain}",
                        f"{last}@{domain}",
                        f"{first}.{last}@{domain}",
                        f"{last}.{first}@{domain}",
                        f"{first[0]}{last}@{domain}",
                        f"{first}{last[0]}@{domain}"
                    ]

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def reverse_ip_lookup(self, ip):
        """Reverse IP lookup - find domains hosted on IP"""
        try:
            # Try reverse DNS
            try:
                hostname = socket.gethostbyaddr(ip)[0]
            except:
                hostname = None

            results = {
                'ip': ip,
                'hostname': hostname,
                'domains': []
            }

            # Try HackerTarget API (free)
            try:
                url = f'https://api.hackertarget.com/reverseiplookup/?q={ip}'
                response = self.session.get(url, timeout=10)
                if response.status_code == 200 and 'error' not in response.text.lower():
                    domains = response.text.strip().split('\n')
                    results['domains'] = [d for d in domains if d and not d.startswith('API')]
            except:
                pass

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def website_tech_stack(self, url):
        """Detect website technology stack"""
        try:
            if not url.startswith('http'):
                url = 'https://' + url

            response = self.session.get(url, timeout=10, allow_redirects=True)
            soup = BeautifulSoup(response.content, 'html.parser')

            tech_stack = {
                'url': url,
                'status_code': response.status_code,
                'server': response.headers.get('Server', 'Unknown'),
                'powered_by': response.headers.get('X-Powered-By', 'Unknown'),
                'technologies': [],
                'frameworks': [],
                'cms': None,
                'analytics': [],
                'meta_tags': {}
            }

            # Detect CMS
            html_lower = response.text.lower()
            if 'wp-content' in html_lower or 'wordpress' in html_lower:
                tech_stack['cms'] = 'WordPress'
            elif 'joomla' in html_lower:
                tech_stack['cms'] = 'Joomla'
            elif 'drupal' in html_lower:
                tech_stack['cms'] = 'Drupal'
            elif 'shopify' in html_lower:
                tech_stack['cms'] = 'Shopify'

            # Detect frameworks
            if 'react' in html_lower:
                tech_stack['frameworks'].append('React')
            if 'vue' in html_lower:
                tech_stack['frameworks'].append('Vue.js')
            if 'angular' in html_lower:
                tech_stack['frameworks'].append('Angular')
            if 'bootstrap' in html_lower:
                tech_stack['frameworks'].append('Bootstrap')
            if 'jquery' in html_lower:
                tech_stack['frameworks'].append('jQuery')

            # Detect analytics
            if 'google-analytics' in html_lower or 'gtag' in html_lower:
                tech_stack['analytics'].append('Google Analytics')
            if 'facebook.com/tr' in html_lower:
                tech_stack['analytics'].append('Facebook Pixel')

            # Get meta tags
            for meta in soup.find_all('meta'):
                name = meta.get('name', meta.get('property', ''))
                content = meta.get('content', '')
                if name and content:
                    tech_stack['meta_tags'][name] = content

            return json.dumps(tech_stack, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def social_media_search(self, username):
        """Search for username across popular social media platforms"""
        try:
            platforms = {
                'GitHub': f'https://github.com/{username}',
                'Twitter': f'https://twitter.com/{username}',
                'Instagram': f'https://instagram.com/{username}',
                'Facebook': f'https://facebook.com/{username}',
                'LinkedIn': f'https://linkedin.com/in/{username}',
                'Reddit': f'https://reddit.com/user/{username}',
                'YouTube': f'https://youtube.com/@{username}',
                'TikTok': f'https://tiktok.com/@{username}',
                'Medium': f'https://medium.com/@{username}',
                'Pinterest': f'https://pinterest.com/{username}',
                'Telegram': f'https://t.me/{username}',
                'VK': f'https://vk.com/{username}'
            }

            results = {
                'username': username,
                'found': [],
                'not_found': [],
                'error': []
            }

            for platform, url in platforms.items():
                try:
                    response = self.session.get(url, timeout=5, allow_redirects=True)
                    if response.status_code == 200:
                        results['found'].append({
                            'platform': platform,
                            'url': url,
                            'status': 'Found'
                        })
                    elif response.status_code == 404:
                        results['not_found'].append(platform)
                    else:
                        results['error'].append({
                            'platform': platform,
                            'status_code': response.status_code
                        })
                except Exception as e:
                    results['error'].append({
                        'platform': platform,
                        'error': str(e)
                    })

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def enhanced_ip_lookup(self, target_ip):
        """Enhanced IP geolocation with multiple sources"""
        try:
            # Local checks using ipaddress module
            local_info = {}
            try:
                ip = ipaddress.ip_address(target_ip)
                local_info = {
                    'ip_address': target_ip,
                    'version': f'IPv{ip.version}',
                    'is_private': ip.is_private,
                    'is_loopback': ip.is_loopback,
                    'is_reserved': ip.is_reserved,
                    'is_multicast': ip.is_multicast,
                    'is_unspecified': ip.is_unspecified
                }
            except Exception:
                pass

            # Fetch from multiple geolocation APIs in parallel
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

            functions = [fetch_ip_api, fetch_freeipapi, fetch_ipapi_co, fetch_ipwhois]
            results = []

            with concurrent.futures.ThreadPoolExecutor() as executor:
                future_to_func = {executor.submit(func, target_ip): func.__name__ for func in functions}
                for future in concurrent.futures.as_completed(future_to_func):
                    data = future.result()
                    if data and data.get('status') != 'fail':
                        results.append(data)

            if not results:
                return json.dumps({'error': 'Failed to get geolocation data from any service', 'local_info': local_info})

            # Collect unique values from all sources
            collected = defaultdict(set)

            for data in results:
                # Country
                country = data.get('country') or data.get('countryName')
                country_code = data.get('countryCode') or data.get('country_code')
                if country:
                    if country_code:
                        collected['country'].add(f"{country} ({country_code})")
                    else:
                        collected['country'].add(country)

                # Region
                region_name = (data.get('regionName') or data.get('region_name') or data.get('region'))
                region_code = data.get('region') or data.get('regionCode') or data.get('region_code')
                if region_name:
                    if region_code:
                        collected['region'].add(f"{region_name} ({region_code})")
                    else:
                        collected['region'].add(region_name)

                # City
                city = data.get('city') or data.get('cityName')
                if city:
                    collected['city'].add(city)

                # Postal/Zip code
                zip_code = data.get('zip') or data.get('zipCode') or data.get('postal')
                if zip_code:
                    collected['zip'].add(str(zip_code))

                # Coordinates
                lat = data.get('lat') or data.get('latitude')
                lon = data.get('lon') or data.get('longitude')
                if lat is not None and lon is not None:
                    collected['coords'].add(f"{float(lat):.4f}, {float(lon):.4f}")

                # Timezone
                timezone = data.get('timezone') or data.get('time_zone')
                if timezone:
                    collected['timezone'].add(timezone)

                # ISP/Provider/Organization
                isp = (data.get('isp') or data.get('org') or data.get('asnOrganization') or
                       data.get('organization') or data.get('asname'))
                if isp:
                    collected['provider'].add(isp.strip())

                # AS number
                as_num = data.get('as') or data.get('asn')
                if as_num and 'AS' in str(as_num):
                    collected['as'].add(str(as_num))

                # Proxy/VPN detection
                proxy = (data.get('isProxy') or data.get('proxy') or
                         data.get('vpn') or data.get('tor'))
                if proxy:
                    collected['proxy'].add(True)

            # Compile final result
            final_result = {
                'ip': target_ip,
                'local_info': local_info,
                'geolocation': {
                    'country': ', '.join(sorted(collected['country'])) if collected['country'] else None,
                    'region': ', '.join(sorted(collected['region'])) if collected['region'] else None,
                    'city': ', '.join(sorted(collected['city'])) if collected['city'] else None,
                    'postal': ', '.join(sorted(collected['zip'])) if collected['zip'] else None,
                    'coordinates': ', '.join(sorted(collected['coords'])) if collected['coords'] else None,
                    'timezone': ', '.join(sorted(collected['timezone'])) if collected['timezone'] else None,
                    'provider': ', '.join(sorted(collected['provider'])) if collected['provider'] else None,
                    'as': ', '.join(sorted(collected['as'])) if collected['as'] else None,
                    'proxy_vpn_detected': True if collected['proxy'] else False
                }
            }

            return json.dumps(final_result, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})


def whois_lookup(domain):
    """Wrapper function for WHOIS lookup"""
    tool = OSINTTools()
    return tool.whois_lookup(domain)


def phone_lookup(phone):
    """Wrapper function for phone lookup"""
    tool = OSINTTools()
    return tool.advanced_phone_lookup(phone)


def dns_lookup(domain):
    """Wrapper function for DNS lookup"""
    tool = OSINTTools()
    return tool.dns_advanced(domain)


def email_osint(email):
    """Wrapper function for email OSINT"""
    tool = OSINTTools()
    return tool.email_osint(email)


def reverse_ip(ip):
    """Wrapper function for reverse IP"""
    tool = OSINTTools()
    return tool.reverse_ip_lookup(ip)


def tech_stack(url):
    """Wrapper function for tech stack detection"""
    tool = OSINTTools()
    return tool.website_tech_stack(url)


def social_search(username):
    """Wrapper function for social media search"""
    tool = OSINTTools()
    return tool.social_media_search(username)


def enhanced_ip_info(ip):
    """Wrapper function for enhanced IP lookup"""
    tool = OSINTTools()
    return tool.enhanced_ip_lookup(ip)
