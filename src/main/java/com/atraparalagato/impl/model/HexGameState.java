package com.atraparalagato.impl.model;

import com.atraparalagato.base.model.GameState;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación esqueleto de GameState para tableros hexagonales.
 * 
 * Los estudiantes deben completar los métodos marcados con TODO.
 * 
 * Conceptos a implementar:
 * - Estado del juego más sofisticado que ExampleGameState
 * - Sistema de puntuación avanzado
 * - Lógica de victoria/derrota más compleja
 * - Serialización eficiente
 * - Manejo de eventos y callbacks
 */
public class HexGameState extends GameState<HexPosition> {
    
    private HexPosition catPosition;
    private HexGameBoard gameBoard;
    private final int boardSize;
    public Boolean paused = false;
    
    // Los estudiantes pueden agregar más campos según necesiten
    // Ejemplos: tiempo de juego, dificultad, power-ups, etc.
    
    public HexGameState(String gameId, int boardSize) {
        super(gameId);
        this.boardSize = boardSize;

        //Inicializar el tablero y posición inicial del gato
        this.gameBoard = new HexGameBoard(boardSize);
        this.catPosition = new HexPosition( 0, 0); //Gato empieza en el centro
        // Pista: Usar HexGameBoard y posicionar el gato en el centro
        //throw new UnsupportedOperationException("Los estudiantes deben implementar el constructor");
    }
    
    @Override
    protected boolean canExecuteMove(HexPosition position) {
        // Implementar validación de movimientos más sofisticada
        // Considerar:
        // 1. Validación básica del tablero
        // 2. Reglas específicas del juego
        // 3. Estado actual del juego
        // 4. Posibles restricciones adicionales
        return gameBoard.isValidMove(position);

    }
    
    @Override
    protected boolean performMove(HexPosition position) {
        // Ejecutar el movimiento en el tablero
        // Debe actualizar el estado del tablero y verificar consecuencias
        // Retornar true si el movimiento fue exitoso
        return gameBoard.makeMove(position);
    }
    
    @Override
    public void updateGameStatus() {
        // Implementar lógica de determinación de estado del juego
        // Debe verificar:
        // 1. Si el gato llegó al borde (PLAYER_LOST)
        if(isCatAtBorder()) {
            setStatus(GameStatus.PLAYER_LOST);
        // 2. Si el gato está atrapado (PLAYER_WON)
        }else if (isCatTrapped()) {
            setStatus(GameStatus.PLAYER_WON);
        // 3. Si el juego continúa (IN_PROGRESS)
        } else {
            setStatus(GameStatus.IN_PROGRESS);
            
        }
    }       
    
    
    @Override
    public HexPosition getCatPosition() {
        return catPosition;
    }
    
    @Override
    public void setCatPosition(HexPosition position) {
        // IMPORTANTE: Debe llamar a updateGameStatus() después de mover el gato
        // para verificar si el juego terminó
        this.catPosition = position;
        updateGameStatus();

    }
    
    @Override
    public boolean isGameFinished() {
        // Verificar si el juego ha terminado
        // Puede basarse en getStatus() o implementar lógica adicional
        return getStatus() != GameStatus.IN_PROGRESS;
    }
    
    @Override
    public boolean hasPlayerWon() {
        // Verificar si el jugador ganó
        // Determinar las condiciones específicas de victoria
        return getStatus() == GameStatus.PLAYER_WON;
    }
    
    @Override
    public int calculateScore() {
        // Implementar sistema de puntuación más sofisticado que ExampleGameState
        // Considerar factores como:
        // 1. Número de movimientos (menos es mejor)
        // 2. Tiempo transcurrido
        // 3. Tamaño del tablero (más difícil = más puntos)
        // 4. Bonificaciones especiales
        // 5. Penalizaciones por movimientos inválidos
        if(hasPlayerWon()){
            return Math.max(0, 1000 - getMoveCount()*10 + boardSize * 50 - getTimeElapsed());
        }
        else {
            return Math.max(0, 100 - getMoveCount() * 5);
        }
    }
    
    @Override
    public Object getSerializableState() {
        // Crear un mapa con el estado serializable
        Map<String, Object> state = new HashMap<>();
        state.put("gameId", getGameId());
        state.put("catPosition", Map.of("q", catPosition.getQ(), "r", catPosition.getR()));
        state.put("blockedCells", gameBoard.getBlockedPositions());
        state.put("status", getStatus().toString());
        state.put("moveCount", getMoveCount());
        state.put("boardSize", boardSize);
        return state;
    }
    
    @Override
    public void restoreFromSerializable(Object serializedState) {
        // Restaurar el estado desde una representación serializada
        // Debe ser compatible con getSerializableState()
        // Manejar errores y validar la integridad de los datos
        if (serializedState instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) serializedState;
            
            // Restaurar posición del gato
            @SuppressWarnings("unchecked")
            Map<String, Integer> catPos = (Map<String, Integer>) state.get("catPosition");
            if (catPos != null) {
                this.catPosition = new HexPosition(catPos.get("q"), catPos.get("r"));
            }
            
            // Restaurar estado del juego
            String statusStr = (String) state.get("status");
            if (statusStr != null) {
                setStatus(GameStatus.valueOf(statusStr));
            }
        }
    }
    
    // Métodos auxiliares que los estudiantes pueden implementar
    
    /**
     * Verificar si el gato está en el borde del tablero.
     * Los estudiantes deben definir qué constituye "el borde".
     */
    private boolean isCatAtBorder() {
        return Math.abs(catPosition.getQ()) == boardSize -1 || 
               Math.abs(catPosition.getR()) == boardSize-1 || 
               Math.abs(catPosition.getS()) == boardSize-1;
    }
    
    /**
     * Verificar si el gato está completamente atrapado.
     * Debe verificar si todas las posiciones adyacentes están bloqueadas.
     */
    private boolean isCatTrapped() {
        return gameBoard.getAdjacentPositions(catPosition).stream()
                .allMatch(gameBoard::isBlocked);
    }
    
    /**
     * Calcular estadísticas avanzadas del juego.
     * Puede incluir métricas como eficiencia, estrategia, etc.
     */
    public Map<String, Object> getAdvancedStatistics() {
        int q = catPosition.getQ();
        int r = catPosition.getR();
        int s = -q - r; // Cálculo de la tercera coordenada en hexágonos
        String estrategia = "";

        int eficiencia = calculateScore() / (getMoveCount());
        int distancia_origen = ((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2); // Ejemplo simple

        if (distancia_origen > 4){
            estrategia = "Mala"; // Evitar división por cero
        }else if(distancia_origen > 2){
            estrategia = "Regular";
        }
        else {
            estrategia = "Buena";
        }

        return Map.of(
            "score", calculateScore(),
            "moveCount", getMoveCount(),
            "eficiencia", eficiencia,
            "estrategia", estrategia
        );
    }
    
    // Getters adicionales que pueden ser útiles
    
    public HexGameBoard getGameBoard() {
        return gameBoard;
    }
    
    public int getBoardSize() {
        return boardSize;
    }
    
    // 2Los estudiantes pueden agregar más métodos según necesiten
    // Ejemplos: getDifficulty(), getTimeElapsed(), getPowerUps(), etc.

    public int getTimeElapsed() {
        // Implementar lógica para calcular el tiempo transcurrido desde la creación del juego
        // Puede usar createdAt y LocalDateTime.now()
        return (int) java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();
    }
} 