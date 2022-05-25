package patryk.game.MasterOfGame;

public class Player
{
    String nick;

    public Player(String nick)
    {
        this.nick = nick;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null)
            return false;
        if(o instanceof Player p)
            return this.nick.equals(p.nick);
        return false;
    }
}