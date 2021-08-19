package chae4ek;

import chae4ek.engine.GameEngineStuff;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public abstract class _7_AspirationSearch_Quiescence extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    /** Обычно берут стоимость пешки, пол пешки или четверть пешки */
    private static final int WINDOW_SIZE = 25;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    /*------------ Получение следующего хода у бота ------------*/

    /** Расширенный поиск. Можно использовать везде, где нужен алгоритм с первым предположением */
    int aspirationSearch(
            final boolean isMyMove,
            final int firstGuess,
            final int alfa,
            final int beta,
            final int depth) {
        int value =
                pvsWithQuiescence(
                        isMyMove, firstGuess - WINDOW_SIZE, firstGuess + WINDOW_SIZE, depth);
        if (beta <= value) // fail-high
        value = pvsWithQuiescence(isMyMove, value, MAX_VALUE, depth);
        else if (value <= alfa) // fail-low
        value = pvsWithQuiescence(isMyMove, MIN_VALUE, value, depth);
        return value;
    }

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int pvsWithQuiescence(final boolean isMyMove, int alfa, final int beta, final int depth) {

        final Deque<Move> allMoves = getLegalMovesFor(isMyMove);
        if (depth <= 0 || isTerminalNode(currNode))
            // return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);
            return quiesce(isMyMove, alfa, beta);

        final Iterator<Move> it = allMoves.iterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);
        int value = -pvsWithQuiescence(!isMyMove, -beta, -alfa, depth - 1);
        if (alfa < value) alfa = value; // PV-node
        undoMove(move);

        while (it.hasNext()) {

            if (beta <= value) return beta; // fail-high

            move = it.next();
            makeMove(move);

            // null-window search:
            value = -pvsWithQuiescence(!isMyMove, -alfa - 1, -alfa, depth - 1);

            // PVS
            if (alfa < value && value < beta) {
                value = -aspirationSearch(!isMyMove, value, -beta, -alfa, depth - 1);
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
            final int value = -aspirationSearch(!isMyMove, 0, MIN_VALUE, MAX_VALUE, MAX_DEPTH);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /*------------ Quiescence ------------*/

    /**
     * Симулирует все атакующие ходы и считает оценку доски
     *
     * @return лучшая оценка доски для текущего игрока
     */
    private int quiesce(final boolean isMyMove, int alfa, final int beta) {

        // Тут можно (нужно) подключить ТТ...

        final List<Move> allMoves = getLegalMovesFor(isMyMove);

        final int standPat = (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);

        /*------------ Условие выхода из рекурсии ------------*/

        if (standPat >= beta) return beta;
        if (alfa < standPat) alfa = standPat;
        if (isTerminalNode(currNode)) return alfa;

        /*------------ Проведение взятий до потери пульса ------------*/

        final Iterator<Move> attackMoves = MoveSorter.MVV_LVA_filter(currNode.board, allMoves);

        while (attackMoves.hasNext()) {
            final Move move = attackMoves.next();

            /*------------ Delta Pruning ------------*/
            int BIG_DELTA;
            {
                // Прежде чем мы сделаем захват, мы проверяем, достаточно ли значения захваченной
                // части
                // плюс некоторый запас прочности, т.е. нет смысла захватывать фигуру, если позиция
                // не
                // будет иметь достаточную компенсацию для этого хода

                // ДОЛЖНО быть отключено в эндшпилях
                BIG_DELTA = 900; // цена ферзя
                if (isPromotingPawn(move)) BIG_DELTA += 700; // цена ферзя минус 2 пешки
            }

            makeMove(move);
            final int score = -quiesce(!isMyMove, -beta, -alfa);
            undoMove(move);

            /*------------ Delta Pruning ------------*/
            {
                // Если захват не стоит потери позиции
                // ДОЛЖНО быть отключено в эндшпилях
                if (score < alfa - BIG_DELTA) return alfa;
            }

            if (beta <= score) return beta; // fail-high
            if (alfa < score) alfa = score; // PV-node
        }

        return alfa;
    }
}
