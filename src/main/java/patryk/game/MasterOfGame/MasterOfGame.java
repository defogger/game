package patryk.game.MasterOfGame;

import javafx.util.Pair;
import patryk.game.MasterOfGame.Unit.Unit;

import java.util.Vector;

//klasa odpowiadająca za przebieg gry
public class MasterOfGame
{
    Map map;
    //do poprawy public
    public Player eneny;
    //do poprawy public
    public Player player;
    public MasterOfGame()
    {
        eneny = new Player("ai");
        player = new Player("player");
    }
    public MasterOfGame createMap()
    {
        map = new Map(16,16);
        return this;
    }

    public Map getMap()
    {
        return map;
    }

    public Player getPlayer()
    {
        return player;
    }

    public Pair<Integer,Integer> getUnitMoveTo(Pair<Integer,Integer> positionOfArea)
    {
        //zwraca punkt, do którego zmierza jednostka
        Unit unit = map.getUnit(positionOfArea);
        if(unit == null)
            return null;
        return unit.getMoveTo();
    }
}
