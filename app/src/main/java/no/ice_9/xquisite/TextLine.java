package no.ice_9.xquisite;

/**
 * Created by human on 31.03.16.
 */
public class TextLine {

    boolean empty=true;
    private final int mProgram;

    public TextLine(int selfLineNr,int program)
    {
        mProgram=program;
    }

    public void draw()
    {

    }

    public void set(String str)
    {
        empty=false;
    }

    public boolean isEmpty()
    {
        return empty;
    }
}
