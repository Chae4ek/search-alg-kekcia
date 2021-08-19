package chae4ek.engine;

import java.util.Collection;

public abstract class GameEngineStuff {

    /*------------ Функция оценки состояния игры ------------*/

    // +-1000 нужна, чтобы можно было добавлять глубину к худшей/лучшей оценки для следования к
    // лучшей ноде по кратчайшему пути
    public static final int MIN_VALUE = 1000 - Integer.MAX_VALUE;
    public static final int MAX_VALUE = Integer.MAX_VALUE - 1000;

    /** @return ВСЕГДА возвращает оценку доски для максимизирующего игрока */
    public abstract int evaluation(Board board, boolean color);

    /*------------ Методы логики игры ------------*/

    /** @return true, если нода терминальная */
    public abstract boolean isTerminalNode(GameNode node);

    /** Делает ход */
    public abstract void makeMove(Move move);

    /** Отменяет ход */
    public abstract void undoMove(Move move);

    /** @return все легальные ходы для игрока */
    public abstract <T extends Collection<Move>> T getLegalMovesFor(boolean color);

    /** @return true, если ход move - продвижение пешки */
    public abstract boolean isPromotingPawn(Move move);

    /*------------ Сущности игрового движка ------------*/

    /** Тип ноды */
    public enum NodeType {
        PV_NODE,
        CUT_NODE,
        ALL_NODE
    }

    /** Хранит состояние доски */
    public interface Board {
        /** @return чем выше число, тем значимее фигура */
        int getFigureOn(int position);
    }

    /** Хранит состояние игры */
    public interface GameNode {
        Board board = null;
    }

    /** Описывает игровой ход */
    public abstract static class Move {
        public int from; // хранит один из 64 квадратов на доске (для шахмат)
        public int to;

        public MoveType moveType;

        public enum MoveType {
            ATTACK_MOVE,
            QUIET_MOVE
        }
    }
}
