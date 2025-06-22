package com.atraparalagato.impl.strategy;

import com.atraparalagato.base.model.GameBoard;
import com.atraparalagato.base.strategy.CatMovementStrategy;
import com.atraparalagato.impl.model.HexGameBoard;
import com.atraparalagato.impl.model.HexPosition;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementación esqueleto de estrategia de movimiento usando algoritmo A*.
 * 
 * Los estudiantes deben completar los métodos marcados con todo.
 * 
 * Conceptos a implementar:
 * - Algoritmos: A* pathfinding
 * - Programación Funcional: Function, Predicate
 * - Estructuras de Datos: PriorityQueue, Map, Set
 */
public class AStarCatMovement extends CatMovementStrategy<HexPosition> {
    
    public AStarCatMovement(GameBoard<HexPosition> board) {
        super(board);
    }
    
    @Override
    protected List<HexPosition> getPossibleMoves(HexPosition currentPosition) {
        // Obtener posiciones adyacentes válidas
        // Filtrar posiciones bloqueadas
        return board.getAdjacentPositions(currentPosition).stream()
                .filter(pos -> !board.isBlocked(pos))
                .toList();
    }
    
    @Override
    public Optional<HexPosition> selectBestMove(List<HexPosition> possibleMoves, 
                                                  HexPosition currentPosition, 
                                                  HexPosition targetPosition) {
        // Implementar selección del mejor movimiento usando A*
        // Calcular f(n) = g(n) + h(n) para cada movimiento posible
        // g(n) = costo desde inicio hasta n
        // h(n) = heurística desde n hasta objetivo
        // Retornar el movimiento con menor f(n)
        // 
        // Pista: Usar Function para calcular costos y comparar
        // Ejemplo:
        // return possibleMoves.stream()
        // Usa los métodos existentes para obtener las funciones de costo y heurística
        Function<HexPosition, Double> gCost = pos -> getMoveCost(currentPosition, pos);
        Function<HexPosition, Double> hCost = getHeuristicFunction(targetPosition);

        return possibleMoves.stream()
                .min((a, b) -> {
                    double fa = gCost.apply(a) + hCost.apply(a);
                    double fb = gCost.apply(b) + hCost.apply(b);
                    return Double.compare(fa, fb);
                });
    }
    
    @Override
    protected Function<HexPosition, Double> getHeuristicFunction(HexPosition targetPosition) {
        // Implementar función heurística
        // Para tablero hexagonal, usar distancia hexagonal
        // La heurística debe ser admisible (nunca sobreestimar el costo real)
        // 
        // Ejemplo:
        // return position -> position.distanceTo(targetPosition);
        // Distancia hexagonal admisible usando coordenadas axiales
        return position -> {
            int dq = Math.abs(position.getQ() - targetPosition.getQ());
            int dr = Math.abs(position.getR() - targetPosition.getR());
            int ds = Math.abs(position.getS() - targetPosition.getS());
            return (double) Math.max(dq, Math.max(dr, ds));
        };
    }
    
    @Override
    protected Predicate<HexPosition> getGoalPredicate() {
        // Definir qué posiciones son objetivos válidos
        // Para "atrapar al gato", el objetivo son las posiciones del borde
        // 
        // Pista: Una posición está en el borde si está en el límite del tablero
        // return position -> Math.abs(position.getQ()) == board.getSize() ||
        //                   Math.abs(position.getR()) == board.getSize() ||
        //                   Math.abs(position.getS()) == board.getSize();
        HexGameBoard hexBoard = (HexGameBoard) board;
        int size = hexBoard.getSize() - 1; // Suponiendo que el tablero va de 0 a size-1

        return position ->
            Math.abs(position.getQ()) == size ||
            Math.abs(position.getR()) == size ||
            Math.abs(position.getS()) == size;
    }
    
    @Override
    protected double getMoveCost(HexPosition from, HexPosition to) {
        // Calcula la distancia hexagonal usando coordenadas axiales (q, r, s)
        int dq = Math.abs(from.getQ() - to.getQ());
        int dr = Math.abs(from.getR() - to.getR());
        int ds = Math.abs(from.getS() - to.getS());

        int hexDistance = Math.max(dq, Math.max(dr, ds));
        return (hexDistance == 1) ? 1.0 : 0.0; 
    }
    
