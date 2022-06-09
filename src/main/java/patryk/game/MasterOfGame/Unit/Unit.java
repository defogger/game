package patryk.game.MasterOfGame.Unit;

import javafx.scene.image.Image;
import javafx.util.Pair;
import patryk.game.MasterOfGame.Area;
import patryk.game.MasterOfGame.Map;
import patryk.game.MasterOfGame.Player;

import java.io.File;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Unit
{
    static File swordsmanFile = new File("img/sword.png");
    static File cavalryFile = new File("img/horse.png");
    static File spearmanFile = new File("img/spear.png");

    public enum Type {
        SWORDSMAN, CAVALRY, SPEARMAN;
    }
    Type type;
    Double size;
    final Map map;
    //do poprawy public
    public Player owner;
    Pair<Integer,Integer> position;
    Pair<Integer,Integer> moveTo;
    Pair<Integer,Integer> target;
    public Thread threadMoveTo;

    Integer pointsOfdamage;
    Integer pointsOfLife;
    Double speed;
    Integer lastHit;
    Double multiplier;

    public Unit(Map map,Player owner, Pair<Integer,Integer> position,Type t, Integer s)
    {
        this.map = map;
        this.type = t;
        this.owner = owner;
        this.position = position;
        size = Double.valueOf(s);
        setParameters();
    }

    private void setParameters()
    {
        if(type.equals(Type.SWORDSMAN))
        {
            pointsOfdamage = 10;
            pointsOfLife = 250;
            speed = 1.0;
        }
        else if(type.equals(Type.CAVALRY))
        {
            pointsOfdamage = 20;
            pointsOfLife = 500;
            speed = 1.8;
        }
        else if(type.equals(Type.SPEARMAN))
        {
            pointsOfdamage = 15;
            pointsOfLife = 200;
            speed = 0.9;
        }
    }
    public Image getImg(Double imgSize)
    {
        if(type.equals(Type.SWORDSMAN))
            return new Image(swordsmanFile.toURI().toString(),imgSize,imgSize,false,false);
        else if(type.equals(Type.CAVALRY))
            return new Image(cavalryFile.toURI().toString(),imgSize,imgSize,false,false);
        else if(type.equals(Type.SPEARMAN))
            return new Image(spearmanFile.toURI().toString(),imgSize,imgSize,false,false);
        return null;
    }
    public Integer getSize()
    {
        return (int) Math.ceil(size);
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
                if(map.getArea(position).isBattle)
                    break;
                synchronized (map)
                {
                    //moveTo = nextAreaToMove();
                    moveTo = nextAreaToMove2();
                }
                try
                {
                    Double time = map.getArea(moveTo).getDistance() * 1000/speed;
                    Thread.sleep(time.intValue());
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
        //funkcja wyznacza pierwszy ruch, który umożliwia przejść od position from do pola target
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
            System.out.println("przyjazna jednostka stoi na drodze");
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

    private Pair<Integer,Integer> nextAreaToMove2()
    {
        //funkcja wyznacza pierwszy ruch, który umożliwia przejść od pola position do pola target
        //Algorytm Dijkstry

        class P implements Comparable
        {
            Pair<Integer,Integer> point;
            Double distance;
            P (Pair<Integer,Integer> point,Double distance)
            {
                this.point = point;
                this.distance = distance;
            }
            @Override
            public int compareTo(Object o) {
                if(o instanceof P && o != null)
                    if(this.distance < ((P) o).distance)
                        return 1;
                    else
                        return -1;
                return 0;
            }
        }

        HashMap<Pair<Integer,Integer>,Double> d = new HashMap<>();
        for(int i=0;i<map.getY();i++)
            for(int j=0;j< map.getX();j++)
                if(position.getKey() == i && position.getValue() == j)
                    d.put(position,0.0);
                else
                    d.put(new Pair<>(i,j),Double.MAX_VALUE);

        PriorityQueue<P> q = new PriorityQueue<>();

        for(int i=0;i<map.getY();i++)
            for(int j=0;j< map.getX();j++)
                q.add(new P(new Pair<>(i,j),d.get(new Pair<>(i,j))));

        while (!q.isEmpty())
        {
            P u = q.poll();
            //dla karzdego sąsiada v wierzchołka u sprawdzam czy da się
            //z v dojść do u szybciej niż dotychczas
            for(Area a : map.getArea(u.point).getAdjacentAreas())
            {
                if(a.getFirstUnit() != null)
                    //tego pola nie da się osiągnąć
                    continue;
                if( d.get(u.point) + a.getDistance() < d.get(a.getPosition()) )
                {
                    d.replace(a.getPosition(),d.get(u.point) + a.getDistance());
                    q.add(new P(a.getPosition(),d.get(u.point) + a.getDistance()));
                }
            }
        }

        Pair<Integer,Integer> stepEarlier = target;
        Pair<Integer,Integer> nowImOn = target;
        //szukam jak dotrzeć to punktu target
        while (!nowImOn.equals(position))
        {
            Double min = Double.MAX_VALUE;
            Pair<Integer,Integer> nextStep = null;
            for(Area a : map.getArea(nowImOn).getAdjacentAreas())
            {
                if(d.get(a.getPosition()) < min)
                {
                    min = d.get(a.getPosition());
                    nextStep = a.getPosition();
                }
            }
            if(!stepEarlier.equals(nowImOn))
                stepEarlier = nowImOn;
            nowImOn = nextStep;

            if(nowImOn == null)
            {
                System.out.println("nie da się dotrzeć na to pole");
                target = position;
                threadMoveTo.stop();
                break;
            }
        }

        return stepEarlier;
    }

    public boolean areFriendlyUnits(Unit secend)
    {
        return owner.equals(secend.owner);
    }

    public Double countDamage()
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
        if(this == map.getArea(position).getFirstUnit())
            if(map.getArea(position).getType() == Area.Type.HILL)
                multiplier += 0.1;
            else if(map.getArea(position).getType() == Area.Type.MOUNTAIN)
                multiplier += 0.2;
        multiplier++;
        this.multiplier = multiplier;
        Double out = size * pointsOfdamage * multiplier;
        return out;
    }

    public boolean addDamage(Double damage)
    {
        //funkcja zwraca true gdy jednostka została zniszczona
        Double result = damage/pointsOfLife;
        lastHit =  (int) Math.ceil(size) - (int) Math.ceil(size - result);
        size -= result;
        if(size <= 0)
            return true;
        return false;
    }

    public Integer getLastHit()
    {
        return lastHit;
    }
}
