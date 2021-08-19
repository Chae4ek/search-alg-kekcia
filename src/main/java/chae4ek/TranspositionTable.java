package chae4ek;

import chae4ek.engine.GameEngineStuff;
import chae4ek.engine.GameEngineStuff.Move;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TranspositionTable<T extends Collection<Move>> extends GameEngineStuff {

    /** Максимальное число состояний, заносимых в ТТ - вычисляется экспериментально */
    private static final int MAX_NODES = 1000000;

    private final Map<GameNode, TTEntry<T>> entries = new ConcurrentHashMap<>(MAX_NODES);

    /**
     * @param node состояние игры
     * @return вхождение состояния игры или null, если такое состояние еще не встречалось
     */
    public TTEntry<T> find(final GameNode node) {
        return entries.get(node);
    }

    /**
     * Создает новое вхождение и добавляет его в ТТ, либо заменяет, если оно уже есть
     *
     * @param allMoves все возможные текущие ходы
     * @param result лучшая оценка
     * @param node состояние игры
     * @param alfaOrigin первоначальная альфа в отсечениях
     * @param betaOrigin первоначальная бета в отсечениях
     * @param depth текущая глубина
     */
    public void store(
            final T allMoves, // по желанию
            final int result,
            final GameNode node,
            final int alfaOrigin,
            final int betaOrigin,
            final int depth) {
        final TTEntry<T> entry = new TTEntry<>(allMoves, depth);

        if (result <= alfaOrigin) entry.upperBound = result;
        if (result >= betaOrigin) entry.lowerBound = result;
        if (alfaOrigin < result && result < betaOrigin)
            entry.lowerBound = entry.upperBound = result;

        entries.put(node, entry);
    }

    public static class TTEntry<T extends Collection<Move>> {

        public T allMoves; // по желанию
        public int lowerBound = MIN_VALUE;
        public int upperBound = MAX_VALUE;
        public int depth;

        public TTEntry(final T allMoves, final int depth) {
            this.allMoves = allMoves;
            this.depth = depth;
        }
    }
}