    @Override
    public boolean hasPathToGoal(HexPosition currentPosition) {
        // Verificar si existe camino desde posición actual hasta cualquier objetivo
        // A* para explorar hasta encontrar una posición objetivo
        // Retornar true si se encuentra camino, false si no
        // 
        // Pista: Usar getGoalPredicate() para identificar objetivos
        Predicate<HexPosition> goalPredicate = getGoalPredicate();
        Set<AStarNode> closedSet = new HashSet<>();
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> node.fScore));
        Map<HexPosition, AStarNode> allNodes = new HashMap<>();

        AStarNode startNode = new AStarNode(
            currentPosition,
            0.0,
            getHeuristicFunction(currentPosition).apply(currentPosition),
            null
        );
        openSet.add(startNode);
        allNodes.put(currentPosition, startNode);

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            if (goalPredicate.test(current.position)) {
                return true;
            }

            closedSet.add(current);

            for (HexPosition neighborPos : getPossibleMoves(current.position)) {
                double tentativeG = current.gScore + getMoveCost(current.position, neighborPos);

                AStarNode neighborNode = allNodes.get(neighborPos);
                if (neighborNode == null) {
                    neighborNode = new AStarNode(
                        neighborPos,
                        tentativeG,
                        tentativeG + getHeuristicFunction(currentPosition).apply(neighborPos),
                        current
                    );
                    allNodes.put(neighborPos, neighborNode);
                }

                if (closedSet.contains(neighborNode) && tentativeG >= neighborNode.gScore) continue;

                if (tentativeG < neighborNode.gScore) {
                    neighborNode = new AStarNode(
                        neighborPos,
                        tentativeG,
                        tentativeG + getHeuristicFunction(currentPosition).apply(neighborPos),
                        current
                    );
                    allNodes.put(neighborPos, neighborNode);
                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return false;

    }
    
    @Override
    public List<HexPosition> getFullPath(HexPosition currentPosition, HexPosition targetPosition) {
        // Implementar A* completo para obtener el camino completo
        // Usar PriorityQueue para nodos a explorar
        // Mantener Map de padres para reconstruir el camino
        // Retornar lista de posiciones desde inicio hasta objetivo
        // 
        // Estructura sugerida:
        // 1. Inicializar estructuras de datos (openSet, closedSet, gScore, fScore, cameFrom)
        // 2. Agregar posición inicial a openSet
        // 3. Mientras openSet no esté vacío:
        //    a. Tomar nodo con menor fScore
        //    b. Si es objetivo, reconstruir y retornar camino
        //    c. Mover a closedSet
        //    d. Para cada vecino válido, calcular scores y actualizar
        // 4. Si no se encuentra camino, retornar lista vacíaSet<AStarNode> closedSet = new HashSet<>();
        // Utiliza un Set de posiciones para closedSet
        Set<HexPosition> closedSet = new HashSet<>();
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> node.fScore));
        Map<HexPosition, AStarNode> allNodes = new HashMap<>();

        // Nodo inicial
        AStarNode startNode = new AStarNode(
            currentPosition,
            0.0,
            getHeuristicFunction(targetPosition).apply(currentPosition),
            null
        );
        openSet.add(startNode);
        allNodes.put(currentPosition, startNode);

        while (!openSet.isEmpty()) {
            AStarNode current = openSet.poll();

            if (current.position.equals(targetPosition)) {
                return reconstructPath(current);
            }

            closedSet.add(current.position);

            for (HexPosition neighborPos : getPossibleMoves(current.position)) {
                double tentativeG = current.gScore + getMoveCost(current.position, neighborPos);

                AStarNode neighborNode = allNodes.get(neighborPos);
                if (neighborNode == null) {
                    neighborNode = new AStarNode(
                        neighborPos,
                        tentativeG,
                        tentativeG + getHeuristicFunction(targetPosition).apply(neighborPos),
                        current
                    );
                    allNodes.put(neighborPos, neighborNode);
                }

                if (closedSet.contains(neighborPos) && tentativeG >= neighborNode.gScore) continue;

                if (tentativeG < neighborNode.gScore) {
                    neighborNode = new AStarNode(
                        neighborPos,
                        tentativeG,
                        tentativeG + getHeuristicFunction(targetPosition).apply(neighborPos),
                        current
                    );
                    allNodes.put(neighborPos, neighborNode);
                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        // Si no se encuentra camino, retornar lista vacía
        return new ArrayList<>();
    }


    
    // Clase auxiliar para nodos del algoritmo A*
    private static class AStarNode {
        public final HexPosition position;
        public final double gScore; // Costo desde inicio
        public final double fScore; // gScore + heurística
        public final AStarNode parent;
        
        public AStarNode(HexPosition position, double gScore, double fScore, AStarNode parent) {
            this.position = position;
            this.gScore = gScore;
            this.fScore = fScore;
            this.parent = parent;
        }
    }
    
    // Método auxiliar para reconstruir el camino
    private List<HexPosition> reconstructPath(AStarNode goalNode) {
        List<HexPosition> path = new LinkedList<>();
        AStarNode current = goalNode;
        while (current != null) {
            path.add(0, current.position); // Usa el campo o método definido en AStarCatMovement
            current = current.parent;      // Usa el campo o método definido en AStarCatMovement
        }
    return path;
    }
    
    // Hook methods - los estudiantes pueden override para debugging
    @Override
    protected void beforeMovementCalculation(HexPosition currentPosition) {
        // Opcional - logging, métricas, etc.
        super.beforeMovementCalculation(currentPosition);
    }
    
    @Override
    protected void afterMovementCalculation(Optional<HexPosition> selectedMove) {
        // Opcional - logging, métricas, etc.
        super.afterMovementCalculation(selectedMove);
    }
} 