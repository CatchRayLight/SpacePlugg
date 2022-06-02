package gameDA;


import gameDA.config.output.BufferedImageLoader;
import gameDA.config.output.Camera;
import gameDA.config.output.SpriteSheet;
import gameDA.gui.GameWindow;
import gameDA.gui.Gamestate;
import gameDA.gui.menus.MenuHandler;
import gameDA.gui.menus.MenuOption;
import gameDA.gui.menus.StartMenu;
import gameDA.objects.*;
import gameDA.objects.model.Block;
import gameDA.objects.model.Player;
import gameDA.config.input.KeyListener;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;


public class Game extends Canvas implements Runnable {

    private final int SCREEN_WIDTH = 1216;
    private final int SCREEN_HEIGHT = 928;
    private boolean isRunning = false;
    private Thread thread;
    private final ObjectHandler objectHandler;
    private int frame = 0;
    private BufferedImage testLvL = null;
    private BufferedImage spriteSheet = null;
    private BufferedImage background = null;
    private Camera camera;
    private Gamestate gamestate;
    private MenuHandler menuHandler;
    private KeyListener keyListener;
    private SpriteSheet spriteS;



    public Game() {
        new GameWindow(SCREEN_HEIGHT, SCREEN_WIDTH, "Space Plugg", this);
        start();
        objectHandler = new ObjectHandler();
        camera = new Camera(0, 0);
        keyListener = new KeyListener(objectHandler, menuHandler, gamestate);
        this.addKeyListener(keyListener);

        BufferedImageLoader loader = new BufferedImageLoader();
        testLvL = loader.loadImage("/TestLVL.png");
        spriteSheet = loader.loadImage("/SpriteSheet.png");
        spriteS = new SpriteSheet(spriteSheet);
        background = spriteS.getImage(5,1,32,32);




        loadLevel(testLvL);
        //Add new obj

    }

    public void updateGamestate(Gamestate newgamestate) {
        gamestate = newgamestate;
        keyListener.setGamestate(gamestate);
    }

    private void stop() throws InterruptedException {
        isRunning = false;
        thread.join();
    }

    private void start() {
        gamestate = Gamestate.INMENU;
        MenuOption[] menuOptions = {new MenuOption(() -> {
            updateGamestate(Gamestate.INGAME);
        }, "Play", 100, 100), new MenuOption(() -> {
            updateGamestate(Gamestate.INGAME);
        }, "Options",250,100), new MenuOption(() -> {
            updateGamestate(Gamestate.INGAME);
        }, "Exit",400,100)
        };
        menuHandler = new MenuHandler(new StartMenu(menuOptions));
        isRunning = true;
        thread = new Thread(this);
        thread.start();

    }

    //game-Loop

    @Override
    public void run() {
        /**
         * lastime: Time since the last iteration of the loop. Helps compute delta.
         * AmountOfTicks: The max FPS for the game.
         * ns: The number of nanoseconds per frame.
         * delta: The 'progress' that must be elapsed until the next frame.
         * frames: The number of frames elapsed since the last time we displayed the FPS.
         * time: The current time. Used to know when to display next the FPS.
         **/
        long lastime = System.nanoTime();
        double amountOfTicks = 60;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        int frames = 0;
        double time = System.currentTimeMillis();

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastime) / ns;
            lastime = now;

            if (delta >= 1) {
                update();
                render();
                frames++;
                delta--;
                if (System.currentTimeMillis() - time >= 1000) {
                    frame = frames;
                    System.out.println("fps:" + frames);
                    time += 1000;
                    frames = 0;
                }
            }
        }
    }


    public void update() {
        if (gamestate.equals(Gamestate.INGAME)) {
            for (int i = 0; i < objectHandler.gameObjects.size(); i++) {
                if (objectHandler.gameObjects.get(i).getId() == ObjectID.PLAYER) {
                    camera.update(objectHandler.gameObjects.get(i));
                }
            }
            objectHandler.update();

        }
        if (gamestate.equals(Gamestate.INMENU)) {
            menuHandler.update(this);
            objectHandler.update();
        }
    }




    public void render() {
        //starten bei null
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        //die tatsächlichen Frames sind schon vor dem Anzeigen da, "Preloaded", also bswp. 1ster Frame wird gezeigt
        // 2 andere sind schon in der Warteschlange dahinter zum zeigen
        if (bufferStrategy == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bufferStrategy.getDrawGraphics();
        //----------Ab hier wird auf den Canvas gezeichet
        //background

        g.setColor(Color.white);
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        //
        //ab hier werden die Objecte, Player, Walls etc auf den canvas gerendert
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.translate(-camera.getX(), -camera.getY());

        //only render ingame == true
        if(gamestate.equals(Gamestate.INGAME)) {
            //objects
            for (int i = 0; i < SCREEN_WIDTH*3; i+=32) {
                for (int j = 0; j < SCREEN_HEIGHT * 3; j += 32) {
                    g.drawImage(background, i, j, null);

                }
            }
            objectHandler.render(g);
            //----------bis hier
        }
        //only render in startmenu == true
        if(gamestate.equals(Gamestate.INMENU)){
            menuHandler.render(g);
        }
        graphics2D.translate(camera.getX(), camera.getY());
        //
        //in linke ecke die FPS
        g.setColor(Color.black);
        g.setFont(new Font("Courier New", Font.BOLD, 10));
        g.drawString("Frames: " + frame, 10, 10);

        g.dispose();
        bufferStrategy.show();

    }

    // level loader durch rbg differenzierung, png wird eingelesen und dann mit objecten verwiesen
    private void loadLevel(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int xAxis = 0; xAxis < width; xAxis++) {
            for (int yAxis = 0; yAxis < height; yAxis++) {
                int pixel = image.getRGB(xAxis, yAxis);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (red == 255) {
                    objectHandler.addObj(new Block(xAxis * 32, yAxis * 32, ObjectID.BLOCK,spriteS));
                }
                if (blue == 255) {
                    objectHandler.addObj(new Player(xAxis * 32, yAxis * 32, ObjectID.PLAYER, spriteS, objectHandler));
                }
                if(green == 255){

                }
            }
        }
    }



}
