package com.atraparalagato.impl.strategy;

import com.atraparalagato.base.model.GameBoard;
import com.atraparalagato.base.strategy.CatMovementStrategy;
import com.atraparalagato.impl.model.HexPosition;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementación esqueleto de estrategia BFS (Breadth-First Search) para el gato.
 * 
 * Los estudiantes deben completar los métodos marcados con TODO.
 * 
 * Conceptos a implementar:
 * - Algoritmo BFS para pathfinding
 * - Exploración exhaustiva de caminos
 * - Garantía de encontrar el camino más corto
 * - Uso de colas para exploración por niveles
 */
public class BFSCatMovement extends CatMovementStrategy<HexPosition> {
    
    public BFSCatMovement(GameBoard<HexPosition> board) {
        super(board);
    }
    
    @Override
    protected List<HexPosition> getPossibleMoves(HexPosition currentPosition) {
        return board.getAdjacentPositions(currentPosition).stream()
                .filter(pos -> !board.isBlocked(pos))
                .toList();
        // Filtrar posiciones bloqueadas y fuera de límites
        //throw new UnsupportedOperationException("Los estudiantes deben implementar getPossibleMoves");
    }
    
    @Override
    public Optional<HexPosition> selectBestMove(List<HexPosition> possibleMoves, 
                                                  HexPosition currentPosition, 
                                                  HexPosition targetPosition) {
        // 1. Ejecutar BFS desde cada posible movimiento
        // 2. Evaluar cuál lleva más rápido al objetivo
        // 3. Retornar el primer paso del mejor camino
        possibleMoves = getPossibleMoves(currentPosition);
        int minSteps = Integer.MAX_VALUE;

        HexPosition bestMove = currentPosition;

        for (HexPosition move : possibleMoves) {
            Optional<List<HexPosition>> steps = bfsToGoal(move);

            if (steps.isPresent() && steps.get().size() < minSteps) {
                minSteps = steps.get().size();
                bestMove = move;
            }
        }
        return Optional.of(bestMove);
    }
    
    @Override
    protected Function<HexPosition, Double> getHeuristicFunction(HexPosition targetPosition) {
        // Retornar función que calcule distancia euclidiana o Manhattan
        return pos -> Math.sqrt(
        Math.pow(pos.getR() - targetPosition.getR(), 2) +
        Math.pow(pos.getQ() - targetPosition.getQ(), 2)
        );
    }
    
    @Override
    protected Predicate<HexPosition> getGoalPredicate() {
        int size = board.getSize(); // O usa getRows()/getCols() según tu implementación
        return pos -> {
            int row = pos.getR();
            int col = pos.getQ();
            return row == 0 || col == 0 || row == size - 1 || col == size - 1;
        };
    }
    
    @Override
    protected double getMoveCost(HexPosition from, HexPosition to) {
        // BFS usa costo uniforme (1.0 para movimientos válidos)
        return 1.0;
    }
    
    @Override
    public boolean hasPathToGoal(HexPosition currentPosition) {
        Set<HexPosition> visited = new HashSet<>();
        Queue<HexPosition> queue = new LinkedList<>();
        int size = board.getSize(); // O usa board.getRows()/getCols() según tu implementación

        queue.add(currentPosition);
        visited.add(currentPosition);

        while (!queue.isEmpty()) {
            HexPosition pos = queue.poll();

            // Verifica si está en el borde del tablero (objetivo)
            int row = pos.getR();
            int col = pos.getQ();
            if (row == 0 || col == 0 || row == size - 1 || col == size - 1) {
                // Si se encontró el objetivo, retornar true
                // Esto significa que hay un camino al borde
                return true;
            }

            for (HexPosition neighbor : getPossibleMoves(pos)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        // Si se agotaron las posiciones y no se encontró el objetivo, retornar false
        // Esto significa que no hay camino al borde
        return false;
        
    }
    
    @Override
    public List<HexPosition> getFullPath(HexPosition currentPosition, HexPosition targetPosition) {
        Set<HexPosition> visited = new HashSet<>();
        Queue<List<HexPosition>> queue = new LinkedList<>();

        List<HexPosition> initialPath = new ArrayList<>();
        initialPath.add(currentPosition);
        queue.add(initialPath);
        visited.add(currentPosition);

        while (!queue.isEmpty()) {
            List<HexPosition> path = queue.poll();
            HexPosition last = path.get(path.size() - 1);

            if (last.equals(targetPosition)) {
                return path;
            }

            for (HexPosition neighbor : getPossibleMoves(last)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<HexPosition> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }
        // Si no hay camino, retorna lista vacía
        return new ArrayList<>();
    }
    
    // Métodos auxiliares que los estudiantes pueden implementar
    
    private Optional<List<HexPosition>> bfsToGoal(HexPosition start) {
        Set<HexPosition> visited = new HashSet<>();
        Queue<List<HexPosition>> queue = new LinkedList<>();

        // La ruta inicial solo contiene el punto de inicio
        List<HexPosition> initialPath = new ArrayList<>();
        initialPath.add(start);
        queue.add(initialPath);
        visited.add(start);

        int size = board.getSize(); // Tamaño del tablero, para verificar bordes
        // Realizar BFS

        while (!queue.isEmpty()) {
            List<HexPosition> path = queue.poll();
            HexPosition current = path.get(path.size() - 1);

            // Comprobar si está en el borde del tablero
            int row = current.getQ();
            int col = current.getR();
            if (row == 0 || col == 0 || row == size - 1 || col == size - 1) {
                return Optional.of(path);
            }

            for (HexPosition neighbor : getPossibleMoves(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<HexPosition> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }
        return Optional.empty(); // No se encontró camino al objetivo

    }
    
    private List<HexPosition> reconstructPath(Map<HexPosition, HexPosition> parentMap, 
                                            HexPosition start, HexPosition goal) {
        List<HexPosition> path = new LinkedList<>();
        HexPosition current = goal;
        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = parentMap.get(current);
        }
        if (current != null) {
            path.add(0, start);
        }
        return path;
    }
    
    private double evaluatePathQuality(List<HexPosition> path) {
        if (path == null || path.isEmpty()) {
        return Double.POSITIVE_INFINITY; // Camino inválido o inexistente
        }
        // Ejemplo simple: menor longitud = mejor calidad
        return path.size();
    }
} 