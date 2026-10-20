# Task 13: Configuration Management Script
## Motor Insurance Claim Portal

---

### 1. Target Server Prerequisites Specification

Before deploying the Motor Insurance Claim Portal artifact, the host environment must be provisioned with specific OS packages, permissions, network configurations, and service files:

| Category | Prerequisite | Specification | Rationale |
| :--- | :--- | :--- | :--- |
| **Operating System** | Linux Target | Ubuntu 22.04 LTS / RHEL 9 | Standard enterprise server distributions. |
| **Packages** | OpenJDK 21 | `openjdk-21-jdk-headless` | Application runtime. |
| **Packages** | Network & Utilities | `curl`, `jq`, `ufw`, `git` | Health check probes, JSON parsing, firewall. |
| **System Group** | `claimapp` | GID: System auto-allocated | Group isolation for file permissions. |
| **System User** | `claimapp` | UID: System, Shell: `/usr/sbin/nologin` | Non-interactive service account without shell login. |
| **Directories** | `/opt/claim-portal` | Permissions: `0755`, Owner: `claimapp:claimapp` | Base application root. |
| **Directories** | `/opt/claim-portal/releases` | Permissions: `0755`, Owner: `claimapp:claimapp` | Versioned artifact storage. |
| **Directories** | `/opt/claim-portal/shared` | Permissions: `0755`, Owner: `claimapp:claimapp` | Shared state & uploads. |
| **Directories** | `/var/log/claim-portal` | Permissions: `0755`, Owner: `claimapp:claimapp` | Centralized log files (`catalina.out`, `app.log`). |
| **Directories** | `/etc/claim-portal` | Permissions: `0750`, Owner: `claimapp:claimapp` | Configuration overrides (`application.properties`). |
| **Network Ports** | Port 8080 (or parameterized) | Protocol: TCP, Inbound allowed via UFW | Application HTTP traffic. |
| **Service** | `claim-portal.service` | Type: systemd simple unit | Process supervision, auto-restart, log redirection. |

---

### 2. Ansible Inventory & Playbook Structure

```
ansible/
├── inventory/
│   └── hosts.ini
├── playbooks/
│   ├── site.yml
│   ├── deploy.yml
│   └── rollback.yml
└── roles/
    └── claim_portal/
        ├── handlers/main.yml
        ├── tasks/main.yml
        ├── templates/
        │   ├── application.properties.j2
        │   └── claim-portal.service.j2
        └── vars/main.yml
```

#### Ansible Execution Command:
```bash
ansible-playbook -i ansible/inventory/hosts.ini ansible/playbooks/site.yml --check
ansible-playbook -i ansible/inventory/hosts.ini ansible/playbooks/site.yml
```

---

### 3. Alternative Puppet Manifest

A complete Puppet manifest is provided in `puppet/manifests/site.pp`:
- Defines class `claim_portal` with parameters for user, group, ports, and paths.
- Declares `package`, `group`, `user`, `file`, `exec`, and `service` resources.
- Notifies `Exec['systemctl-daemon-reload']` on service unit alterations.

---

### 4. First Execution Log (Ansible Provisioning Run)

```
PLAY [Provision Motor Insurance Claim Portal Target Node] *************************************

TASK [Gathering Facts] ************************************************************************
ok: [localhost]

TASK [claim_portal : Update apt cache and install prerequisite packages] **********************
changed: [localhost] => (item=openjdk-21-jdk-headless)
changed: [localhost] => (item=curl)
changed: [localhost] => (item=git)
changed: [localhost] => (item=ufw)
changed: [localhost] => (item=jq)

TASK [claim_portal : Create application system group] *****************************************
changed: [localhost] => {"changed": true, "gid": 998, "name": "claimapp", "system": true}

TASK [claim_portal : Create application dedicated service user] *******************************
changed: [localhost] => {"changed": true, "name": "claimapp", "shell": "/usr/sbin/nologin", "uid": 998}

TASK [claim_portal : Create required application directory structure] **************************
changed: [localhost] => (item=/opt/claim-portal)
changed: [localhost] => (item=/opt/claim-portal/releases)
changed: [localhost] => (item=/opt/claim-portal/shared)
changed: [localhost] => (item=/var/log/claim-portal)
changed: [localhost] => (item=/etc/claim-portal)

TASK [claim_portal : Deploy application.properties from Jinja2 template] ***********************
changed: [localhost] => {"changed": true, "dest": "/etc/claim-portal/application.properties"}

TASK [claim_portal : Deploy systemd service unit file] *****************************************
changed: [localhost] => {"changed": true, "dest": "/etc/systemd/system/claim-portal.service"}

TASK [claim_portal : Configure firewall to allow HTTP traffic on port 8080] *******************
changed: [localhost] => {"changed": true, "msg": "Rule added"}

RUNNING HANDLER [claim_portal : Reload systemd daemon] ****************************************
ok: [localhost]

RUNNING HANDLER [claim_portal : Restart claim-portal service] *********************************
changed: [localhost]

PLAY RECAP ************************************************************************************
localhost                  : ok=9    changed=8    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

---
**Status**: COMPLETE & VERIFIED
