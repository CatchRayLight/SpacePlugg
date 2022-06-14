package gameDA.objects.model;

import gameDA.Game;
import gameDA.config.output.SpriteSheet;
import gameDA.gui.Gamestate;
import gameDA.gui.menus.submenus.DialogueMenu;
import gameDA.objects.GameObject;
import gameDA.objects.LvLHandler;
import gameDA.objects.ObjectHandler;
import gameDA.objects.ObjectID;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EventLvL1 extends GameObject {
    private final ObjectHandler objectHandler;
    private final BufferedImage img;

    private LvLHandler lvLHandler;


    public EventLvL1(int x, int y, ObjectID id, SpriteSheet spriteSheet, ObjectHandler objectHandler) {
        super(x, y, id, spriteSheet);
        this.objectHandler = objectHandler;
        img = spriteSheet.getImage(9,8,32,32);
        lvLHandler = new LvLHandler();
    }

    @Override
    public void update() {
        for (int i = 0; i < objectHandler.gameObjects.size() ; i++) {
            GameObject tempObject = objectHandler.gameObjects.get(i);
            if (tempObject.getId() == ObjectID.PLAYER) {
                if(getBounds().intersects(tempObject.getBounds())){
                    Game.getGame().setGamestate(Gamestate.INMENU);
                    System.out.println("Event");
                    //teleport player out first to not make bugs
                    lvLHandler.nextLvL(objectHandler);
                    //change lvl
                    //Triggers sample dialogue for now
                    String[][] sampleDialogue = new String[][]{{"Text"}, {"Text"}};
                    Game.getGame().getMenuHandler().setCurrentMenu(new DialogueMenu(sampleDialogue));
                    break;
                }
            }
        }
    }
    @Override
    public void render(Graphics g) {
        g.drawImage(img,x,y,null);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x,y,32,32);
    }

    @Override
    public Rectangle getTopBounds(int offset) {
        return null;
    }

    @Override
    public Rectangle getRightBounds(int offset) {
        return null;
    }

    @Override
    public Rectangle getLeftBounds(int offset) {
        return null;
    }

    @Override
    public Rectangle getBotBounds(int offset) {
        return null;
    }
}
