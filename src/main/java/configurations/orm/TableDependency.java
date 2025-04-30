package configurations.orm;

import java.util.ArrayList;
import java.util.List;

public class TableDependency {
    private final String tableName; // Nome da tabela
    private final List<String> dependencies; // Lista de tabelas das quais ela depende

    public TableDependency(String tableName) {
        this.tableName = tableName;
        this.dependencies = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    // Adiciona uma tabela da qual essa depende (FK)
    public void addDependency(String dependentTable) {
        dependencies.add(dependentTable);
    }

    // Retorna as dependências (FKs)
    public List<String> getDependencies() {
        return dependencies;
    }
}

