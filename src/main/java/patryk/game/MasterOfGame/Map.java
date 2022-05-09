package patryk.game.MasterOfGame;

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
                areas[i][j] = new Area();
    }
    public Integer getX() {
        return sizeX;
    }
    public Integer getY() {
        return sizeY;
    }
}
