package gameDA.objects.model;


import gameDA.Game;
import gameDA.config.output.Camera;
import gameDA.config.output.SpriteSheet;
import gameDA.gui.Gamestate;
import gameDA.gui.menus.submenus.DeathMenu;
import gameDA.objects.*;

import java.awt.*;

import java.awt.image.BufferedImage;

import static gameDA.config.input.KeyListener.frameChange;

public class Player extends GameObject {
    private boolean onPlanet;
    private final BufferedImage playerSpaceSR;
    private final BufferedImage playerSpaceSL;
    private final BufferedImage playerSpaceSU;
    private final BufferedImage playerSpaceSD;
    private final int speed = 5;

    private final ObjectHandler objectHandler;
    private final BufferedImage[] playerOnPlanetL = new BufferedImage[3];
    private final BufferedImage[] playerOnPlanetR = new BufferedImage[3];
    private final Animation animL;
    private final Animation animR;

    public static Healthbar playerHealthbar = null;

    private int hp;
    private int ammo;
    private int fuel;
    private  int couldownCounter = 0;
    private int bulletSpeed;
    private int cooldownBullet;

    private int bulletCost;

    private int bulletDmg;

    private SpaceEnemy spaceEnemy;

    public Player(int x, int y, ObjectID id, SpriteSheet spriteSheet, ObjectHandler objectHandler, boolean onPlanet,
                  Camera camera, int hp, int ammo, int fuel, int bulletSpeed, int countdownBullet,int bulletCost,int bulletDmg) {
        super(x, y, id,spriteSheet);
        this.objectHandler = objectHandler;
        this.onPlanet = onPlanet;
        this.fuel = fuel;
        this.ammo = ammo;
        this.hp = hp;
        this.cooldownBullet = countdownBullet;
        this.bulletSpeed = bulletSpeed;
        this.bulletCost = bulletCost;
        this.bulletDmg = bulletDmg;
        playerSpaceSR = spriteSheet.getImage(7,8,32,32);
        playerSpaceSL = spriteSheet.getImage(8,8,32,32);
        playerSpaceSU = spriteSheet.getImage(7,7,32,32);
        playerSpaceSD = spriteSheet.getImage(8,7,32,32);
        playerOnPlanetR[0] = spriteSheet.getImage(1,6,32,32); //R1
        playerOnPlanetL[0] = spriteSheet.getImage(2,6,32,32); //L1
        playerOnPlanetR[1] = spriteSheet.getImage(3,6,32,32); //R2
        playerOnPlanetR[2] = spriteSheet.getImage(4,6,32,32); //R3
        playerOnPlanetL[1] = spriteSheet.getImage(5,6,32,32); //L2
        playerOnPlanetL[2] = spriteSheet.getImage(6,6,32,32); //L3
        animL = new Animation(6, playerOnPlanetL);
        animR = new Animation(6, playerOnPlanetR);
        playerHealthbar = new Healthbar(spriteSheet, hp,ammo,fuel, camera);

    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;
        motionCancelCollision(5,ObjectID.BLOCK);
        playerMovement();
        animL.runAnimation();
        animR.runAnimation();
        playerShooting();
        for (int i = 0; i < objectHandler.gameObjects.size(); i++) {
            GameObject tempObject = objectHandler.gameObjects.get(i);
            if(tempObject.getId() == ObjectID.ENEMY){
                spaceEnemy = (SpaceEnemy) tempObject;
            }
            if (tempObject.getId() == ObjectID.ENEMYBULLET) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    if(getHp() > 0){
                        setHp((getHp() - spaceEnemy.getBulletDmg()));
                        objectHandler.removeObj(tempObject);
                        playerHealthbar.setHp(getHp());
                    }
                    if (getHp() <= 0) {
                        System.out.println(Game.getGame().getGamestate());
                        Game.getGame().setGamestate(Gamestate.INMENU);
                        Game.getGame().getMenuHandler().setCurrentMenu(new DeathMenu());
                        break;
                    }
                }
            }
        }
    }


    @Override
    public void render(Graphics g) {
        playerHealthbar.render(g);
        if(!frameChange){
            if (onPlanet) g.drawImage(playerOnPlanetL[0], x, y, null);
            else
                g.drawImage(playerSpaceSD, x, y, null);
        }
        switch (objectHandler.getDirection()){
            case 'L':
                if (onPlanet) animL.drawAnimation(g, x, y, 0);
                else
                    g.drawImage(playerSpaceSL, x, y, null);
                break;
            case 'R':
                if (onPlanet) animR.drawAnimation(g,x,y,0);
                else
                    g.drawImage(playerSpaceSR, x, y, null);
                break;
            case 'D':
                if (onPlanet) g.drawImage(playerOnPlanetL[0], x, y, null);
                else
                    g.drawImage(playerSpaceSD, x, y, null);
                break;
            case 'U':
                if (onPlanet) g.drawImage(playerOnPlanetR[0], x, y, null);
                else
                    g.drawImage(playerSpaceSU, x, y, null);
                break;
        }
//        //Hitbox
//        //top
//        g.setColor(Color.cyan);
//        g.drawRect(x, y, 32, 5);
//        //right
//        g.setColor(Color.blue);
//        g.drawRect(x+27, y, 5, 32);
//        //left
//        g.setColor(Color.orange);
//        g.drawRect(x , y, 5, 32);
//        //bot
//        g.setColor(Color.pink);
//        g.drawRect(x, y+27, 32, 5);
    }
    @Override
    public Rectangle getBounds() {
        return new Rectangle(x,y,32,32);
    }
    @Override
    public Rectangle getTopBounds(int offset) {
        return new Rectangle(x, y-offset, 32, 5);
    }

    @Override
    public Rectangle getRightBounds(int offset) {
        return new Rectangle(x+offset+27, y, 5, 32);
    }

    @Override
    public Rectangle getLeftBounds(int offset) {
        return new Rectangle(x - offset, y, 5, 32);
    }

    @Override
    public Rectangle getBotBounds(int offset) {
        return new Rectangle(x, y+27+ offset, 32, 5);
    }
    public boolean isOnPlanet() {
        return onPlanet;
    }
   public void setOnPlanet(boolean onPlanet) {
        this.onPlanet = onPlanet;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAmmo() {
        return ammo;
    }

    public Player setAmmo(int ammo) {
        this.ammo = ammo;
        return this;
    }

    public int getFuel() {
        return fuel;
    }

    public Player setFuel(int fuel) {
        this.fuel = fuel;
        return this;
    }

    public int getBulletSpeed() {
        return bulletSpeed;
    }

    public Player setBulletSpeed(int bulletSpeed) {
        this.bulletSpeed = bulletSpeed;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public int getBulletDmg() {
        return bulletDmg;
    }

    public int getBulletCost() {
        return bulletCost;
    }

    public Player setBulletCost(int bulletCost) {
        this.bulletCost = bulletCost;
        return this;
    }

    public Player setBulletDmg(int bulletDmg) {
        this.bulletDmg = bulletDmg;
        return this;
    }
    public int getCooldownBullet() {
        return cooldownBullet;
    }

    public void setCooldownBullet(int cooldownBullet) {
        this.cooldownBullet = cooldownBullet;
    }
    private void motionCancelCollision(int offset, ObjectID objectID) {
        for (int i = 0; i < objectHandler.gameObjects.size(); i++) {
            GameObject tempObject = objectHandler.gameObjects.get(i);
            if (tempObject.getId() == objectID) {
                if (getLeftBounds(offset).intersects(tempObject.getRightBounds(0))) {
                    x += velocityX *= -1;
                }
                if (getRightBounds(offset).intersects(tempObject.getLeftBounds(0))) {
                    x += velocityX *= -1;
                }
                if (getTopBounds(offset).intersects(tempObject.getBotBounds(0))) {
                    y += velocityY *= -1;
                }
                if (getBotBounds(offset).intersects(tempObject.getTopBounds(0))) {
                    y += velocityY *= -1;
                }
            }
        }
    }
   private void playerMovement(){
       if(!(Game.gamestate.equals(Gamestate.INGAME))){
           velocityX = 0;
           velocityY = 0;
           objectHandler.setLeft(false);
           objectHandler.setRight(false);
           objectHandler.setUp(false);
           objectHandler.setDown(false);
           objectHandler.setSpace(false);
       }else {
           if (objectHandler.isLeft()) velocityX = -speed;
           if (objectHandler.isRight()) velocityX = speed;
           else if (!(objectHandler.isLeft() || objectHandler.isRight())) velocityX = 0;
           if (objectHandler.isUp()) velocityY = -speed;
           if (objectHandler.isDown()) velocityY = speed;
           else if (!(objectHandler.isUp() || objectHandler.isDown())) velocityY = 0;
        }
    }
   private void playerShooting(){
       if(!onPlanet) {
           couldownCounter++;
           if (objectHandler.isSpace() && couldownCounter > cooldownBullet) {
               if (playerHealthbar.getAmmo() > 0) {
                   objectHandler.addObj(new Bullet(getX(), getY(), ObjectID.PLAYERBULLET,
                           spriteSheet, objectHandler, bulletSpeed, objectHandler.getDirection(),true));
                   setAmmo(getAmmo()-getBulletCost());
                   playerHealthbar.setAmmo(getAmmo());
               }
               couldownCounter = 0;
           }
       }
   }
}

