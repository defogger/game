package patryk.game.MasterOfGame;

import javafx.util.Pair;
import patryk.game.MasterOfGame.Unit.Unit;

public class Map
{
    private Integer sizeX;
    private Integer sizeY;
    public Area[][] areas;
    Map (Integer x,Integer y)
    {
        this.sizeX = x;
        this.sizeY = y;
        areas = new Area[sizeY][sizeX];

        for(int i=0;i<sizeY;i++)
            for(int j=0;j<sizeX;j++)
                areas[i][j] = new Area(this,new Pair<>(i,j));
    }
    public Integer getX() {
        return sizeX;
    }
    public Integer getY() {
        return sizeY;
    }

    public Unit getUnit(Pair<Integer,Integer> positionOfArea)
    {
        return areas[positionOfArea.getKey()][positionOfArea.getValue()].firstUnit;
    }

    public Area getArea(Pair<Integer,Integer> positionOfArea)
    {
        if(positionOfArea.getKey() < 0 || positionOfArea.getKey() >= sizeY)
            return null;
        if(positionOfArea.getValue() < 0 || positionOfArea.getValue() >= sizeX)
            return null;
        return areas[positionOfArea.getKey()][positionOfArea.getValue()];
    }

    public Unit getSecondUnit(Pair<Integer,Integer> positionOfArea)
    {
        return areas[positionOfArea.getKey()][positionOfArea.getValue()].secondUnit;
    }
    synchronized public void moveUnit(Unit unit, Pair<Integer,Integer> moveTo)
    {
        Pair<Integer,Integer> goFrom = unit.getPosition();
        if(goFrom.equals(moveTo))
            return;
        if(getUnit(goFrom) == null)
        {
            //błąd
            return;
        }

        Unit unitOnSecondArea = getUnit(moveTo);
        Unit secondUnitOnSecondArea = getSecondUnit(moveTo);;

        if(unitOnSecondArea == null)
        {
            unit.setPosition(moveTo);
            areas[moveTo.getKey()][moveTo.getValue()].firstUnit = areas[goFrom.getKey()][goFrom.getValue()].firstUnit;
            areas[goFrom.getKey()][goFrom.getValue()].firstUnit = null;
            return;
        }
        else if(unitOnSecondArea != null && unitOnSecondArea.areFriendlyUnits(unit))
        {
            System.out.println("Inna przyjazna jednostka zajeła to pole");
            return;
        }
        else if(secondUnitOnSecondArea == null)
        {
            unit.setPosition(moveTo);
            areas[moveTo.getKey()][moveTo.getValue()].secondUnit = areas[goFrom.getKey()][goFrom.getValue()].firstUnit;
            areas[goFrom.getKey()][goFrom.getValue()].firstUnit = null;
            areas[moveTo.getKey()][moveTo.getValue()].startBattle();
            unitOnSecondArea.endMoveThread();
            unit.endMoveThread();
            return;
        }
        System.out.println("jakaś jednostka już atakuje to pole");
        return;
    }
}
