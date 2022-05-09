package patryk.game.MasterOfGame;

//klasa odpowiadająca za przebieg gry
public class MasterOfGame
{
    public Map map;
    public MasterOfGame()
    {

    }
    public MasterOfGame createMap(Integer x,Integer y)
    {
        map = new Map(x,y);
        return this;
    }
}
