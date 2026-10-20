# ==============================================================================
# Puppet Manifest: Motor Insurance Claim Portal Node Provisioning
# ==============================================================================

node default {
  class { 'claim_portal':
    app_port    => 8080,
    app_user    => 'claimapp',
    app_group   => 'claimapp',
    install_dir => '/opt/claim-portal',
  }
}

class claim_portal (
  Integer $app_port    = 8080,
  String  $app_user    = 'claimapp',
  String  $app_group   = 'claimapp',
  String  $install_dir = '/opt/claim-portal',
) {

  # 1. Ensure OpenJDK 21 is installed
  package { 'openjdk-21-jdk-headless':
    ensure => installed,
  }

  package { ['curl', 'jq', 'ufw']:
    ensure => installed,
  }

  # 2. Manage Service Group and User
  group { $app_group:
    ensure => present,
    system => true,
  }

  user { $app_user:
    ensure     => present,
    gid        => $app_group,
    home       => $install_dir,
    shell      => '/usr/sbin/nologin',
    system     => true,
    managehome => false,
    require    => Group[$app_group],
  }

  # 3. Directory Structure
  $dirs = [
    $install_dir,
    "${install_dir}/releases",
    "${install_dir}/shared",
    '/var/log/claim-portal',
    '/etc/claim-portal',
  ]

  file { $dirs:
    ensure  => directory,
    owner   => $app_user,
    group   => $app_group,
    mode    => '0755',
    require => User[$app_user],
  }

  # 4. Configuration File
  file { '/etc/claim-portal/application.properties':
    ensure  => file,
    owner   => $app_user,
    group   => $app_group,
    mode    => '0640',
    content => @("EOF"),
      server.port=${app_port}
      spring.application.name=motor-insurance-claim-portal
      management.endpoints.web.exposure.include=health,info,metrics
      app.features.reviewer-workbench.enabled=true
      | EOF
    require => File['/etc/claim-portal'],
    notify  => Service['claim-portal'],
  }

  # 5. Systemd Service Unit
  file { '/etc/systemd/system/claim-portal.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => @("EOF"),
      [Unit]
      Description=Motor Insurance Claim Portal Service
      After=network.target

      [Service]
      Type=simple
      User=${app_user}
      Group=${app_group}
      ExecStart=/usr/bin/java -jar ${install_dir}/current/claim-portal.jar --server.port=${app_port}
      Restart=always
      RestartSec=10

      [Install]
      WantedBy=multi-user.target
      | EOF
    notify  => [Exec['systemctl-daemon-reload'], Service['claim-portal']],
  }

  exec { 'systemctl-daemon-reload':
    command     => '/bin/systemctl daemon-reload',
    refreshonly => true,
  }

  # 6. Service Management
  service { 'claim-portal':
    ensure  => running,
    enable  => true,
    require => [
      File['/etc/systemd/system/claim-portal.service'],
      Package['openjdk-21-jdk-headless'],
    ],
  }
}
