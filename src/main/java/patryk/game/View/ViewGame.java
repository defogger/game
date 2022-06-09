package patryk.game.View;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Pair;
import patryk.game.MasterOfGame.Area;
import patryk.game.MasterOfGame.Map;
import patryk.game.MasterOfGame.MasterOfGame;
import patryk.game.MasterOfGame.Player;
import patryk.game.MasterOfGame.Unit.Unit;

import java.util.Random;
import java.util.Vector;

//klasa odpowiadająca za wyświetlanie przebiegu gry
public class ViewGame
{
    private Canvas canvas;
    private GraphicsContext gc;
    private MasterOfGame masterOfGame;
    private Double windowSizeX;
    private Double windowSizeY;
    private Integer radius = 50;
    private Pair<Double,Double> points[][];

    private Double x = 80.0;
    private Double y = 80.0;

    private Double MauseX;
    private Double MauseY;
    private Unit checkedUnit = null;
    static final double a = 2 * Math.PI / 6;
    public ViewGame()
    {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        windowSizeX = screenBounds.getHeight();
        windowSizeY = screenBounds.getWidth();

        canvas = new Canvas(windowSizeY, windowSizeX);
        gc = canvas.getGraphicsContext2D();

        addClickedEvent();

        masterOfGame = new MasterOfGame().createMap();
        points = new Pair[masterOfGame.getMap().getY()][masterOfGame.getMap().getX()];
        drawHexagonalGrid(masterOfGame.getMap().getX(),masterOfGame.getMap().getY());
    }

    public void reDraw()
    {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHexagonalGrid(masterOfGame.getMap().getX(),masterOfGame.getMap().getY());
    }

