package patryk.game.MasterOfGame;

import patryk.game.MasterOfGame.Unit.Sword;
import patryk.game.MasterOfGame.Unit.Unit;

//nazwa klasy Field była już zajęta
public class Area
{
    public Unit unit = null;
    public Area setUnit(Integer c)
    {
        unit = new Sword(c);
        return this;
    }
}
