import pymysql

# 使用纯 Python 的 PyMySQL 作为 MySQLdb，免去 Windows 上编译 mysqlclient 的麻烦。
pymysql.install_as_MySQLdb()