    private void addClickedEvent()
    {
        EventHandler<MouseEvent> mouseEvent = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Pair<Integer,Integer> clicedField = whichAreaCliced(e);
                if(e.getButton() != MouseButton.SECONDARY)
                {
                    if(masterOfGame.getMap().getArea(clicedField).isBattle)
                        return;
                    if(masterOfGame.getMap().getUnit(clicedField) != null )
                        if(masterOfGame.getMap().getUnit(clicedField).getOwner().equals(new Player("player")))
                            checkedUnit = masterOfGame.getMap().getUnit(clicedField);
                    for(Unit u : Map.enemy)
                    {
                        Random random = new Random();
                        u.setTarget(new Pair<>(Math.abs(random.nextInt())%15,Math.abs(random.nextInt())%15));
                    }
                }
                else if(e.getButton() == MouseButton.SECONDARY)
                    if(checkedUnit != null)
                        checkedUnit.setTarget(clicedField);
            }
        };
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,mouseEvent);

        EventHandler<MouseEvent> mousePressed = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(e.getButton() != MouseButton.SECONDARY)
                {
                    MauseX = e.getX();
                    MauseY = e.getY();
                }
            }
        };
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED,mousePressed);

        EventHandler<MouseEvent> mouseDragged = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if(e.getButton() != MouseButton.SECONDARY)
                {
                    if(x - MauseX + e.getX() > -1000 && x - MauseX + e.getX() < 200)
                        x -= MauseX - e.getX();
                    if(y - MauseY + e.getY() > -1000 && y - MauseY + e.getY() < 200)
                        y -= MauseY - e.getY();
                    MauseX = e.getX();
                    MauseY = e.getY();
                }
            }
        };
        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED,mouseDragged);
    }

    private Pair<Integer,Integer> whichAreaCliced(MouseEvent e)
    {
        Double distance = distance(points[0][0],new Pair<>(e.getY(),e.getX()));
        Pair<Integer,Integer> clicedField = new Pair(0,0);
        for(int i=0;i<masterOfGame.getMap().getY();i++)
        {
            for(int j=0;j<masterOfGame.getMap().getX();j++)
            {
                if(distance > distance(points[i][j],new Pair<>(e.getY(),e.getX())))
                {
                    distance = distance(points[i][j],new Pair<>(e.getY(),e.getX()));
                    clicedField = new Pair(i,j);
                }
            }
        }

        return clicedField;
    }

    private double distance(Pair<Double,Double> a,Pair<Double,Double> b)
    {
        Double x = Math.abs(a.getValue() - b.getValue());
        Double y = Math.abs(a.getKey() - b.getKey());
        return Math.sqrt(x*x+y*y);
    }

    public void drawHexagonalGrid(Integer sizeX, Integer sizeY)
    {

        boolean f = true;
        for(int i=0;i<sizeY;i++)
            if(f)
            {
                for(int j=0;j<sizeX;j++)
                    points[i][j] = new Pair<>(y+i*(radius * (1+Math.cos(a))),x+j*2*radius*Math.sin(a));
                f = !f;
            }
            else
            {
                for(int j=0;j<sizeX;j++)
                    points[i][j] = new Pair<>(y+i*(radius * (1+Math.cos(a))),x+(j*2+1)*radius*Math.sin(a));
                f = !f;
            }

        for(int i=0;i<sizeY;i++)
            for(int j=0;j<sizeX;j++)
                drawArea(new Pair(i,j));
    }

    private void drawArea(Pair<Integer,Integer> positionOfArea)
    {
        drawHexagon(positionOfArea);

        drawUnit(positionOfArea);
    }

    private void drawUnit(Pair<Integer,Integer> positionOfArea)
    {
        Pair<Double,Double> centerOfArea = points[positionOfArea.getKey()][positionOfArea.getValue()];
        Unit unit = masterOfGame.getMap().getUnit(positionOfArea);

        Double imgsize = radius*1.0;

        if(masterOfGame.getMap().getSecondUnit(positionOfArea) != null)
        {
            //funkcja rysuje dwie walczące jednostki
            Unit secondUnit = masterOfGame.getMap().getSecondUnit(positionOfArea);

            //rysuje pierwszą jednostke
            gc.drawImage(unit.getImg(imgsize),centerOfArea.getValue()-imgsize/2 + radius * 0.35,centerOfArea.getKey()-imgsize/2);

            //rysuje drugą jednostke
            gc.drawImage(secondUnit.getImg(imgsize),centerOfArea.getValue()-imgsize/2 - radius * 0.35,centerOfArea.getKey()-imgsize/2);

            gc.setFill(Color.BLACK);
            gc.fillText(unit.getSize().toString(), centerOfArea.getValue() + (radius*0.1), centerOfArea.getKey()+(radius*2/3));
            gc.fillText(secondUnit.getSize().toString(), centerOfArea.getValue() - (radius*0.5), centerOfArea.getKey()+(radius*2/3));

            gc.setFill(Color.RED);
            gc.fillText("- " + unit.getLastHit().toString(), centerOfArea.getValue() + (radius*0.2), centerOfArea.getKey());
            gc.fillText("- " + secondUnit.getLastHit().toString(), centerOfArea.getValue() - (radius*0.6), centerOfArea.getKey());

            drawFlag(unit,centerOfArea,radius * 0.2);
            drawFlag(secondUnit,centerOfArea,radius * -0.2);
        }
        else if(unit != null)
        {
            gc.drawImage(unit.getImg(imgsize),centerOfArea.getValue()-imgsize/2,centerOfArea.getKey()-imgsize/2);
            gc.setFill(Color.BLACK);
            if(unit.equals(checkedUnit))
                gc.strokeOval(centerOfArea.getValue()-imgsize/2,centerOfArea.getKey()-imgsize/2,imgsize-1,imgsize-1);
            gc.fillText(unit.getSize().toString(), centerOfArea.getValue()-(radius*0.2), centerOfArea.getKey()+(radius*2/3));

            drawFlag(unit,centerOfArea);
            drawVector(positionOfArea,masterOfGame.getUnitMoveTo(positionOfArea));
        }
    }

    private void drawFlag(Unit unit,Pair<Double,Double> centerOfArea)
    {
        //rysuje flagę przynależności
        //zielone gracz
        //czerwone przeciwnik
        gc.beginPath();
        gc.rect(centerOfArea.getValue() - radius*0.1,centerOfArea.getKey()-radius*0.5,radius*0.2,radius*0.2);
        if(unit.getOwner().equals(masterOfGame.getPlayer()))
            gc.setFill(Color.BLUE);
        else
            gc.setFill(Color.RED);
        gc.fill();
    }

    private void drawFlag(Unit unit,Pair<Double,Double> centerOfArea,Double x)
    {
        //rysuje flagę przynależności
        //zielone gracz
        //czerwone przeciwnik
        gc.beginPath();
        gc.rect(centerOfArea.getValue() - radius*0.1 + x,centerOfArea.getKey()-radius*0.5,radius*0.2,radius*0.2);
        if(unit.getOwner().equals(masterOfGame.getPlayer()))
            gc.setFill(Color.BLUE);
        else
            gc.setFill(Color.RED);
        gc.fill();
    }

    private void drawHexagon(Pair<Integer,Integer> positionOfArea)
    {
        Pair<Double,Double> centerOfArea = points[positionOfArea.getKey()][positionOfArea.getValue()];
        Double x = centerOfArea.getValue(),y = centerOfArea.getKey();
        Double newX,newY;
        y-=radius;

        double x1[] = new double[6];
        double y1[] = new double[6];

        for(int i=-1;i<5;i++)
        {
            newX = x + radius * Math.sin(a*i);
            newY = y + radius * Math.cos(a*i);
            gc.strokeLine(x, y, newX, newY);
            x = newX;
            y = newY;
            x1[i+1] = x;
            y1[i+1] = y;
        }

        if(masterOfGame.getMap().getArea(positionOfArea).getType().equals(Area.Type.MEADOW))
            gc.setFill(Color.PALEGREEN);
        else if(masterOfGame.getMap().getArea(positionOfArea).getType().equals(Area.Type.HILL))
            gc.setFill(Color.GOLD);
        else
            gc.setFill(Color.PERU);
        gc.fillPolygon(x1, y1, 6);
    }

    private void drawVector(Pair<Integer,Integer> from,Pair<Integer,Integer> to)
    {
        //rysuje wektor przemieszczenia się jednostki
        if(to == null)
            return;
        Pair<Double,Double> f = points[from.getKey()][from.getValue()];
        Pair<Double,Double> t = points[to.getKey()][to.getValue()];
        Double x = f.getValue() + (( t.getValue() - f.getValue() ) * 0.8);
        Double y = f.getKey() + (( t.getKey() - f.getKey() ) * 0.8);
        gc.strokeLine(f.getValue(),f.getKey(),x,y);
    }

    public Canvas getCanvas() {
        return canvas;
    }
}
