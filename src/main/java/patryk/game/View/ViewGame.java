package patryk.game.View;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.util.Pair;
import patryk.game.MasterOfGame.MasterOfGame;

import java.io.File;

//klasa odpowiadająca za wyświetlanie przebiegu gry
public class ViewGame
{
    private Canvas canvas;
    private GraphicsContext gc;
    private MasterOfGame game;
    private Double windowSizeX;
    private Double windowSizeY;
    private Integer radius = 50;
    private Pair<Double,Double> points[][];
    private Pair<Integer,Integer> checkedArea = null;
    static final double a = 2 * Math.PI / 6;
    public ViewGame()
    {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        windowSizeX = screenBounds.getHeight();
        windowSizeY = screenBounds.getWidth();

        canvas = new Canvas(windowSizeY, windowSizeX);
        gc = canvas.getGraphicsContext2D();

        addMouseClickedEvent();

        game = new MasterOfGame().createMap(15,9);
        points = new Pair[game.map.getY()][game.map.getX()];
        drawHexagonalGrid(game.map.getX(),game.map.getY());
    }

    public void reDraw()
    {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawHexagonalGrid(game.map.getX(),game.map.getY());
    }

    private void addMouseClickedEvent()
    {
        //funkcja tymczasowa będzie jeszcze poprawiana
        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                Pair<Integer,Integer> clicedField = whichAreaCliced(e);
                if(e.getButton() != MouseButton.SECONDARY)
                {
                    if(game.map.areas[clicedField.getValue()][clicedField.getKey()].unit == null)
                    {
                        if(checkedArea != null)
                        {
                            game.map.areas[checkedArea.getValue()][checkedArea.getKey()].unit.moveTo = new Pair<>(clicedField.getKey(),clicedField.getValue());
                            new Thread(()->{
                                Pair<Integer,Integer> goFrom = checkedArea;
                                synchronized (this)
                                {
                                    checkedArea = null;
                                }
                                try
                                {
                                    Thread.sleep(1000);
                                }
                                catch (Exception f){}
                                Pair<Integer,Integer> t = game.map.areas[goFrom.getValue()][goFrom.getKey()].unit.moveTo;
                                synchronized (this)
                                {
                                    game.map.areas[t.getValue()][t.getKey()].unit = game.map.areas[goFrom.getValue()][goFrom.getKey()].unit;
                                    game.map.areas[goFrom.getValue()][goFrom.getKey()].unit = null;

                                }
                            }).start();
                        }
                    }
                    else
                    {
                        checkedArea = new Pair<>(clicedField.getKey(),clicedField.getValue());
                        System.out.println("zaznaczono");
                    }
                }
                if(e.getButton() == MouseButton.SECONDARY)
                {
                    //funkcja tymczasowo dodaje jednostke po kliknięci prawym przyciskiem
                    if(game.map.areas[clicedField.getValue()][clicedField.getKey()].unit == null)
                        game.map.areas[clicedField.getValue()][clicedField.getKey()].setUnit(100);
                    else
                        System.out.println("Jednosta już tu jest");
                }
                System.out.println(clicedField.getValue()+" "+clicedField.getKey());
            }
        };
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,eventHandler);
    }

    private Pair<Integer,Integer> whichAreaCliced(MouseEvent e)
    {
        Double distance = distance(points[0][0],new Pair<>(e.getX(),e.getY()));
        Pair<Integer,Integer> clicedField = new Pair(0,0);
        for(int i=0;i<game.map.getY();i++)
        {
            for(int j=0;j<game.map.getX();j++)
            {
                if(distance > distance(points[i][j],new Pair<>(e.getX(),e.getY())))
                {
                    distance = distance(points[i][j],new Pair<>(e.getX(),e.getY()));
                    clicedField = new Pair(j,i);
                }
            }
        }

        return clicedField;
    }

    private double distance(Pair<Double,Double> a,Pair<Double,Double> b)
    {
        Double x = Math.abs(a.getKey() - b.getKey());
        Double y = Math.abs(a.getValue() - b.getValue());
        return Math.sqrt(x*x+y*y);
    }

    public void drawHexagonalGrid(Integer sizeX, Integer sizeY)
    {
        double x=100,y=100;
        boolean f = true;
        for(int i=0;i<sizeY;i++)
            if(f)
            {
                for(int j=0;j<sizeX;j++)
                {
                    points[i][j] = new Pair<>(x+j*2*radius*Math.sin(a),y+i*(radius * (1+Math.cos(a))));
                    drawArea(j,i,x+j*2*radius*Math.sin(a),y+i*(radius * (1+Math.cos(a))));
                }
                f = !f;
            }
            else
            {
                for(int j=0;j<sizeX;j++)
                {
                    points[i][j] = new Pair<>(x+(j*2+1)*radius*Math.sin(a),y+i*(radius * (1+Math.cos(a))));
                    drawArea(j,i,x+(j*2+1)*radius*Math.sin(a),y+i*(radius * (1+Math.cos(a))));
                }
                f = !f;
            }
    }

    private void drawArea(Integer positionX,Integer positionY,Double x,Double y)
    {
        drawUnit(positionX,positionY,x,y);
        drawHexagon(x,y);
    }

    private void drawUnit(Integer positionX,Integer positionY,Double x,Double y)
    {
        if(game.map.areas[positionY][positionX].unit != null)
        {
            Double imgsize = radius*1.6;
            String imgUrl = "sword";
            File file = new File("img/"+imgUrl+".jpg");
            Image img = new Image(file.toURI().toString(),imgsize,imgsize,false,false);
            gc.drawImage(img,x-imgsize/2,y-imgsize/2);
            gc.fillText(game.map.areas[positionY][positionX].unit.size.toString(), x-(radius*0.2), y+(radius*2/3));
        }
    }

    private void drawHexagon(Double x,Double y)
    {
        double newX,newY;
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

    public Canvas getCanvas() {
        return canvas;
    }
}
