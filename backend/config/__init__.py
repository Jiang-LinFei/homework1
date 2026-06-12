import pymysql

# Django 3.2 requires mysqlclient>=1.4.0; PyMySQL emulates it but reports a
# lower version, so advertise a compatible version before installing it.
pymysql.version_info = (1, 4, 6, 'final', 0)
pymysql.install_as_MySQLdb()
