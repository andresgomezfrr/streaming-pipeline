#!/usr/bin/env bash
envsubst < /opt/rb_monitor/conf/config_env.json > /opt/rb_monitor/conf/config.json
rb_monitor -c /opt/rb_monitor/conf/config.json
