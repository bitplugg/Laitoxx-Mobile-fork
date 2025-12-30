# -*- coding: utf-8 -*-
"""
WhatsApp Checker - Alternatives to Selenium
Different methods to check if a phone number is registered on WhatsApp
"""

import requests
import json
from typing import Dict, Optional


class WhatsAppChecker:
    """
    Multiple methods to check WhatsApp account existence
    without using heavy Selenium automation
    """

    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        })

    def check_wa_me_redirect(self, phone_number: str) -> Dict[str, any]:
        """
        Method 1: Check using wa.me redirect behavior
        Simple and fast, but less accurate
        """
        try:
            num = phone_number.lstrip('+')
            url = f"https://wa.me/{num}"

            response = self.session.head(url, timeout=10, allow_redirects=False)

            result = {
                'method': 'wa.me_redirect',
                'phone': phone_number,
                'status_code': response.status_code,
                'exists': None,
                'confidence': 'low'
            }

            if response.status_code in [301, 302]:
                result['exists'] = True
                result['confidence'] = 'medium'
            elif response.status_code == 200:
                result['exists'] = None
                result['note'] = 'Check manually via link'
            else:
                result['exists'] = False

            return result
        except Exception as e:
            return {'method': 'wa.me_redirect', 'error': str(e)}

    def check_whatsapp_api_send(self, phone_number: str) -> Dict[str, any]:
        """
        Method 2: Check using WhatsApp send API
        More reliable than wa.me
        """
        try:
            num = phone_number.lstrip('+')
            url = f"https://api.whatsapp.com/send?phone={num}"

            response = self.session.get(url, timeout=10)

            result = {
                'method': 'api_send',
                'phone': phone_number,
                'status_code': response.status_code,
                'exists': None,
                'confidence': 'medium'
            }

            # Check response content for hints
            if 'chat' in response.url.lower() or response.status_code == 200:
                result['exists'] = True
            else:
                result['exists'] = False

            return result
        except Exception as e:
            return {'method': 'api_send', 'error': str(e)}

    def generate_whatsapp_links(self, phone_number: str) -> Dict[str, str]:
        """
        Method 3: Generate links for manual verification
        Most reliable - let user verify directly
        """
        num = phone_number.lstrip('+')

        return {
            'wa_me': f'https://wa.me/{num}',
            'api_send': f'https://api.whatsapp.com/send?phone={num}',
            'whatsapp_protocol': f'whatsapp://send?phone={num}',
            'web_whatsapp': f'https://web.whatsapp.com/send?phone={num}',
            'info': 'Open any link to check if account exists and start chat'
        }

    def check_all_methods(self, phone_number: str) -> Dict[str, any]:
        """
        Run all available methods and aggregate results
        """
        results = {
            'phone_number': phone_number,
            'checks': []
        }

        # Method 1: wa.me redirect
        wa_me_result = self.check_wa_me_redirect(phone_number)
        results['checks'].append(wa_me_result)

        # Method 2: API send
        api_result = self.check_whatsapp_api_send(phone_number)
        results['checks'].append(api_result)

        # Aggregate conclusion
        positive_checks = sum(1 for check in results['checks']
                            if check.get('exists') == True)

        if positive_checks >= 2:
            results['conclusion'] = 'Account likely exists'
            results['confidence'] = 'high'
        elif positive_checks == 1:
            results['conclusion'] = 'Account may exist'
            results['confidence'] = 'medium'
        else:
            results['conclusion'] = 'Account likely does not exist'
            results['confidence'] = 'low'

        # Add manual verification links
        results['verification_links'] = self.generate_whatsapp_links(phone_number)
        results['recommendation'] = 'For accurate results, manually open verification links'

        return results


def check_whatsapp(phone_number: str) -> str:
    """
    Wrapper function for easy integration
    Returns JSON string
    """
    checker = WhatsAppChecker()
    result = checker.check_all_methods(phone_number)
    return json.dumps(result, ensure_ascii=False)


# For standalone testing
if __name__ == "__main__":
    import sys

    if len(sys.argv) < 2:
        print("Usage: python whatsapp_checker.py +1234567890")
        sys.exit(1)

    phone = sys.argv[1]
    checker = WhatsAppChecker()
    result = checker.check_all_methods(phone)

    print(json.dumps(result, indent=2, ensure_ascii=False))
