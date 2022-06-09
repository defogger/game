package patryk.game.MasterOfGame;

import javafx.util.Pair;
import patryk.game.MasterOfGame.Unit.Unit;

import java.util.Random;
import java.util.Vector;

//nazwa klasy Field była już zajęta
public class Area
{
    Pair<Integer,Integer> position;
    public enum Type {
        MEADOW,HILL,MOUNTAIN;
    }
    Type type;
    Double distance;
    Unit firstUnit = null;
    Unit secondUnit = null;
    Thread thredBattle;
    public boolean isBattle = false;
    Map map;
    public Area(Map map,Pair<Integer,Integer> position)
    {
        this.map = map;
        this.position = position;
        randomUnit();
        randomType();
        setDistance();
    }

    private void randomUnit()
    {
        Player player;
        if(position.getValue() < 5 && position.getKey() < 5)
            player = new Player("player");
        else if(position.getValue() > 10 && position.getKey() > 10)
            player = new Player("ai");
        else
            return;
        Random random = new Random();
        int i = random.nextInt();
        i %= 10;
        if(i == 0)
            setUnit(player,Unit.Type.SWORDSMAN,100);
        else if (i == 1)
            setUnit(player,Unit.Type.SPEARMAN,100);
        else if (i == 2)
            setUnit(player,Unit.Type.CAVALRY,100);
    }

    private void randomType()
    {
        Random random = new Random();
        int i = random.nextInt();
        i %= 3;
        if(i == 0)
            type = Type.MEADOW;
        else if (i == 1)
            type = Type.HILL;
        else
            type = Type.MOUNTAIN;
    }

    private void setDistance()
    {
        if(type.equals(Type.MEADOW))
            distance = 0.8;
        else if(type.equals(Type.HILL))
            distance = 1.0;
        else if(type.equals(Type.MOUNTAIN))
            distance = 1.2;
    }

    public Double getDistance()
    {
        return distance;
    }

    public Area setUnit(Player p,Unit.Type t,Integer c)
    {
        firstUnit = new Unit(map,p,position, t,c);

        if(p.equals(new Player("ai")))
            map.enemy.add(firstUnit);

        return this;
    }
    public Type getType()
    {
        return type;
    }
    public Pair<Integer,Integer> getPosition()
    {
        return position;
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
                if(secondUnit == null || firstUnit == null)
                    break;
                synchronized (map)
                {
                    if( firstUnit.addDamage( secondUnit.countDamage() ) )
                    {
                        firstUnit = secondUnit;
                        secondUnit = null;
                    }
                    else if( secondUnit.addDamage( firstUnit.countDamage() ) )
                        secondUnit = null;
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
