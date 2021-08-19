package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;

public abstract class _0_SavingPVMovesPath extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    /** Лучшие ходы с предыдущего хода */
    private Move[] pvMoves;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int negamax(
            final boolean isMyMove,
            int alfa,
            final int beta,
            final int depth,
            final Move[] pvMoves) {

        if (depth == 0 || isTerminalNode(currNode))
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);

        for (final Move move : allMoves) {
            makeMove(move);
            final int value = -negamax(!isMyMove, -beta, -alfa, depth - 1, pvMoves);
            undoMove(move);

            if (beta <= value) return beta; // Cut-node - fail-high
            // if (beta <= value) return value; // Cut-node - fail-soft

            if (alfa < value && value < beta) { // PV-node
                alfa = value;
                pvMoves[MAX_DEPTH - depth] = move;
            }

            if (value <= alfa) {} // All-node - fail-low
        }

        return alfa;
    }

    /*------------ Точка запуска алгоритма поиска ------------*/

    Move getNextBestMove() {
        final boolean isMyMove = true;
        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);

        if (isTerminalNode(currNode)) throw new RuntimeException("Ходов нет, игра окончена");

        /*------------ Сортир овка ходов ------------*/

        if (pvMoves == null) { // если это первый ход
            pvMoves = new Move[MAX_DEPTH];
        } else {
            MoveSorter.pvMovesSort(allMoves, pvMoves);
        }

        /*------------ Далее обычный поиск ------------*/

        int bestValue = MIN_VALUE;

        for (final Move move : allMoves) {
            makeMove(move);
            final int value = -negamax(!isMyMove, MIN_VALUE, MAX_VALUE, MAX_DEPTH, pvMoves);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
            }
        }

        return pvMoves[0];
    }
}
