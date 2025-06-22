package com.atraparalagato.impl.model;

import com.atraparalagato.base.model.GameBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementación esqueleto de GameBoard para tableros hexagonales.
 * 
 * Los estudiantes deben completar los métodos marcados con todo.
 * 
 * Conceptos a implementar:
 * - Modularización: Separación de lógica de tablero hexagonal
 * - OOP: Herencia y polimorfismo
 * - Programación Funcional: Uso de Predicate y streams
 */
public class HexGameBoard extends GameBoard<HexPosition> {
    
    public HexGameBoard(int size) {
        super(size);
    }
    
    @Override
    protected Set<HexPosition> initializeBlockedPositions() {
        // Los estudiantes deben decidir qué estructura de datos usar
        // Opciones: HashSet, TreeSet, LinkedHashSet, etc.
        // Considerar rendimiento vs orden vs duplicados
        return new HashSet<>();
    }
    
    @Override
    protected boolean isPositionInBounds(HexPosition position) {
        // Implementar validación de límites para tablero hexagonal
        // Condición: |q| <= size && |r| <= size && |s| <= size
        return Math.abs(position.getQ()) <= size && 
               Math.abs(position.getR()) <= size && 
               Math.abs(position.getS()) <= size;
    }
    
    @Override
    public boolean isValidMove(HexPosition position) {
        // Combinar validación de límites y estado actual
        // Debe verificar:
        // 1. Que la posición esté dentro de los límites
        // 2. Que la posición no esté ya bloqueada
        // 3. Cualquier regla adicional del juego
        return isPositionInBounds(position) && 
               !isBlocked(position);
               // && !isAtBorder(position);
    }
    
    @Override
    public void executeMove(HexPosition position) {
        // Actualizar el estado interno del tablero
        // Agregar la posición a las posiciones bloqueadas
        blockedPositions.add(position);

    }
    
    @Override
    public List<HexPosition> getPositionsWhere(Predicate<HexPosition> condition) {
        //Implementar usando programación funcional
        // Generar todas las posiciones posibles del tablero
        // Filtrar usando el Predicate
        // Retornar como List
        // 
        // Ejemplo de uso de streams:
        return getAllPossiblePositions().stream()
             .filter(condition)
             .collect(Collectors.toList());
    }
    
    @Override
    public List<HexPosition> getAdjacentPositions(HexPosition position) {
        // Obtener las 6 posiciones adyacentes en un tablero hexagonal
        // Direcciones hexagonales: (+1,0), (+1,-1), (0,-1), (-1,0), (-1,+1), (0,+1)
        // Filtrar las que estén dentro de los límites del tablero
        // 
        // Pista: Crear array de direcciones y usar streams para mapear
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
        // Verificar si una posición está en el conjunto de bloqueadas
        // Método simple de consulta
        return blockedPositions.contains(position);
    }
    
    // Método auxiliar que los estudiantes pueden implementar
    private List<HexPosition> getAllPossiblePositions() {
        // Generar todas las posiciones válidas del tablero
        // Usar doble loop para q y r, calcular s = -q - r
        // Filtrar posiciones que estén dentro de los límites
        List<HexPosition> positions = new ArrayList<>();
        for (int q = -size + 1; q < size; q++) {
            for (int r = -size + 1; r < size; r++) {
                int s = -q - r; // Calcular s para mantener la relación q + r + s = 0
                HexPosition pos = new HexPosition(q, r);
                //La suma q + r + s debe ser 0, y todos dentro del rango
                if (Math.abs(s) <= size) {
                        positions.add(pos);
                    }
                }
        }
        throw new UnsupportedOperationException("Método auxiliar para implementar");
    }
    
    // Hook method override - ejemplo de extensibilidad
    @Override
    protected void onMoveExecuted(HexPosition position) {
        //TODO Los estudiantes pueden agregar lógica adicional aquí
        // Ejemplos: logging, notificaciones, validaciones post-movimiento
        super.onMoveExecuted(position);
    }
} 