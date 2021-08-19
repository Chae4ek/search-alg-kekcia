package chae4ek;

import static chae4ek.engine.GameEngineStuff.Move.MoveType.ATTACK_MOVE;

import chae4ek.engine.GameEngineStuff;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public abstract class MoveSorter extends GameEngineStuff {

    private static final int SCALE = 1;

    /** Сортирует ходы на основе предыдущего лучшего пути pvMoves */
    public static void pvMovesSort(final Deque<Move> allMoves, final Move[] pvMoves) {
        for (int i = pvMoves.length - 1; i >= 0; --i) {
            if (allMoves.contains(pvMoves[i])) {
                allMoves.remove(pvMoves[i]);
                allMoves.addFirst(pvMoves[i]);
            }
        }
    }

    /**
     * Сортирует ходы на основе эвристики истории
     *
     * @param color <br>
     *     1 - максимизирующий игрок<br>
     *     0 - минимизирующий игрок
     */
    public static void historyHeuristicSort(
            final List<Move> allMoves, final int[][][] moveHistory, final int color) {
        allMoves.sort(
                Comparator.<Move>comparingInt(m -> moveHistory[color][m.from][m.to]).reversed());
    }

    /** Сортирует ходы на основе эвристики бабочки */
    public static void butterflyHeuristicSort(
            final List<Move> allMoves, final int[][][] butterfly, final int color) {
        allMoves.sort(
                Comparator.<Move>comparingInt(m -> butterfly[color][m.from][m.to]).reversed());
    }

    /** Сортирует ходы на основе эвристики истории и бабочки */
    public static void relativeHistoryHeuristicSort(
            final List<Move> allMoves,
            final int[][][] moveHistory,
            final int[][][] butterfly,
            final int color) {
        allMoves.sort(
                Comparator.<Move>comparingInt(
                                m ->
                                        (SCALE * moveHistory[color][m.from][m.to])
                                                / butterfly[color][m.from][m.to])
                        .reversed());
    }

    /** @return отсортированный итератор по убыванию (макс. жертва - мин. агрессор) */
    public static Iterator<Move> MVV_LVA_filter(final Board board, final List<Move> allMoves) {
        return allMoves.stream()
                .filter(move -> move.moveType == ATTACK_MOVE)
                .sorted(
                        Comparator.comparingInt(
                                m -> board.getFigureOn(m.from) - board.getFigureOn(m.to)))
                .iterator();
    }
}
