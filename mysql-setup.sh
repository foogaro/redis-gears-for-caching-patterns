#!/bin/bash
set -e

mysql -h"mysql" -P3306 -uroot -p"$MYSQL_ROOT_PASSWORD" < /tmp/mysql-setup.sql
