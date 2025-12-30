# -*- coding: utf-8 -*-
"""
LAITOXX Security Tools - Python Module
Advanced security scanning and testing tools
"""

import requests
import json
import socket
import re
from bs4 import BeautifulSoup
from urllib.parse import urlparse, urljoin, quote
import ssl
import OpenSSL
from datetime import datetime

class SecurityTools:
    """Advanced security scanning tools"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def sql_injection_scan(self, url):
        """Test for SQL injection vulnerabilities"""
        try:
            payloads = [
                "'",
                "' OR '1'='1",
                "' OR '1'='1' --",
                "' OR '1'='1' /*",
                "admin' --",
                "admin' #",
                "' UNION SELECT NULL--",
                "1' AND '1'='1",
                "' AND 1=1--"
            ]

            results = {
                'url': url,
                'vulnerable': False,
                'findings': [],
                'tested_payloads': len(payloads)
            }

            # Get baseline response
            try:
                baseline = self.session.get(url, timeout=10)
                baseline_length = len(baseline.text)
            except:
                return json.dumps({'error': 'Could not connect to URL'})

            for payload in payloads:
                try:
                    # Test in query parameter
                    test_url = url + ('&' if '?' in url else '?') + f'test={quote(payload)}'
                    response = self.session.get(test_url, timeout=5)

                    # Check for SQL error patterns
                    sql_errors = [
                        'sql syntax', 'mysql', 'mysqli', 'postgresql',
                        'ora-', 'mssql', 'sqlite', 'odbc', 'jdbc',
                        'warning: mysql', 'error in your sql syntax',
                        'unclosed quotation mark'
                    ]

                    response_lower = response.text.lower()
                    for error in sql_errors:
                        if error in response_lower:
                            results['vulnerable'] = True
                            results['findings'].append({
                                'payload': payload,
                                'error_found': error,
                                'url': test_url
                            })
                            break

                    # Check for significant length difference
                    if abs(len(response.text) - baseline_length) > 500:
                        results['findings'].append({
                            'payload': payload,
                            'note': 'Significant response length difference',
                            'baseline': baseline_length,
                            'response': len(response.text)
                        })

                except Exception as e:
                    continue

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def xss_scanner(self, url):
        """Test for Cross-Site Scripting vulnerabilities"""
        try:
            payloads = [
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "<svg/onload=alert('XSS')>",
                "<iframe src=javascript:alert('XSS')>",
                "<body onload=alert('XSS')>",
                "javascript:alert('XSS')",
                "<input onfocus=alert('XSS') autofocus>",
                "<marquee onstart=alert('XSS')>"
            ]

            results = {
                'url': url,
                'vulnerable': False,
                'findings': [],
                'tested_payloads': len(payloads)
            }

            for payload in payloads:
                try:
                    # Test in query parameter
                    test_url = url + ('&' if '?' in url else '?') + f'test={quote(payload)}'
                    response = self.session.get(test_url, timeout=5)

                    # Check if payload is reflected in response
                    if payload in response.text or payload.replace("'", '"') in response.text:
                        results['vulnerable'] = True
                        results['findings'].append({
                            'payload': payload,
                            'reflected': True,
                            'note': 'Payload reflected in response without encoding'
                        })

                except Exception as e:
                    continue

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def ssl_certificate_check(self, domain):
        """Analyze SSL/TLS certificate"""
        try:
            # Remove protocol if present
            domain = domain.replace('https://', '').replace('http://', '').split('/')[0]

            context = ssl.create_default_context()
            with socket.create_connection((domain, 443), timeout=10) as sock:
                with context.wrap_socket(sock, server_hostname=domain) as ssock:
                    cert_bin = ssock.getpeercert(True)
                    cert = OpenSSL.crypto.load_certificate(OpenSSL.crypto.FILETYPE_ASN1, cert_bin)

                    # Extract certificate info
                    subject = dict(x[0] for x in cert.get_subject().get_components())
                    issuer = dict(x[0] for x in cert.get_issuer().get_components())

                    results = {
                        'domain': domain,
                        'subject': {k.decode(): v.decode() for k, v in subject.items()},
                        'issuer': {k.decode(): v.decode() for k, v in issuer.items()},
                        'version': cert.get_version(),
                        'serial_number': cert.get_serial_number(),
                        'not_before': cert.get_notBefore().decode(),
                        'not_after': cert.get_notAfter().decode(),
                        'has_expired': cert.has_expired(),
                        'signature_algorithm': cert.get_signature_algorithm().decode(),
                        'san': []
                    }

                    # Get Subject Alternative Names
                    for i in range(cert.get_extension_count()):
                        ext = cert.get_extension(i)
                        if 'subjectAltName' in str(ext.get_short_name()):
                            results['san'] = str(ext).split(', ')

                    # Check if cert is valid
                    not_after = datetime.strptime(results['not_after'], '%Y%m%d%H%M%SZ')
                    days_until_expiry = (not_after - datetime.now()).days
                    results['days_until_expiry'] = days_until_expiry
                    results['will_expire_soon'] = days_until_expiry < 30

                    return json.dumps(results, ensure_ascii=False)

        except Exception as e:
            return json.dumps({'error': str(e)})

    def security_headers_check(self, url):
        """Check security headers"""
        try:
            if not url.startswith('http'):
                url = 'https://' + url

            response = self.session.get(url, timeout=10, allow_redirects=True)

            security_headers = {
                'Strict-Transport-Security': 'HSTS - Forces HTTPS',
                'Content-Security-Policy': 'CSP - Prevents XSS',
                'X-Frame-Options': 'Prevents Clickjacking',
                'X-Content-Type-Options': 'Prevents MIME sniffing',
                'X-XSS-Protection': 'XSS Filter',
                'Referrer-Policy': 'Controls referrer information',
                'Permissions-Policy': 'Feature Policy',
                'X-Permitted-Cross-Domain-Policies': 'Cross-domain policy'
            }

            results = {
                'url': url,
                'headers_present': {},
                'headers_missing': [],
                'security_score': 0,
                'max_score': len(security_headers)
            }

            for header, description in security_headers.items():
                if header in response.headers:
                    results['headers_present'][header] = {
                        'value': response.headers[header],
                        'description': description
                    }
                    results['security_score'] += 1
                else:
                    results['headers_missing'].append({
                        'header': header,
                        'description': description
                    })

            # Calculate percentage
            results['security_percentage'] = (results['security_score'] / results['max_score']) * 100

            # Check for insecure headers
            insecure_headers = []
            if 'Server' in response.headers:
                insecure_headers.append({
                    'header': 'Server',
                    'value': response.headers['Server'],
                    'risk': 'Information disclosure'
                })
            if 'X-Powered-By' in response.headers:
                insecure_headers.append({
                    'header': 'X-Powered-By',
                    'value': response.headers['X-Powered-By'],
                    'risk': 'Technology disclosure'
                })

            results['insecure_headers'] = insecure_headers

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def robots_txt_analyzer(self, url):
        """Analyze robots.txt for interesting paths"""
        try:
            if not url.startswith('http'):
                url = 'https://' + url

            parsed = urlparse(url)
            robots_url = f"{parsed.scheme}://{parsed.netloc}/robots.txt"

            response = self.session.get(robots_url, timeout=10)

            if response.status_code == 200:
                disallowed_paths = []
                allowed_paths = []
                sitemaps = []

                for line in response.text.split('\n'):
                    line = line.strip()
                    if line.startswith('Disallow:'):
                        path = line.split(':', 1)[1].strip()
                        if path:
                            disallowed_paths.append(path)
                    elif line.startswith('Allow:'):
                        path = line.split(':', 1)[1].strip()
                        if path:
                            allowed_paths.append(path)
                    elif line.startswith('Sitemap:'):
                        sitemap = line.split(':', 1)[1].strip()
                        sitemaps.append(sitemap)

                results = {
                    'url': robots_url,
                    'found': True,
                    'disallowed_paths': disallowed_paths[:50],  # Limit to 50
                    'allowed_paths': allowed_paths[:50],
                    'sitemaps': sitemaps,
                    'interesting_paths': [p for p in disallowed_paths if any(
                        keyword in p.lower() for keyword in ['admin', 'login', 'api', 'config', 'backup', 'test']
                    )]
                }

                return json.dumps(results, ensure_ascii=False)
            else:
                return json.dumps({'found': False, 'url': robots_url})

        except Exception as e:
            return json.dumps({'error': str(e)})

    def directory_fuzzing(self, url, wordlist=None):
        """Simple directory fuzzing"""
        try:
            if not url.startswith('http'):
                url = 'https://' + url

            url = url.rstrip('/')

            # Common directories if no wordlist provided
            if not wordlist:
                wordlist = [
                    'admin', 'api', 'backup', 'config', 'dashboard',
                    'login', 'panel', 'test', 'dev', 'staging',
                    'wp-admin', 'wp-content', 'uploads', 'images',
                    'css', 'js', 'assets', 'static', 'media'
                ]

            results = {
                'url': url,
                'found': [],
                'not_found': 0,
                'errors': 0
            }

            for directory in wordlist[:30]:  # Limit to 30 to avoid timeout
                try:
                    test_url = f"{url}/{directory}"
                    response = self.session.get(test_url, timeout=3, allow_redirects=False)

                    if response.status_code in [200, 301, 302, 403]:
                        results['found'].append({
                            'path': directory,
                            'url': test_url,
                            'status': response.status_code,
                            'size': len(response.content)
                        })
                    else:
                        results['not_found'] += 1

                except:
                    results['errors'] += 1

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})


# Wrapper functions for easy Kotlin integration
def sql_scan(url):
    tool = SecurityTools()
    return tool.sql_injection_scan(url)


def xss_scan(url):
    tool = SecurityTools()
    return tool.xss_scanner(url)


def ssl_check(domain):
    tool = SecurityTools()
    return tool.ssl_certificate_check(domain)


def security_headers(url):
    tool = SecurityTools()
    return tool.security_headers_check(url)


def robots_analyzer(url):
    tool = SecurityTools()
    return tool.robots_txt_analyzer(url)


def dir_fuzz(url):
    tool = SecurityTools()
    return tool.directory_fuzzing(url)
