package com.b4utrade.mysql;

import java.sql.Connection;

public interface IDBConnectionManager {

	Connection getConnection();
}
