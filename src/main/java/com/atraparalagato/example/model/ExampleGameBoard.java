package com.atraparalagato.example.model;

import com.atraparalagato.base.model.GameBoard;
import com.atraparalagato.impl.model.HexPosition;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementación de ejemplo de GameBoard para tableros hexagonales.
 * 
 * NOTA PARA ESTUDIANTES:
 * Esta es una implementación BÁSICA de ejemplo. Los estudiantes deben:
 * 1. Estudiar esta implementación para entender los conceptos
 * 2. Crear su propia implementación más sofisticada
 * 3. Optimizar las estructuras de datos según sus necesidades
 * 
 * Esta implementación es intencionalmente simple para fines educativos.
 */
public class ExampleGameBoard extends GameBoard<HexPosition> {
    
    public ExampleGameBoard(int size) {
        super(size);
    }
    
    @Override
    protected Set<HexPosition> initializeBlockedPositions() {
        // Usar HashSet para almacenamiento eficiente
        return new HashSet<>();
    }
    
    @Override
    public boolean isPositionInBounds(HexPosition position) {
        // Verificar límites hexagonales usando coordenadas axiales
        // Las posiciones válidas incluyen el borde para que el gato pueda escapar
        return Math.abs(position.getQ()) <= size && 
               Math.abs(position.getR()) <= size && 
               Math.abs(position.getS()) <= size;
    }
    
    @Override
    public boolean isValidMove(HexPosition position) {
        // Un movimiento del JUGADOR es válido si está DENTRO del tablero (no en el borde) y no está bloqueado
        // El gato puede moverse al borde, pero el jugador no puede bloquear el borde
        return isPositionInBounds(position) && 
               !isAtBorder(position) && 
               !isBlocked(position);
    }
    
    @Override
    public void executeMove(HexPosition position) {
        // Agregar la posición a las posiciones bloqueadas
        blockedPositions.add(position);
    }
    
    @Override
    public List<HexPosition> getPositionsWhere(Predicate<HexPosition> condition) {
        // Generar todas las posiciones posibles y filtrar
        return getAllPossiblePositions().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HexPosition> getAdjacentPositions(HexPosition position) {
        // Direcciones hexagonales: las 6 direcciones posibles
        HexPosition[] directions = {
            new HexPosition(1, 0),   // Este
            new HexPosition(1, -1),  // Noreste
            new HexPosition(0, -1),  // Noroeste
            new HexPosition(-1, 0),  // Oeste
            new HexPosition(-1, 1),  // Suroeste
            new HexPosition(0, 1)    // Sureste
        };
        
        return Arrays.stream(directions)
                .map(dir -> (HexPosition) position.add(dir))
                .filter(this::isPositionInBounds) // Incluye posiciones del borde
                .filter(pos -> !isBlocked(pos))   // Excluye posiciones bloqueadas
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isBlocked(HexPosition position) {
        // Verificar si la posición está en el conjunto de bloqueadas
        return blockedPositions.contains(position);
    }
    
    /**
     * Verifica si una posición está en el borde del tablero.
     * El borde son las posiciones donde el gato puede escapar.
     */
    public boolean isAtBorder(HexPosition position) {
        return Math.abs(position.getQ()) == size ||
               Math.abs(position.getR()) == size ||
               Math.abs(position.getS()) == size;
    }
    
    // Método auxiliar para generar todas las posiciones posibles
    private List<HexPosition> getAllPossiblePositions() {
        List<HexPosition> positions = new ArrayList<>();
        
        // Generar todas las posiciones dentro del tablero (excluyendo el borde para jugabilidad)
        for (int q = -size + 1; q < size; q++) {
            for (int r = -size + 1; r < size; r++) {
                HexPosition pos = new HexPosition(q, r);
                // Solo incluir posiciones que no están en el borde (donde el jugador puede jugar)
                if (isPositionInBounds(pos) && !isAtBorder(pos)) {
                    positions.add(pos);
                }
            }
        }
        
        return positions;
    }
    
    // Método adicional útil para debugging
    public void printBoard() {
        System.out.println("Estado del tablero (tamaño: " + size + "):");
        System.out.println("Posiciones bloqueadas: " + blockedPositions.size());
        
        // Imprimir algunas estadísticas básicas
        long totalPositions = getAllPossiblePositions().size();
        System.out.println("Total de posiciones: " + totalPositions);
        System.out.println("Posiciones libres: " + (totalPositions - blockedPositions.size()));
    }
    
    // Método para obtener estadísticas del tablero
    public Map<String, Object> getBoardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<HexPosition> allPositions = getAllPossiblePositions();
        
        stats.put("boardSize", size);
        stats.put("totalPositions", allPositions.size());
        stats.put("blockedPositions", blockedPositions.size());
        stats.put("freePositions", allPositions.size() - blockedPositions.size());
        stats.put("blockagePercentage", 
                  (double) blockedPositions.size() / allPositions.size() * 100);
        
        return stats;
    }
} 