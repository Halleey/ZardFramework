package configurations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionPool {

    private static final String URL = "jdbc:mysql://localhost:3306/frame?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER ="root";
    private static final String PASSWORD = "zard";

    // Quantidade inicial de conexões no pool
    private static final int INITIAL_POOL_SIZE = 10;

    //  Lista de conexões livres para uso
    private static final List<Connection> connectionPool = new ArrayList<>();

    // Lista de conexões que já foram entregues para uso
    private static final List<Connection> usedConnections = new ArrayList<>();


    // Este bloco roda assim que a classe é carregada pela primeira vez
    static {
        try {
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                connectionPool.add(createConnection()); // Cria e adiciona conexões ao pool
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Em caso de erro ao criar conexão, imprime o erro
        }
    }

    // Método auxiliar para criar uma nova conexão com o banco
    private static Connection createConnection() throws SQLException {
        System.out.println("Criando nova conexão com o banco de dados...");
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Conexão criada com sucesso!");
        return connection;
    }


    // Método para pegar uma conexão do pool
    public static synchronized Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            // Se não houver conexões livres, cria uma nova conexão e adiciona ao pool
            connectionPool.add(createConnection());
        }
        // Pega a última conexão livre da lista
        Connection conn = connectionPool.remove(connectionPool.size() - 1);
        // Adiciona ela à lista de conexões em uso
        usedConnections.add(conn);

        // Verifica se a conexão foi estabelecida corretamente
        if (conn != null) {
            System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
        } else {
            System.err.println("Falha ao estabelecer a conexão com o banco de dados.");
        }

        return conn;
    }

    // devolve uma conexão ao pool depois do uso
    public static synchronized void releaseConnection(Connection conn) {
        connectionPool.add(conn);      // Devolve para a lista de conexões livres
        usedConnections.remove(conn);  // Remove da lista de conexões em uso
    }

    // Método para verificar quantas conexões livres existem no pool
    public static int getPoolSize() {
        return connectionPool.size();
    }
}

