package configurations.orm;

import java.util.*;
public class DependencyResolver {

    // Resolve a ordem de criação das tabelas com base nas dependências
    public static List<String> resolveOrder(Map<String, TableDependency> dependencyGraph) {
        List<String> ordered = new ArrayList<>(); // Ordem final
        Set<String> visited = new HashSet<>();    // Tabelas já processadas
        Set<String> visiting = new HashSet<>();   // Tabelas atualmente em processamento (para detectar ciclos)

        for (String table : dependencyGraph.keySet()) {
            if (!visited.contains(table)) {
                // Executa DFS para garantir que todas as dependências dessa tabela sejam processadas antes
                if (!dfs(table, dependencyGraph, visited, visiting, ordered)) {
                    throw new RuntimeException("Ciclo detectado nas dependências entre tabelas!");
                }
            }
        }

        return ordered;
    }

    // Função auxiliar para DFS com detecção de ciclos
    private static boolean dfs(String current,
                               Map<String, TableDependency> graph,
                               Set<String> visited,
                               Set<String> visiting,
                               List<String> ordered) {

        // Se já estamos processando essa tabela, há um ciclo
        if (visiting.contains(current)) {
            return false;
        }

        // Se já foi processada, não precisa repetir
        if (visited.contains(current)) {
            return true;
        }

        visiting.add(current); // Marca como em processamento

        // Para cada tabela da qual essa tabela depende...
        for (String dependency : graph.get(current).getDependencies()) {
            // Executa DFS recursivamente
            if (!dfs(dependency, graph, visited, visiting, ordered)) {
                return false;
            }
        }

        visiting.remove(current); // Termina o processamento
        visited.add(current);    // Marca como processada
        ordered.add(current);    // Adiciona na ordem final

        return true;
    }
}
