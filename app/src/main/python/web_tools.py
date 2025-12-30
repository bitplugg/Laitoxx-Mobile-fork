# -*- coding: utf-8 -*-
"""
LAITOXX Web Tools - Python Module
Web crawling, scraping and analysis
"""

import requests
import json
from bs4 import BeautifulSoup
from urllib.parse import urlparse, urljoin
import re

class WebTools:
    """Web crawling and analysis tools"""

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def web_crawler(self, start_url, max_pages=20):
        """Crawl website and extract links"""
        try:
            visited = set()
            to_visit = [start_url]
            all_links = []

            parsed_start = urlparse(start_url)
            base_domain = parsed_start.netloc

            count = 0
            while to_visit and count < max_pages:
                url = to_visit.pop(0)

                if url in visited:
                    continue

                try:
                    response = self.session.get(url, timeout=5)
                    visited.add(url)
                    count += 1

                    if 'text/html' not in response.headers.get('Content-Type', ''):
                        continue

                    soup = BeautifulSoup(response.content, 'html.parser')

                    # Extract all links
                    for link in soup.find_all('a', href=True):
                        href = link['href']
                        full_url = urljoin(url, href)

                        # Only crawl same domain
                        parsed = urlparse(full_url)
                        if parsed.netloc == base_domain and full_url not in visited:
                            to_visit.append(full_url)
                            all_links.append(full_url)

                except:
                    continue

            results = {
                'start_url': start_url,
                'pages_crawled': len(visited),
                'links_found': len(set(all_links)),
                'visited_urls': list(visited)[:50],  # Limit to 50
                'all_links': list(set(all_links))[:100]  # Limit to 100
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_emails(self, url):
        """Extract email addresses from webpage"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            # Email regex pattern
            email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'

            # Extract from HTML
            emails = set(re.findall(email_pattern, response.text))

            # Extract from mailto links
            for link in soup.find_all('a', href=True):
                if link['href'].startswith('mailto:'):
                    email = link['href'].replace('mailto:', '').split('?')[0]
                    emails.add(email)

            results = {
                'url': url,
                'emails_found': len(emails),
                'emails': sorted(list(emails))
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_phone_numbers(self, url):
        """Extract phone numbers from webpage"""
        try:
            response = self.session.get(url, timeout=10)

            # Phone number patterns
            patterns = [
                r'\+?\d{1,3}[-.\s]?\(?\d{1,4}\)?[-.\s]?\d{1,4}[-.\s]?\d{1,9}',
                r'\(\d{3}\)\s*\d{3}[-.\s]?\d{4}',
                r'\d{3}[-.\s]\d{3}[-.\s]\d{4}'
            ]

            phones = set()
            for pattern in patterns:
                matches = re.findall(pattern, response.text)
                phones.update(matches)

            results = {
                'url': url,
                'phones_found': len(phones),
                'phones': sorted(list(phones))
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_social_links(self, url):
        """Extract social media links from webpage"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            social_patterns = {
                'Facebook': r'facebook\.com/[^/\s]+',
                'Twitter': r'twitter\.com/[^/\s]+',
                'Instagram': r'instagram\.com/[^/\s]+',
                'LinkedIn': r'linkedin\.com/(in|company)/[^/\s]+',
                'YouTube': r'youtube\.com/(channel|c|user)/[^/\s]+',
                'GitHub': r'github\.com/[^/\s]+',
                'Telegram': r't\.me/[^/\s]+',
                'TikTok': r'tiktok\.com/@[^/\s]+'
            }

            results = {
                'url': url,
                'social_links': {}
            }

            for platform, pattern in social_patterns.items():
                matches = set(re.findall(pattern, response.text, re.IGNORECASE))
                if matches:
                    results['social_links'][platform] = list(matches)

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_forms(self, url):
        """Extract and analyze forms from webpage"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            forms = []
            for form in soup.find_all('form'):
                form_data = {
                    'action': form.get('action', ''),
                    'method': form.get('method', 'GET').upper(),
                    'inputs': [],
                    'has_password': False,
                    'has_file_upload': False
                }

                # Analyze inputs
                for input_tag in form.find_all(['input', 'textarea', 'select']):
                    input_data = {
                        'type': input_tag.get('type', 'text'),
                        'name': input_tag.get('name', ''),
                        'required': input_tag.has_attr('required')
                    }
                    form_data['inputs'].append(input_data)

                    if input_data['type'] == 'password':
                        form_data['has_password'] = True
                    if input_data['type'] == 'file':
                        form_data['has_file_upload'] = True

                forms.append(form_data)

            results = {
                'url': url,
                'forms_found': len(forms),
                'forms': forms
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_js_files(self, url):
        """Extract JavaScript files from webpage"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            js_files = set()

            # Find <script> tags with src
            for script in soup.find_all('script', src=True):
                js_url = urljoin(url, script['src'])
                js_files.add(js_url)

            # Find inline script references
            inline_scripts = soup.find_all('script', src=False)

            results = {
                'url': url,
                'external_js_count': len(js_files),
                'inline_scripts_count': len(inline_scripts),
                'js_files': list(js_files)
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def extract_css_files(self, url):
        """Extract CSS files from webpage"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            css_files = set()

            # Find <link rel="stylesheet">
            for link in soup.find_all('link', rel='stylesheet'):
                css_url = urljoin(url, link.get('href', ''))
                css_files.add(css_url)

            # Find inline styles
            style_tags = soup.find_all('style')

            results = {
                'url': url,
                'external_css_count': len(css_files),
                'inline_styles_count': len(style_tags),
                'css_files': list(css_files)
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})

    def page_analyzer(self, url):
        """Comprehensive page analysis"""
        try:
            response = self.session.get(url, timeout=10)
            soup = BeautifulSoup(response.content, 'html.parser')

            # Count various elements
            results = {
                'url': url,
                'title': soup.title.string if soup.title else '',
                'status_code': response.status_code,
                'size': len(response.content),
                'links': len(soup.find_all('a')),
                'images': len(soup.find_all('img')),
                'scripts': len(soup.find_all('script')),
                'stylesheets': len(soup.find_all('link', rel='stylesheet')),
                'forms': len(soup.find_all('form')),
                'iframes': len(soup.find_all('iframe')),
                'meta_tags': len(soup.find_all('meta')),
                'headings': {
                    'h1': len(soup.find_all('h1')),
                    'h2': len(soup.find_all('h2')),
                    'h3': len(soup.find_all('h3')),
                    'h4': len(soup.find_all('h4')),
                    'h5': len(soup.find_all('h5')),
                    'h6': len(soup.find_all('h6'))
                },
                'has_favicon': bool(soup.find('link', rel='icon')),
                'language': soup.html.get('lang', 'Not specified') if soup.html else 'Unknown'
            }

            return json.dumps(results, ensure_ascii=False)
        except Exception as e:
            return json.dumps({'error': str(e)})


# Wrapper functions
def crawl(url, max_pages=20):
    tool = WebTools()
    return tool.web_crawler(url, int(max_pages))


def extract_emails(url):
    tool = WebTools()
    return tool.extract_emails(url)


def extract_phones(url):
    tool = WebTools()
    return tool.extract_phone_numbers(url)


def extract_social(url):
    tool = WebTools()
    return tool.extract_social_links(url)


def extract_forms(url):
    tool = WebTools()
    return tool.extract_forms(url)


def extract_js(url):
    tool = WebTools()
    return tool.extract_js_files(url)


def extract_css(url):
    tool = WebTools()
    return tool.extract_css_files(url)


def analyze_page(url):
    tool = WebTools()
    return tool.page_analyzer(url)
