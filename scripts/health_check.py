#!/usr/bin/env python3
"""
Automated Health Check Script for Motor Insurance Claim Portal.
Used in Jenkins pipelines, Docker health probes, and Ansible provisioning.
"""
import sys
import json
import urllib.request
import urllib.error
import time

def check_health(host="http://localhost:8080/actuator/health", timeout=10, retries=5, delay=3):
    print(f"[*] Probing health endpoint: {host} (Retries: {retries}, Timeout: {timeout}s)")
    for attempt in range(1, retries + 1):
        try:
            req = urllib.request.Request(host, headers={'User-Agent': 'HealthCheckProbe/1.0'})
            with urllib.request.urlopen(req, timeout=timeout) as response:
                status_code = response.getcode()
                body = response.read().decode('utf-8')
                data = json.loads(body)
                health_status = data.get("status")
                print(f"[+] Attempt {attempt}: HTTP {status_code} - Status: {health_status}")
                if status_code == 200 and health_status == "UP":
                    print("[✓] Service is HEALTHY and UP!")
                    return 0
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError, ConnectionRefusedError) as e:
            print(f"[-] Attempt {attempt} failed: {e}")
            if attempt < retries:
                time.sleep(delay)
    print("[✗] Health check FAILED. Target service unreachable or DOWN.")
    return 1

if __name__ == "__main__":
    url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/actuator/health"
    sys.exit(check_health(url))
