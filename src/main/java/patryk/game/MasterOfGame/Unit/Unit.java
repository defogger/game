package patryk.game.MasterOfGame.Unit;

import javafx.util.Pair;
import patryk.game.MasterOfGame.Area;
import patryk.game.MasterOfGame.Map;
import patryk.game.MasterOfGame.Player;

abstract public class Unit
{
    Integer size;
    final Map map;
    //do poprawy public
    public Player owner;
    Pair<Integer,Integer> position;
    Pair<Integer,Integer> moveTo;
    Pair<Integer,Integer> target;
    public Thread threadMoveTo;

    Integer pointsOfdamage = 10;
    Integer pointsOfLife = 250;
    Integer lastHit;

    Unit(Map map,Player owner, Pair<Integer,Integer> position, Integer s)
    {
        this.map = map;
        this.owner = owner;
        this.position = position;
        size = s;
    }

    public Integer getSize()
    {
        return size;
    }

    public Player getOwner()
    {
        return owner;
    }
    public Pair<Integer,Integer> getPosition()
    {
        return position;
    }
    public void setPosition(Pair<Integer,Integer> position)
    {
        this.position = position;
    }
    public void setTarget(Pair<Integer,Integer> target)
    {
        endMoveThread();
        this.target = target;
        threadMoveTo = new Thread(()->{
            //dopóki cel nie został osiągnięty
            while ( !position.equals(this.target) )
            {
                synchronized (map)
                {
                    moveTo = nextAreaToMove();
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception exception){}
                map.moveUnit(this,moveTo);
            }
            endMoveThread();
        });
        threadMoveTo.start();
    }

    public void endMoveThread()
    {
        synchronized (map)
        {
            if(threadMoveTo != null)
            {
                target = null;
                moveTo = null;
                threadMoveTo.stop();
                System.out.println("thread end");
                threadMoveTo = null;
            }
        }
    }

    public Pair<Integer,Integer> getMoveTo()
    {
        return moveTo;
    }

    private Pair<Integer,Integer> nextAreaToMove()
    {
        //funkcja wyznacza pierwszy ruch, który umożliwia przejść od pola from do pola to
        Integer y,x;
        y = position.getKey();
        x = position.getValue();
        boolean yChenge = false;
        if(position.getKey() < target.getKey())
        {
            yChenge = true;
            y++;
        }
        else if(position.getKey() > target.getKey())
        {
            yChenge = true;
            y--;
        }
        //sprawdzam parzystość pola
        //jeśli pole jest parzyste to
        //współżędną y moge zmniejszyć lub zostawić
        //jeśli pole jest nie parzyste to
        //współżędną y moge zwiększyć lub zostawić
        if(yChenge)
        {
            if(position.getKey() % 2 == 0)
            {
                if(position.getValue() > target.getValue())
                    x--;
            }
            else
            if(position.getValue() < target.getValue())
                x++;
        }
        else
            if(position.getValue() > target.getValue())
                x--;
            else if(position.getValue() < target.getValue())
                x++;

        Unit unitOnSecondArea = map.getUnit(new Pair<>(y,x));

        //do poprawy
        if(unitOnSecondArea != null && this.areFriendlyUnits(unitOnSecondArea))
        {
            System.out.println("pzryjazna jednostka stoi na drodze");
            target = position;
            return  position;
        }

        Unit secondUnitOnSecondArea = map.getSecondUnit(new Pair<>(y,x));

        if(secondUnitOnSecondArea != null)
        {
            System.out.println("jakaś jednostka już atakuje to pole");
            target = position;
            return  position;
        }
        return new Pair<>(y,x);
    }

    public boolean areFriendlyUnits(Unit secend)
    {
        return owner.equals(secend.owner);
    }

    public Integer countDamage()
    {
        Double multiplier = 0.0;
        for(Area i : map.getArea(position).getAdjacentAreas())
        {
            if(i.getFirstUnit() != null && i.getFirstUnit().areFriendlyUnits(this))
            {
                multiplier++;
                if(i.getSecondUnit() != null && i.getSecondUnit().areFriendlyUnits(this))
                    multiplier++;
            }
        }
        multiplier *= 0.25;
        multiplier++;
        System.out.println(multiplier);
        Double out = size * pointsOfdamage * multiplier;
        return out.intValue();
    }

    public boolean addDamage(Integer damage)
    {
        //funkcja zwraca true gdy jednostka została zniszczona
        Double result = Double.valueOf(damage);
        result = Math.ceil( result/pointsOfLife );
        lastHit = result.intValue();
        size -= result.intValue();
        if(size <= 0)
            return true;
        return false;
    }

    public Integer getLastHit()
    {
        return lastHit;
    }
}
