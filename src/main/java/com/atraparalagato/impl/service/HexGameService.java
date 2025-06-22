package com.atraparalagato.impl.service;

import com.atraparalagato.base.service.GameService;
import com.atraparalagato.base.model.GameState;
import com.atraparalagato.base.model.GameBoard;
import com.atraparalagato.base.strategy.CatMovementStrategy;
import com.atraparalagato.impl.model.HexPosition;
import com.atraparalagato.impl.repository.H2GameRepository;
import com.atraparalagato.impl.strategy.AStarCatMovement;
import com.atraparalagato.impl.strategy.BFSCatMovement;
import com.atraparalagato.impl.model.HexGameState;
import com.atraparalagato.impl.model.HexGameBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atraparalagato.base.repository.DataRepository;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementación esqueleto de GameService para el juego hexagonal.
 * 
 * Los estudiantes deben completar los métodos marcados con TODO.
 * 
 * Conceptos a implementar:
 * - Orquestación de todos los componentes del juego
 * - Lógica de negocio compleja
 * - Manejo de eventos y callbacks
 * - Validaciones avanzadas
 * - Integración con repositorio y estrategias
 */
public class HexGameService extends GameService<HexPosition> {
    
    // Los estudiantes deben inyectar dependencias
    // Ejemplos: repository, movementStrategy, validator, etc.

    private final H2GameRepository gameRepository;
    private final CatMovementStrategy<HexPosition> movementStrategy;
    private final Function<Integer, GameBoard<HexPosition>> boardFactory;
    private final Function<String, GameState<HexPosition>> gameStateFactory;
    private final Supplier<String> gameIdGenerator;
        
    public HexGameService() {
        // Los estudiantes deben inyectar las dependencias requeridas
        // Configuración del DataSource para H2 en memoria
        super(
            new HexGameBoard(11), // GameBoard<HexPosition>
            new BFSCatMovement(new HexGameBoard(11)), // CatMovementStrategy<HexPosition>
            // Cast to match DataRepository<GameState<HexPosition>, String>
            (DataRepository<GameState<HexPosition>, String>) (DataRepository<?, ?>)
                new H2GameRepository(),
            () -> java.util.UUID.randomUUID().toString(), // Supplier<String>
            size -> new HexGameBoard(size), // Function<Integer, GameBoard<HexPosition>>
            id -> new HexGameState(id, 11) // Function<String, GameState<HexPosition>>
        );
        
        // Inicializar el repositorio con el DataSource configurado
        this.gameRepository = new H2GameRepository();
    
        // Inicializar dependencias y configuración
        // Pista: Usar el patrón Factory para crear componentes
        throw new UnsupportedOperationException("Los estudiantes deben implementar el constructor");
    }
    
      
    /**
     * Crear un nuevo juego con configuración personalizada.
     * Debe ser más sofisticado que ExampleGameService.
     */
    public HexGameState startNewGame(int boardSize, String difficulty, Map<String, Object> options) {
        // 1. Validar parámetros de entrada
        if (boardSize < 3) {
            throw new IllegalArgumentException("El tamaño del tablero debe ser al menos 3");
        }
        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = "normal";
        }

        // 2. Crear tablero según dificultad (puedes personalizar según dificultad)
        HexGameBoard board = new HexGameBoard(boardSize);

        // 3. Configurar estrategia del gato según dificultad
        CatMovementStrategy<HexPosition> movementStrategy = createMovementStrategy(difficulty, board);

        // 4. Inicializar estado del juego
        String gameId = generateGameId();
        HexGameState gameState = new HexGameState(gameId, boardSize);

        // 5. Guardar en repositorio
        gameRepository.save(gameState);

        // 6. Configurar callbacks y eventos
        configureGameCallbacks(gameState);

        // Puedes agregar lógica adicional para opciones avanzadas aquí

