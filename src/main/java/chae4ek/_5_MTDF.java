package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;

public abstract class _5_MTDF extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    /** Максимальное время на ход */
    private static final long MAX_TIME = 5000;

    private GameNode currNode; // текущее состояние игры

    /*------------ Получение следующего хода у бота ------------*/

    private static boolean timesUp(final long startTime) {
        return System.currentTimeMillis() - startTime > MAX_TIME;
    }

    public int iterativeDeepening(final long startTime, int firstGuess, final int depth) {
        for (int d = 1; d <= depth; ++d) {
            firstGuess = MTDF(firstGuess, d);
            if (timesUp(startTime)) break;
        }
        return firstGuess;
    }

    private int MTDF(final int firstGuess, final int depth) {
        int value = firstGuess;
        int lowerBound = MIN_VALUE;
        int upperBound = MAX_VALUE;
        int beta;

        do {
            beta = Math.max(value, lowerBound + 1);

            value = searchAlgorithmWithTT(beta - 1, beta, depth);

            if (value < beta) upperBound = value;
            else lowerBound = value;

        } while (lowerBound < upperBound);

        return value;
    }

    /** @return должен возвращать лучшую оценку для максимизирующего игрока */
    int searchAlgorithmWithTT(final int alfa, final int beta, final int depth) {
        return -negamaxTT(false, -beta, -alfa, depth); // либо любой другой алгоритм
    }

    abstract int negamaxTT(boolean isMyMove, int alfa, int beta, int depth);

    /*------------ Точка запуска алгоритма поиска ------------*/

    Move getNextBestMove() {
        final boolean isMyMove = true;
        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);

        if (isTerminalNode(currNode)) throw new RuntimeException("Ходов нет, игра окончена");

        int bestValue = MIN_VALUE;
        Move bestMove = allMoves.getFirst();

        final long startTime = System.currentTimeMillis();

        for (final Move move : allMoves) {
            makeMove(move);
            final int value = iterativeDeepening(startTime, 0, MAX_DEPTH);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
