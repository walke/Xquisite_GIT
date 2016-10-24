package no.ice_9.xquisite_POL_ENG;

/**
 * Created by human on 05.07.16.
 */
public class StoryPlayingClass extends PlayerClass {

    public StoryPlayingClass(MainActivity activity, ASCIIscreen ascii, DBmanager dBman, int parent, int parentParts, Session session)
    {
        tAct=activity;
        mDBmanager=dBman;
        mAscii = ascii;
        mParent=parent;
        mStoryParts=parentParts;

        mSession=session;

        init();
    }

}
