package patryk.game.MasterOfGame.Unit;

import javafx.util.Pair;

abstract public class Unit
{
    public Integer size;
    public Pair<Integer,Integer> moveTo = null;
    Unit(Integer s)
    {
        size = s;
    }
}
