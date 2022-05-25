package patryk.game.View;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Pair;
import patryk.game.MasterOfGame.MasterOfGame;
import patryk.game.MasterOfGame.Unit.Unit;
import java.io.File;

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

    private Double x = 100.0;
    private Double y = 100.0;
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
                System.out.println(clicedField.getKey()+" "+clicedField.getValue());
                if(e.getButton() != MouseButton.SECONDARY)
                {
                    if(checkedUnit == null)
                    {
                        if(masterOfGame.getMap().getArea(clicedField).isBattle)
                            return;
                        if(masterOfGame.getMap().getUnit(clicedField) != null)
                            checkedUnit = masterOfGame.getMap().getUnit(clicedField);
                        System.out.println("zaznaczono");
                        return;
                    }
                    checkedUnit.setTarget(clicedField);
                    checkedUnit = null;
                }
                if(e.getButton() == MouseButton.SECONDARY)
                {
                    //funkcja tymczasowo dodaje jednostke po kliknięci prawym przyciskiem
                    if(masterOfGame.getMap().getUnit(clicedField) == null)
                        masterOfGame.getMap().areas[clicedField.getKey()][clicedField.getValue()].setUnit(100);
                    else
                        if(masterOfGame.getMap().getUnit(clicedField).owner.equals(masterOfGame.eneny))
                            masterOfGame.getMap().getUnit(clicedField).owner = masterOfGame.player;
                        else
                            masterOfGame.getMap().getUnit(clicedField).owner = masterOfGame.eneny;
                }
            }
        };
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,mouseEvent);

        //do poprawy
        EventHandler<KeyEvent> keyEvent = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                System.out.println(keyEvent.getCode());
            }
        };

        canvas.addEventFilter(KeyEvent.KEY_TYPED,keyEvent);
        canvas.setVisible(true);

        EventHandler<ScrollEvent> scrollEvent = new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                Integer delta = (int) scrollEvent.getDeltaY();
                //do poprawy
                /*if(radius + delta > 20 && radius + delta < 100)
                    radius += delta;*/
            }
        };
        canvas.addEventFilter(ScrollEvent.SCROLL,scrollEvent);
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
            String imgUrl = "sword";
            File file = new File("img/"+imgUrl+".jpg");
            Image img = new Image(file.toURI().toString(),imgsize,imgsize,false,false);
            gc.drawImage(img,centerOfArea.getValue()-imgsize/2 + radius * 0.35,centerOfArea.getKey()-imgsize/2);

            //rysuje drugą jednostke
            imgUrl = "sword";
            file = new File("img/"+imgUrl+".jpg");
            img = new Image(file.toURI().toString(),imgsize,imgsize,false,false);
            gc.drawImage(img,centerOfArea.getValue()-imgsize/2 - radius * 0.35,centerOfArea.getKey()-imgsize/2);

            gc.fillText(unit.getSize().toString(), centerOfArea.getValue() + (radius*0.1), centerOfArea.getKey()+(radius*2/3));
            gc.fillText(secondUnit.getSize().toString(), centerOfArea.getValue() - (radius*0.5), centerOfArea.getKey()+(radius*2/3));

            gc.setFill(Color.RED);
            gc.fillText("- " + unit.getLastHit().toString(), centerOfArea.getValue() + (radius*0.2), centerOfArea.getKey());
            gc.fillText("- " + secondUnit.getLastHit().toString(), centerOfArea.getValue() - (radius*0.6), centerOfArea.getKey());
            gc.setFill(Color.BLACK);

            drawFlag(unit,centerOfArea,radius * 0.2);
            drawFlag(secondUnit,centerOfArea,radius * -0.2);
        }
        else if(unit != null)
        {
            //do poprawy
            String imgUrl = "sword";
            File file = new File("img/"+imgUrl+".jpg");
            Image img = new Image(file.toURI().toString(),imgsize,imgsize,false,false);
            gc.drawImage(img,centerOfArea.getValue()-imgsize/2,centerOfArea.getKey()-imgsize/2);
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
            gc.setFill(Color.LIGHTGREEN);
        else
            gc.setFill(Color.RED);
        gc.fill();
        gc.setFill(Color.BLACK);
    }

    private void drawFlag(Unit unit,Pair<Double,Double> centerOfArea,Double x)
    {
        //rysuje flagę przynależności
        //zielone gracz
        //czerwone przeciwnik
        gc.beginPath();
        gc.rect(centerOfArea.getValue() - radius*0.1 + x,centerOfArea.getKey()-radius*0.5,radius*0.2,radius*0.2);
        if(unit.getOwner().equals(masterOfGame.getPlayer()))
            gc.setFill(Color.LIGHTGREEN);
        else
            gc.setFill(Color.RED);
        gc.fill();
        gc.setFill(Color.BLACK);
    }

    private void drawHexagon(Pair<Integer,Integer> positionOfArea)
    {
        Pair<Double,Double> centerOfArea = points[positionOfArea.getKey()][positionOfArea.getValue()];
        Double x = centerOfArea.getValue(),y = centerOfArea.getKey();
        Double newX,newY;
        y-=radius;

        for(int i=-1;i<5;i++)
        {
            newX = x + radius * Math.sin(a*i);
            newY = y + radius * Math.cos(a*i);
            gc.strokeLine(x, y, newX, newY);
            x = newX;
            y = newY;
        }
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
