package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;
import java.util.Iterator;

public abstract class _2_NullMove extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    public static final int DEPTH_REDUCTION = 2;

    private static final int MAX_DEPTH = 271828183;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    abstract int getFigureCount(boolean color);

    abstract boolean canMove(boolean color);

    boolean isAllowNullMove(final boolean color, final boolean isPrevNullMove) {
        return !isPrevNullMove && canMove(!color) && getFigureCount(!color) > 5;
        /*
         * null-move запрещен, если выполнено одно из следующих условий:
         *  0. Предыдущий ход был нулевым ходом
         *  1. Противник имеет только короля и пешки
         *  2. У противника осталось мало материала
         *  3. Осталось мало материала на доске
         *  4. Игра близится к завершению
         */
    }

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @param isPrevNullMove был ли предыдущий ход нулевым ходом
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int nullMoveImpl(
            final boolean isMyMove,
            int alfa,
            final int beta,
            final int depth,
            boolean isPrevNullMove) {

        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);
        if (depth <= 0 || isTerminalNode(currNode))
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        /*------------ Null-Move ------------*/

        final boolean isAllowNullMove = isAllowNullMove(isMyMove, isPrevNullMove);
        if (isAllowNullMove) {
            isPrevNullMove = true;
            final Deque<Move> enemyMoves = getLegalMovesFor(!isMyMove);
            // Получаем лучший ход противника по какому-либо предположению:
            final Move enemyMove = enemyMoves.getFirst();
            // null-move:
            makeMove(enemyMove);
            final int value =
                    -nullMoveImpl(
                            isMyMove,
                            -beta,
                            -beta + 1,
                            depth - DEPTH_REDUCTION - 1,
                            isPrevNullMove);
            undoMove(enemyMove);
            if (beta <= value) return beta; // fail-high
        } else isPrevNullMove = false;

        /*------------ Дальше обычный PVS или любой другой алгоритм ------------*/

        final Iterator<Move> it = allMoves.iterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);
        int value = -nullMoveImpl(!isMyMove, -beta, -alfa, depth - 1, isPrevNullMove);
        if (alfa < value) alfa = value; // PV-node
        undoMove(move);

        while (it.hasNext()) {

            if (beta <= value) return beta; // fail-high
            // if (beta <= value) return value; // fail-soft

            move = it.next();
            makeMove(move);

            // null-window search:
            value = -nullMoveImpl(!isMyMove, -alfa - 1, -alfa, depth - 1, isPrevNullMove);

            // PVS
            if (alfa < value && value < beta) {
                value = -nullMoveImpl(!isMyMove, -beta, -alfa, depth - 1, isPrevNullMove);
            }

            undoMove(move);
            if (alfa < value) alfa = value; // PV-node
        }

        return alfa;
    }

    /*------------ Точка запуска алгоритма поиска ------------*/

    Move getNextBestMove() {
        final boolean isMyMove = true;
        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);

        if (isTerminalNode(currNode)) throw new RuntimeException("Ходов нет, игра окончена");

        int bestValue = MIN_VALUE;
        Move bestMove = allMoves.getFirst();

        for (final Move move : allMoves) {
            makeMove(move);
            final boolean isPrevNullMove = false;
            final int value =
                    -nullMoveImpl(!isMyMove, MIN_VALUE, MAX_VALUE, MAX_DEPTH, isPrevNullMove);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
