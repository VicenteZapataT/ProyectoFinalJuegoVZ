package com.atraparalagato.base.model;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Clase base abstracta para el tablero del juego.
 * 
 * SOLID Principles:
 * - Single Responsibility: Maneja únicamente la lógica del tablero
 * - Open/Closed: Abierta para extensión, cerrada para modificación
 * - Liskov Substitution: Las implementaciones deben ser intercambiables
 * 
 * Conceptos a implementar:
 * - Programación Funcional: Uso de Predicates y funciones de orden superior
 * - Modularización: Separación clara de responsabilidades
 * - OOP: Herencia y polimorfismo
 */
public abstract class GameBoard<T extends Position> {
    
    protected final int size;
    public Set<T> blockedPositions;
    
    protected GameBoard(int size) {
        this.size = size;
        this.blockedPositions = initializeBlockedPositions();
    }
    
    // Template Method Pattern - Define el algoritmo general
    public final boolean makeMove(T position) {
        if (!isValidMove(position)) {
            return false;
        }
        
        executeMove(position);
        onMoveExecuted(position);
        return true;
    }
    
    /**
     * Inicializa la estructura de datos para posiciones bloqueadas.
     * Los estudiantes deben decidir qué estructura usar (Set, List, Map, etc.)
     */
    protected abstract Set<T> initializeBlockedPositions();
    
    /**
     * Valida si una posición es válida en el tablero.
     * Debe considerar límites del tablero y reglas específicas.
     */
    protected abstract boolean isPositionInBounds(T position);
    
    /**
     * Determina si un movimiento es válido.
     * Combina validación de límites y estado actual.
     */
    protected abstract boolean isValidMove(T position);
    
    /**
     * Ejecuta el movimiento en el tablero.
     * Actualiza el estado interno del tablero.
     */
    protected abstract void executeMove(T position);
    
    /**
     * Hook method - Se ejecuta después de cada movimiento.
     * Permite extensiones futuras sin modificar la clase base.
     */
    protected void onMoveExecuted(T position) {
        // Default: no operation
    }
    
    /**
     * Obtiene todas las posiciones válidas según un criterio.
     * Ejemplo de programación funcional con Predicate.
     */
    public abstract List<T> getPositionsWhere(Predicate<T> condition);
    
    /**
     * Obtiene posiciones adyacentes a una posición dada.
     * Fundamental para algoritmos de pathfinding.
     */
    public abstract List<T> getAdjacentPositions(T position);
    
    /**
     * Verifica si una posición está bloqueada.
     * Método de consulta básico.
     */
    public abstract boolean isBlocked(T position);
    
    /**
     * Obtiene el tamaño del tablero.
     */
    public final int getSize() {
        return size;
    }
    
    /**
     * Obtiene todas las posiciones bloqueadas.
     * Útil para serialización y estado del juego.
     */
    public final Set<T> getBlockedPositions() {
        return Set.copyOf(blockedPositions);
    }
} 