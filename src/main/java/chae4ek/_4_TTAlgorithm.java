package chae4ek;

import chae4ek.TranspositionTable.TTEntry;
import chae4ek.engine.GameEngineStuff;
import java.util.Deque;
import java.util.Iterator;

public abstract class _4_TTAlgorithm extends GameEngineStuff {

    /*------------ Настройки бота ------------*/

    private static final int MAX_DEPTH = 271828183;

    private GameNode currNode; // текущее состояние игры
    private boolean myColor; // цвет максимизирующего игрока

    private TranspositionTable<Deque<Move>> table;

    /*------------ Получение следующего хода у бота ------------*/

    /**
     * @param isMyMove true, если сейчас ход максимизирующего игрока
     * @param alfa гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     * @param beta гарантированная лучшая оценка для ТЕКУЩЕГО противника
     * @return гарантированная лучшая оценка для ТЕКУЩЕГО игрока
     */
    int pvsTT(final boolean isMyMove, int alfa, int beta, final int depth) {

        /*------------ Поиск в ТТ ------------*/

        final TTEntry<Deque<Move>> entry = table.find(currNode);
        if (entry != null && entry.depth >= depth) {
            if (entry.lowerBound >= beta) return entry.lowerBound;
            if (entry.upperBound <= alfa) return entry.upperBound;
            if (entry.lowerBound > alfa) alfa = entry.lowerBound;
            if (entry.upperBound < beta) beta = entry.upperBound;
        }
        final int alfaOrigin = alfa;
        final int betaOrigin = beta;

        /*------------ Получение ходов ------------*/

        final Deque<Move> allMoves;
        if (entry != null) allMoves = entry.allMoves;
        else allMoves = getLegalMovesFor(isMyMove);

        /*------------ Любой алгоритм поиска ------------*/

        if (depth <= 0 || isTerminalNode(currNode)) { // еще можно положить результат в ТТ
            return (isMyMove ? 1 : -1) * evaluation(currNode.board, myColor);
        }

        final Iterator<Move> it = allMoves.iterator();
        Move move = it.next();

        // Первый ход
        makeMove(move);
        int value = -pvsTT(!isMyMove, -beta, -alfa, depth - 1);
        if (alfa < value) alfa = value; // PV-node
        undoMove(move);

        while (it.hasNext()) {

            if (beta <= value) { // fail-high
                alfa = beta;
                break; // нужен break, чтобы занести результат в ТТ
            }

            move = it.next();
            makeMove(move);

            // null-window search:
            value = -pvsTT(!isMyMove, -alfa - 1, -alfa, depth - 1);

            // PVS
            if (alfa < value && value < beta) {
                value = -pvsTT(!isMyMove, -beta, -alfa, depth - 1);
            }

            undoMove(move);
            if (alfa < value) alfa = value; // PV-node
        }

        final int result = alfa;

        /*------------ Кеширование результата в ТТ ------------*/

        table.store(allMoves, result, currNode, alfaOrigin, betaOrigin, depth);

        return result;
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
            final int value = -pvsTT(!isMyMove, MIN_VALUE, MAX_VALUE, MAX_DEPTH);
            undoMove(move);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove;
    }
}
