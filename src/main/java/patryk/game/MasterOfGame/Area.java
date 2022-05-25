package patryk.game.MasterOfGame;

import javafx.util.Pair;
import patryk.game.MasterOfGame.Unit.Sword;
import patryk.game.MasterOfGame.Unit.Unit;

import java.util.Vector;

//nazwa klasy Field była już zajęta
public class Area
{
    Pair<Integer,Integer> position;
    Unit firstUnit = null;
    Unit secondUnit = null;
    Thread thredBattle;
    public boolean isBattle = false;
    Map map;
    public Area(Map map,Pair<Integer,Integer> position)
    {
        this.map = map;
        this.position = position;
    }
    public Area setUnit(Integer c)
    {
        //do poprawy
        firstUnit = new Sword(map,new Player("player"),position,c);
        return this;
    }

    public Unit getFirstUnit()
    {
        return firstUnit;
    }

    public Unit getSecondUnit()
    {
        return secondUnit;
    }

    public void startBattle()
    {
        thredBattle = new Thread(()->{
            synchronized (map)
            {
                isBattle = true;
            }
            while (secondUnit != null)
            {
                //pierwsza jednostka udarza pierwsza
                if(secondUnit == null || firstUnit == null)
                    break;
                synchronized (map)
                {
                    if( secondUnit.addDamage( firstUnit.countDamage() ) )
                        secondUnit = null;
                    else if( firstUnit.addDamage( secondUnit.countDamage() ) )
                    {
                        firstUnit = secondUnit;
                        secondUnit = null;
                    }
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception e){}

            }
            synchronized (map)
            {
                isBattle = false;
            }
        });
        thredBattle.start();
    }

    public Vector<Area> getAdjacentAreas()
    {
        Vector<Area> out = new Vector<>();
        Pair<Integer,Integer> pair;
        pair = new Pair<>(position.getKey()+1,position.getValue());
        if(map.getArea(pair) != null)
            out.add(map.getArea(pair));
        pair = new Pair<>(position.getKey()-1,position.getValue());
        if(map.getArea(pair) != null)
            out.add(map.getArea(pair));
        pair = new Pair<>(position.getKey(),position.getValue()+1);
        if(map.getArea(pair) != null)
            out.add(map.getArea(pair));
        pair = new Pair<>(position.getKey(),position.getValue()-1);
        if(map.getArea(pair) != null)
            out.add(map.getArea(pair));

        if(position.getKey() % 2 == 1)
        {
            pair = new Pair<>(position.getKey()+1,position.getValue()+1);
            if(map.getArea(pair) != null)
                out.add(map.getArea(pair));
            pair = new Pair<>(position.getKey()-1,position.getValue()+1);
            if(map.getArea(pair) != null)
                out.add(map.getArea(pair));
        }
        else
        {
            pair = new Pair<>(position.getKey()+1,position.getValue()-1);
            if(map.getArea(pair) != null)
                out.add(map.getArea(pair));
            pair = new Pair<>(position.getKey()-1,position.getValue()-1);
            if(map.getArea(pair) != null)
                out.add(map.getArea(pair));
        }

        return out;
    }
}