        return gameState;
    }
    
    /**
     * Ejecutar movimiento del jugador con validaciones avanzadas.
     */
    public Optional<HexGameState> executePlayerMove(String gameId, HexPosition position, String playerId) {
           // 1. Obtener el estado actual del juego
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Optional.empty();
        }
        HexGameState gameState = gameStateOpt.get();

        // 2. Validar si el juego ya terminó
        if (gameState.isGameFinished()) {
            return Optional.of(gameState);
        }

        // 3. Validar si el movimiento es válido según las reglas del tablero
        if (!gameState.getGameBoard().isValidMove(position)) {
            return Optional.of(gameState);
        }

        // 4. Ejecutar el movimiento del jugador (bloquear la celda)
        gameState.getGameBoard().executeMove(position);

        // 5. Incrementar el contador de movimientos
        gameState.incrementMoveCount();

        // 6. Actualizar el estado del juego (verifica si el jugador ganó/perdió)
        gameState.updateGameStatus();

        // 7. Guardar el estado actualizado
        gameRepository.save(gameState);

        return Optional.of(gameState);    
    }

    
    /**
     * Obtener estado del juego con información enriquecida.
     */
    public Optional<Map<String, Object>> getEnrichedGameState(String gameId) {
        // Obtener estado enriquecido del juego
        // Incluir:
        // 1. Estado básico del juego
        // 2. Estadísticas avanzadas
        // 3. Sugerencias de movimiento
        // 4. Análisis de la partida
        // 5. Información del tablero
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Optional.empty();
        }
        HexGameState gameState = gameStateOpt.get();

        Map<String, Object> enriched = new HashMap<>();

        // 1. Estado básico del juego (serializable)
        enriched.put("basicState", gameState.getSerializableState());

        // 2. Estadísticas avanzadas
        enriched.put("advancedStats", gameState.getAdvancedStatistics());

        // 3. Sugerencia de movimiento (simple: sugerir la primera celda libre adyacente al gato)
        List<HexPosition> possibleMoves = gameState.getGameBoard().getAdjacentPositions(gameState.getCatPosition())
            .stream()
            .filter(pos -> gameState.getGameBoard().isValidMove(pos))
            .toList();
        HexPosition suggestedMove = possibleMoves.isEmpty() ? null : possibleMoves.get(0);
        enriched.put("suggestedMove", suggestedMove);

        // 4. Análisis de la partida (usa el método analyzeGame si está implementado)
        enriched.put("analysis", analyzeGame(gameId));

        // 5. Información del tablero (bloqueadas y tamaño)
        enriched.put("blockedPositions", gameState.getGameBoard().getBlockedPositions());
        enriched.put("boardSize", gameState.getBoardSize());

        return Optional.of(enriched);
    }
    
    /**
     * Obtener sugerencia inteligente de movimiento.
     */
    public Optional<HexPosition> getIntelligentSuggestion(String gameId, String difficulty) {
        // Generar sugerencia inteligente
        // Considerar:
        // 1. Analizar estado actual del tablero
        // 2. Predecir movimientos futuros del gato
        // 3. Evaluar múltiples opciones
        // 4. Retornar la mejor sugerencia según dificultad

        // 1. Obtener el estado actual del juego
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Optional.empty();
        }
        HexGameState gameState = gameStateOpt.get();

        // 2. Analizar el estado actual del tablero
        HexGameBoard board = gameState.getGameBoard();
        HexPosition catPosition = gameState.getCatPosition();

        // 3. Obtener todos los movimientos posibles del jugador (celdas no bloqueadas)
        List<HexPosition> possibleMoves = board.getPositionsWhere(pos -> board.isValidMove(pos));

        // 4. Predecir movimientos futuros del gato usando la estrategia de la dificultad
        CatMovementStrategy<HexPosition> strategy = createMovementStrategy(difficulty, board);
        HexPosition targetPosition = getTargetPosition(gameState);

        // 5. Evaluar cada movimiento posible: simular el movimiento y ver qué tan cerca queda el gato del borde
        HexPosition bestMove = null;
        int minCatEscapeDistance = Integer.MAX_VALUE;

        for (HexPosition move : possibleMoves) {
            // Simular el movimiento: bloquear la celda
            board.executeMove(move);

            // Predecir el siguiente movimiento del gato
            HexPosition nextCatPosition = null;
            if (strategy instanceof BFSCatMovement bfs) {
                List<HexPosition> catMoves = board.getAdjacentPositions(catPosition);
                nextCatPosition = bfs.selectBestMove(catMoves, catPosition, targetPosition).orElse(catPosition);
            } else if (strategy instanceof AStarCatMovement astar) {
                List<HexPosition> catMoves = board.getAdjacentPositions(catPosition);
                nextCatPosition = astar.selectBestMove(catMoves, catPosition, targetPosition).orElse(catPosition);
            }

            // Calcular la distancia del gato al borde después de este movimiento
            int q = nextCatPosition.getQ();
            int r = nextCatPosition.getR();
            int s = nextCatPosition.getS();
            int boardSize = board.getSize() - 1;
            int catEscapeDistance = Math.min(Math.min(boardSize - Math.abs(q), boardSize - Math.abs(r)), boardSize - Math.abs(s));

            // Deshacer la simulación (quitar el bloqueo)
            board.getBlockedPositions().remove(move);

            // Elegir el movimiento que deja al gato más lejos del borde
            if (catEscapeDistance > minCatEscapeDistance) {
                continue;
            }
            if (catEscapeDistance < minCatEscapeDistance) {
                minCatEscapeDistance = catEscapeDistance;
                bestMove = move;
            }
        }

        return Optional.ofNullable(bestMove);
    }
    
    /**
     * Analizar la partida y generar reporte.
     */
    public Map<String, Object> analyzeGame(String gameId) {
        // Generar análisis completo de la partida
        // Incluir:
        // 1. Eficiencia de movimientos
        // 2. Estrategia utilizada
        // 3. Momentos clave de la partida
        // 4. Sugerencias de mejora
        // 5. Comparación con partidas similares
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Map.of("error", "No se encontró la partida con el ID proporcionado");
        }
        HexGameState gameState = gameStateOpt.get();

        // 1. Eficiencia de movimientos y estrategia utilizada (usa método avanzado del estado)
        Map<String, Object> advancedStats = gameState.getAdvancedStatistics();

        // 2. Momentos clave de la partida (ejemplo: primer y último movimiento)
        // Como no hay historial, solo se puede mostrar el inicio y el final
        Map<String, Object> keyMoments = Map.of(
            "inicio", Map.of("catPosition", new HexPosition(0, 0)),
            "final", Map.of("catPosition", gameState.getCatPosition())
        );

        // 3. Sugerencias de mejora (ejemplo simple según eficiencia)
        String sugerencia;
        int eficiencia = (int) advancedStats.getOrDefault("eficiencia", 0);
        if (eficiencia < 10) {
            sugerencia = "Intenta bloquear caminos más cercanos al gato para atraparlo más rápido.";
        } else if (eficiencia < 30) {
            sugerencia = "¡Buen trabajo! Puedes mejorar anticipando los movimientos del gato.";
        } else {
            sugerencia = "¡Excelente estrategia! Sigue así.";
        }

        // 4. Comparación con partidas similares (ejemplo: promedio de score en partidas del mismo tamaño)
        int boardSize = gameState.getBoardSize();
        List<HexGameState> sameSizeGames = gameRepository.findAll().stream()
            .filter(g -> g.getBoardSize() == boardSize)
            .toList();
        double avgScore = sameSizeGames.stream()
            .mapToInt(HexGameState::calculateScore)
            .average()
            .orElse(0.0);

        Map<String, Object> comparacion = Map.of(
            "scoreActual", gameState.calculateScore(),
            "scorePromedioTableroMismoTamaño", avgScore
        );

        // 5. Armar el reporte final
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("estadisticasAvanzadas", advancedStats);
        reporte.put("momentosClave", keyMoments);
        reporte.put("sugerencias", sugerencia);
        reporte.put("comparacion", comparacion);

        return reporte;}
    
    /**
     * Obtener estadísticas globales del jugador.
     */
    public Map<String, Object> getPlayerStatistics(String playerId) {
        // Calcular estadísticas del jugador
        // Incluir:
        // 1. Número de partidas jugadas
        // 2. Porcentaje de victorias
        // 3. Puntuación promedio
        // 4. Tiempo promedio por partida
        // 5. Progresión en el tiempo
        // Obtener todas las partidas del repositorio
        // Obtener todas las partidas del repositorio
        // Obtener todas las partidas del repositorio
        List<HexGameState> allGames = gameRepository.findAll();

        // Filtrar partidas del jugador buscando el id en el estado serializado (si lo guardaste ahí)
        List<HexGameState> playerGames = allGames.stream()
            .filter(game -> {
                Object serializable = game.getSerializableState();
                if (serializable instanceof Map<?, ?> stateMap) {
                    Object pid = stateMap.get("playerId");
                    return playerId.equals(pid);
                }
                return false;
            })
            .toList();

        int totalGames = playerGames.size();
        int victories = (int) playerGames.stream().filter(HexGameState::hasPlayerWon).count();
        double winRate = totalGames > 0 ? (victories * 100.0) / totalGames : 0.0;
        double avgScore = playerGames.stream().mapToInt(HexGameState::calculateScore).average().orElse(0.0);
        double avgTime = playerGames.stream().mapToInt(HexGameState::getTimeElapsed).average().orElse(0.0);

        // Progresión de puntuaciones (ordenadas por tiempo de creación si lo necesitas)
        List<Integer> scoreProgression = playerGames.stream()
            .map(HexGameState::calculateScore)
            .toList();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", totalGames);
        stats.put("victories", victories);
        stats.put("winRate", winRate);
        stats.put("averageScore", avgScore);
        stats.put("averageDuration", avgTime);
        stats.put("scoreProgression", scoreProgression);

        return stats;
    }
    
    /**
    * Configurar dificultad del juego.
     */
    public void setGameDifficulty(String gameId, String difficulty) {
        //  Cambiar dificultad del juego
        // Afectar:
        // 1. Estrategia de movimiento del gato
        // 2. Tiempo límite por movimiento
        // 3. Ayudas disponibles
        // 4. Sistema de puntuación
         Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe un juego con el ID proporcionado");
        }
        HexGameState gameState = gameStateOpt.get();

        // Obtener el tablero actual
        HexGameBoard board = gameState.getGameBoard();

        // Crear la nueva estrategia de movimiento según la dificultad
        CatMovementStrategy<HexPosition> newStrategy = createMovementStrategy(difficulty, board);

        // No hay campo de estrategia en HexGameState, así que la estrategia debe usarse en el servicio
        // Puedes guardar la dificultad en el estado si tienes un campo, o simplemente notificar el cambio

        // Guardar el estado actualizado (si modificaste algún campo relevante)
        gameRepository.save(gameState);

        // Notificar el cambio de dificultad (opcional)
        notifyGameEvent(gameId, "difficulty_changed", Map.of("difficulty", difficulty));
}
    
    /**
     * Pausar/reanudar juego.
     */
    public boolean toggleGamePause(String gameId) {
        // Manejar pausa del juego
        // Considerar:
        // 1. Guardar timestamp de pausa
        // 2. Actualizar estado del juego
        // 3. Notificar cambio de estado
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return false;
        }
        HexGameState gameState = gameStateOpt.get();

        // Cambia el estado del campo 'paused' directamente
        // (Asegúrate de que 'paused' sea un campo booleano público o con acceso de paquete)
        boolean newPauseState = !(gameState.paused); // invierte el estado actual
        gameState.paused = newPauseState;

        // (Opcional) Guardar timestamp de pausa/reanudación si tienes ese campo
        // gameState.pauseTimestamp = System.currentTimeMillis();

        // Guardar el estado actualizado
        gameRepository.save(gameState);

        // Notificar el cambio de estado (puedes implementar notifyGameEvent)
        notifyGameEvent(gameId, newPauseState ? "paused" : "resumed", Map.of());

        return newPauseState;
    }
    
    /**
     * Deshacer último movimiento.
     */
    public Optional<HexGameState> undoLastMove(String gameId) {
        // Implementar funcionalidad de deshacer
        // Considerar:
        // 1. Mantener historial de movimientos
        // 2. Restaurar estado anterior
        // 3. Ajustar puntuación
        // 4. Validar que se puede deshacer
        // 1. Obtener el estado actual del juego
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Optional.empty();
        }
        HexGameState gameState = gameStateOpt.get();

        // Si no tienes historial de movimientos, no puedes deshacer
        // Puedes retornar el estado actual sin cambios
        // O lanzar una excepción si lo prefieres
        return Optional.of(gameState);
    }
    
    /**
     * Obtener ranking de mejores puntuaciones.
     */
    public List<Map<String, Object>> getLeaderboard(int limit) {
        // Generar tabla de líderes
        // Incluir:
        // 1. Mejores puntuaciones
        // 2. Información del jugador
        // 3. Fecha de la partida
        // 4. Detalles de la partida
        // 1. Obtener todos los estados de juego
        List<HexGameState> allGames = gameRepository.findAll();

        // 2. Ordenar por puntuación descendente
        List<HexGameState> sortedGames = allGames.stream()
            .sorted((a, b) -> Integer.compare(b.calculateScore(), a.calculateScore()))
            .limit(limit)
            .toList();

        // 3. Mapear a estructura de leaderboard
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (HexGameState game : sortedGames) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("gameId", game.getGameId());
            entry.put("score", game.calculateScore());
            entry.put("moveCount", game.getMoveCount());
            entry.put("catPosition", game.getCatPosition());
            entry.put("status", game.getStatus());
            entry.put("boardSize", game.getBoardSize());
            entry.put("createdAt", game.getCreatedAt()); // Si tienes este campo en GameState
            // Puedes agregar más detalles según tu modelo, como jugador, fecha, etc.
            leaderboard.add(entry);
        }
        return leaderboard;
}
    
    // Métodos auxiliares que los estudiantes pueden implementar
    
    /**
     * Validar movimiento según reglas avanzadas.
     */
    private boolean isValidAdvancedMove(HexGameState gameState, HexPosition position, String playerId) {
        // 1. Validación básica del tablero
        if (!gameState.getGameBoard().isValidMove(position)) {
            return false;
        }

        // 2. Verificar si el juego ya terminó
        if (gameState.isGameFinished()) {
            return false;
        }

        // Si pasa todas las validaciones, el movimiento es válido
        return true;
}
    
    /**
     * Ejecutar movimiento del gato usando estrategia apropiada.
     */
    private void executeCatMove(HexGameState gameState, String difficulty) {
        HexGameBoard board = gameState.getGameBoard();
        HexPosition currentCatPosition = gameState.getCatPosition();

        // Selecciona la estrategia según la dificultad
        CatMovementStrategy<HexPosition> strategy = createMovementStrategy(difficulty, board);

        // Obtener movimientos posibles
        List<HexPosition> possibleMoves = board.getAdjacentPositions(currentCatPosition);

        // Determinar la posición objetivo (por ejemplo, el borde más cercano)
        HexPosition targetPosition = getTargetPosition(gameState);

        HexPosition nextCatPosition = null;
        if (strategy instanceof BFSCatMovement bfs) {
            nextCatPosition = bfs.selectBestMove(possibleMoves, currentCatPosition, targetPosition)
                                .orElse(null);
        } else if (strategy instanceof AStarCatMovement astar) {
            nextCatPosition = astar.selectBestMove(possibleMoves, currentCatPosition, targetPosition)
                                .orElse(null);
        }

        // Si el movimiento es válido, actualiza la posición del gato
        if (nextCatPosition != null && board.isValidMove(nextCatPosition)) {
            gameState.setCatPosition(nextCatPosition);
        }
    }
    
    
    /**
     * Calcular puntuación avanzada.
     */
    private int calculateAdvancedScore(HexGameState gameState, Map<String, Object> factors) {
        // Ejemplo de cálculo avanzado de puntuación:
        int baseScore = gameState.calculateScore();

        // Puedes usar factores adicionales si están presentes
        int bonus = 0;
        if (factors != null) {
            // Ejemplo: bonificación por dificultad
            String difficulty = (String) factors.getOrDefault("difficulty", "normal");
            switch (difficulty) {
                case "dificil":
                    bonus += 200;
                    break;
                case "facil":
                    bonus -= 100;
                    break;
                default:
                    break;
            }
            // Ejemplo: penalización por power-ups usados
            Integer powerUpsUsed = (Integer) factors.getOrDefault("powerUpsUsed", 0);
            bonus -= powerUpsUsed * 20;
        }

        // Puedes agregar más lógica según tus reglas
        return Math.max(0, baseScore + bonus);}
    
    /**
     * Notificar eventos del juego.
     */
    private void notifyGameEvent(String gameId, String eventType, Map<String, Object> eventData) {
        System.out.println("Evento de juego:");
        System.out.println("ID del juego: " + gameId);
        System.out.println("Tipo de evento: " + eventType);
        System.out.println("Datos del evento: " + eventData);
    }
    
    /**
     * Crea la estrategia de movimiento del gato según la dificultad.
     * @param difficulty Dificultad seleccionada ("facil", "normal", "dificil", etc.)
     * @param board Tablero de juego
     * @return Estrategia de movimiento para el gato
     */
    private CatMovementStrategy<HexPosition> createMovementStrategy(String difficulty, HexGameBoard board) {
        switch (difficulty.toLowerCase()) {
            case "fácil":
                return new BFSCatMovement(board); // Utiliza BFS para fácil
            case "dificil":            
                return new AStarCatMovement(board); // Utiliza A* para difícil
            case "normal":
            default:
                return new BFSCatMovement(board); // Puedes ajustar según preferencia
        }
    }

    // Métodos abstractos requeridos por GameService
    
    @Override
    protected void initializeGame(GameState<HexPosition> gameState, GameBoard<HexPosition> gameBoard) {
        // Inicializar el juego con estado y tablero
        // Inicializa el estado del juego con el tablero proporcionado
        if (gameState instanceof HexGameState hexGameState) {
        // Asigna el tablero directamente al campo gameBoard
        hexGameState.getGameBoard(); // Si tienes un método copyFrom, úsalo
        hexGameState.setCatPosition(new HexPosition(0, 0)); // Gato en el centro
        } else {
            throw new IllegalArgumentException("Estado del juego o tablero no es compatible con HexGameState/HexGameBoard");
        }
    }
    
    @Override
    public boolean isValidMove(String gameId, HexPosition position) {
        // Validar si un movimiento es válido
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return false;
        }
        HexGameState gameState = gameStateOpt.get();
        return gameState.getGameBoard().isValidMove(position);
}
    
    @Override
    public Optional<HexPosition> getSuggestedMove(String gameId) {
        // Obtener el estado actual del juego
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return Optional.empty();
        }
        HexGameState gameState = gameStateOpt.get();

        // Obtener la posición actual del gato
        HexPosition catPosition = gameState.getCatPosition();

        // Obtener movimientos posibles
        List<HexPosition> possibleMoves = gameState.getGameBoard().getAdjacentPositions(catPosition)
            .stream()
            .filter(pos -> !gameState.getGameBoard().isBlocked(pos))
            .toList();

        // Seleccionar el movimiento más cercano a un borde (como sugerencia simple)
        int size = gameState.getGameBoard().getSize() - 1;
        return possibleMoves.stream()
                .min((a, b) -> {
                    int distA = Math.min(Math.min(Math.abs(a.getQ()), Math.abs(a.getR())), Math.abs(a.getS()));
                    int distB = Math.min(Math.min(Math.abs(b.getQ()), Math.abs(b.getR())), Math.abs(b.getS()));
                    return Integer.compare(distA, distB);
        });
    }
    
    @Override
    protected HexPosition getTargetPosition(GameState<HexPosition> gameState) {
        // Obtener posición objetivo para el gato
        HexGameState hexState = (HexGameState) gameState;
        HexGameBoard board = hexState.getGameBoard();
        HexPosition catPosition = hexState.getCatPosition();

        // Encuentra todas las posiciones de borde
        List<HexPosition> borderPositions = board.getPositionsWhere(pos -> {
            int size = board.getSize() - 1;
            return Math.abs(pos.getQ()) == size ||
                Math.abs(pos.getR()) == size ||
                Math.abs(pos.getS()) == size;
        });

        // Selecciona la posición de borde más cercana al gato
        HexPosition closest = null;
        double minDist = Double.POSITIVE_INFINITY;
        for (HexPosition border : borderPositions) {
            int dq = Math.abs(catPosition.getQ() - border.getQ());
            int dr = Math.abs(catPosition.getR() - border.getR());
            int ds = Math.abs(catPosition.getS() - border.getS());
            double dist = Math.max(dq, Math.max(dr, ds));
            if (dist < minDist) {
                minDist = dist;
                closest = border;
            }
        }
        return closest;
}
    
    @Override
    public Object getGameStatistics(String gameId) {
        //Obtener estadísticas del juego
        Optional<HexGameState> gameStateOpt = gameRepository.findById(gameId);
        if (gameStateOpt.isEmpty()) {
            return null;
        }
        GameState<HexPosition> gameState = gameStateOpt.get();

        // Puedes personalizar las estadísticas según tu lógica
        // Ejemplo: número de movimientos, posiciones bloqueadas, estado del gato, etc.
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("moveCount", gameState.getMoveCount());
        stats.put("blockedPositions", ((HexGameState) gameState).getGameBoard().getBlockedPositions().size());
        stats.put("catPosition", ((HexGameState) gameState).getCatPosition());
        stats.put("isGameOver", gameState.isGameFinished());

        return stats;
    }
}