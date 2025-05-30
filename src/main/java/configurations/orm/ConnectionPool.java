package configurations.orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    private static final String URL = AppProperties.get("db.url");
    private static final String USER = AppProperties.get("db.user");
    private static final String PASSWORD = AppProperties.get("db.password");

    private static final int INITIAL_POOL_SIZE = AppProperties.getInt("db.pool.size");

    private static final List<Connection> connectionPool = new ArrayList<>();
    private static final List<Connection> usedConnections = new ArrayList<>();

    static {
        try {
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                connectionPool.add(createConnection());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar o pool de conexões", e);
        }
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            connectionPool.add(createConnection());
        }
        Connection conn = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(conn);
        return conn;
    }

    public static synchronized void releaseConnection(Connection conn) {
        connectionPool.add(conn);
        usedConnections.remove(conn);
    }

    public static int getPoolSize() {
        return connectionPool.size();
    }
}
