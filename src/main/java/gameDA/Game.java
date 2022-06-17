package gameDA;


import gameDA.config.output.BufferedImageLoader;
import gameDA.config.output.Camera;
import gameDA.config.output.SpriteSheet;
import gameDA.gui.GameWindow;
import gameDA.gui.Gamestate;
import gameDA.gui.menus.*;
import gameDA.gui.menus.submenus.*;

import gameDA.objects.*;
import gameDA.objects.model.EventTeleportLVL;
import gameDA.objects.model.LootBox;
import gameDA.objects.model.SpaceEnemy;
import gameDA.objects.model.Walls;
import gameDA.objects.model.Player;
import gameDA.config.input.KeyListener;
import gameDA.savemanager.Options;
import gameDA.savemanager.SafeManager;
import gameDA.savemanager.Save;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;


public class Game extends Canvas implements Runnable {

    //Variablen und ressourcen benötigt für das Spiel
    public static final int SCREEN_WIDTH = 1216;
    public static final int SCREEN_HEIGHT = 928;
    private final BufferedImage backgroundImage[] = new BufferedImage[3];
    private final SpriteSheet spriteS;

    //Objekte zur Steuerung des Spieles
    private SafeManager safeManager;
    private MenuHandler menuHandler;
    private Options options;
    private final ObjectHandler objectHandler;
    private final KeyListener keyListener;
    private Sound sound;
    private final Camera camera;
    private Thread thread;
    private LvLHandler lvLHandler;
    private Save[] saves = new Save[]{
            new Save("saves","Save1.txt"), new Save("saves","Save2.txt"), new Save("saves","Save3.txt"), new Save("saves","config.txt")

    };

    //Variablen die Auskunft über den Zustand des Spieles geben
    public static Gamestate gamestate = Gamestate.INMENU;
    private int outputFrames;
    private boolean onPlanet;

    private int LvLInt;
    private boolean isRunning = false;

    //Game Instanz
    private static Game game;

    //Konstruktor
    public Game() {
        //Laden der Ressourcen
        BufferedImageLoader loader = new BufferedImageLoader();
        BufferedImage spriteSheet = loader.loadImage("/textures/SpriteSheet.png");
        backgroundImage[0] = loader.loadImage("/maps/backgroundTest.png");
        backgroundImage[1] = loader.loadImage("/maps/Planet1.png");
        //Initialisierungen
        game = this;
        new GameWindow(SCREEN_HEIGHT, SCREEN_WIDTH, "Space Plugg", loader);
        sound = new Sound();
        objectHandler = new ObjectHandler();
        lvLHandler = new LvLHandler();
        options = new Options(true, true); //Belade options mit Startwerten (falls keine gespeicherten Options verfügbar sind)
        safeManager = new SafeManager(saves);
        options = safeManager.loadOptions(); //Belade Options mit gespeicherten Options
        initMenus(); //Initialisiert alle nötigen Menus und Menuhandler
        spriteS = new SpriteSheet(spriteSheet);
        camera = new Camera(0, 0);
        keyListener = new KeyListener(objectHandler, menuHandler, gamestate);
        this.addKeyListener(keyListener);

        //Starten des Spieles und laden des Levels
        start();
        //setting the Start Level on 1 and declaring onPlanet boolean
        setOnPlanet(true);
        levelBuilder(lvLHandler.setLvL(getLvLInt()));

    }

    //Die Start Methode startet den Thread und somit das Spiel
    private void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    //Die Stop methode beendet den Thread
    private void stop() throws InterruptedException {
        isRunning = false;
        System.exit(0);
        thread.join();
    }
    //game-Loop
    /**
     * lastime: Time since the last iteration of the loop. Helps compute delta.
     * AmountOfTicks: The max FPS for the game.
     * ns: The number of nanoseconds per frame.
     * delta: The 'progress' that must be elapsed until the next frame.
     * frames: The number of frames elapsed since the last time we displayed the FPS.
     * time: The current time. Used to know when to display next the FPS.
     **/
    @Override
    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double FPS = 60.0;
        double ns = 1000000000 / FPS;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (isRunning) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / ns;
            timer += (currentTime - lastTime);
            lastTime = currentTime;
            while(delta >= 1) {
                delta--;
                update();
                render();
                //updates++;
                frames++;
            }
            if (timer >= 1000000000) {
                outputFrames = frames;
                frames = 0;
                timer = 0;
                //updates = 0;
            }
        }
        try {
            stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //update methode
    public void update() {
        if (gamestate.equals(Gamestate.INGAME)) {
            for (int i = 0; i < objectHandler.gameObjects.size(); i++) {
                if (objectHandler.gameObjects.get(i).getId() == ObjectID.PLAYER) {
                    GameObject tempObj = objectHandler.gameObjects.get(i);
                    if(!isOnPlanet())camera.update(objectHandler.gameObjects.get(i));
                    if(isOnPlanet()){
                        camera.setX(0);
                        camera.setY(0);
                    }
                }
            }
            objectHandler.update();
        }
        if (gamestate.equals(Gamestate.INMENU)) {
            menuHandler.update();
        }
    }

    //Render des gesamten Spielinhaltes
    public void render() {
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        if (bufferStrategy == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bufferStrategy.getDrawGraphics();
        switch (gamestate) {
            case INGAME:
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.translate(-camera.getX(), -camera.getY());
                // getLvL int %2 == 0 dann image on planet
                System.out.println(getLvLInt());
                System.out.println(isOnPlanet());
                g.drawImage(backgroundImage[1], 0, 0, null);
                if(getLvLInt() == 1 || getLvLInt() == 3 || getLvLInt() == 5) {
                    g.drawImage(backgroundImage[0], 0, 0, null);
                }
                objectHandler.render(g);
                graphics2D.translate(camera.getX(), camera.getY());
                g.setColor(Color.yellow);
                g.setFont(new Font("Courier New",Font.BOLD,10));
                g.drawString("Frames :" + outputFrames,10,10);
                break;
            case INMENU:
                menuHandler.render(g);
                break;
        }
        g.dispose();
        bufferStrategy.show();
    }

    // level loader durch rbg differenzierung, png wird eingelesen und dann mit objecten verwiesen
    public void levelBuilder(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int xAxis = 0; xAxis < width; xAxis++) {
            for (int yAxis = 0; yAxis < height; yAxis++) {
                int pixel = image.getRGB(xAxis, yAxis);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (blue == 255 && green != 255 && red != 255) {
                    if(getLvLInt() == 0) {
                        objectHandler.addObj(new Player(xAxis * 32, yAxis * 32, ObjectID.PLAYER, spriteS,
                                objectHandler, isOnPlanet(), camera, 100, 90, 15600, 6,
                                10, 10, 20));
                    }
                }
                if (red == 255 && green != 255 && blue != 255) {
                    objectHandler.addObj(new Walls(xAxis * 32, yAxis * 32, ObjectID.BLOCK, spriteS,isOnPlanet()));
                }
                if (green == 255 && blue != 255 && red != 255) {
                    objectHandler.addObj(new SpaceEnemy(xAxis * 32, yAxis * 32, ObjectID.ENEMY, spriteS,
                            objectHandler, 100,5,10,5));
                }
                if (green == 255 && blue == 255 && red != 255) {
                    //cyan
                    objectHandler.addObj(new LootBox(xAxis * 32, yAxis * 32, ObjectID.LOOTBOX, spriteS, objectHandler));
                }
                if (red == 255 && green == 255 && blue != 255) {
                    //yel
                    objectHandler.addObj(new EventTeleportLVL(xAxis * 32, yAxis * 32, ObjectID.EVENT, spriteS, objectHandler));
                }
            }
        }
    }

    //Initialisierung der Menus
    private void initMenus() {
        //TESTING
        safeManager.safe(2);
        safeManager.load(2);
        options = safeManager.loadOptions();

        //TESTING ENDS HERE
        MenuOption[] empty = {};
        //initinalize empty
        OptionsMenu optionsMenu = new OptionsMenu(empty);
        MainMenu mainMenu = new MainMenu(empty);
        SaveMenu saveMenu = new SaveMenu(empty);
        //menuoptions
        //StartMenu
        MenuOption[] menuOptionsStartmenu;
        MenuOption[] menuOptionsOptionsMenu;
        MenuOption[] menuOptionsSaveMenu1;
        MenuOption[] menuOptionsSaveMenu;

        menuOptionsSaveMenu1 = new MenuOption[]{new MenuOption(() -> {
            safeManager.safe();
            menuHandler.setCurrentMenu(mainMenu);
        }, "Save", 100, 100), new MenuOption(() -> {
            safeManager.load();
            menuHandler.setCurrentMenu(mainMenu);
        }, "Load", 100, 250), new MenuOption(() -> {
            menuHandler.setCurrentMenu(mainMenu);
        }, "Back", 100, 550)
        };

        menuOptionsSaveMenu = new MenuOption[]{new MenuOption(() -> {
            safeManager.setCurrentSaveToUse(0);
            saveMenu.setCurrentOption(0);
            saveMenu.setMenuOptions(menuOptionsSaveMenu1);
        }, "Save 1", 100, 100), new MenuOption(() -> {
            safeManager.setCurrentSaveToUse(1);
            saveMenu.setCurrentOption(0);
            saveMenu.setMenuOptions(menuOptionsSaveMenu1);
        }, "Save 2", 100, 250), new MenuOption(() -> {
            safeManager.setCurrentSaveToUse(2);
            saveMenu.setCurrentOption(0);
            saveMenu.setMenuOptions(menuOptionsSaveMenu1);
        }, "Save 3", 100, 400), new MenuOption(() -> {
            menuHandler.setCurrentMenu(mainMenu);
        }, "Back", 100, 550)
        };

        menuOptionsStartmenu = new MenuOption[]{new MenuOption(() -> {
            Game.getGame().getSound().stop();
            setGamestate(Gamestate.INGAME);
        }, "Play", 100, 100), new MenuOption(() -> {
            saveMenu.setCurrentOption(0);
            saveMenu.setMenuOptions(menuOptionsSaveMenu);
            menuHandler.setCurrentMenu(saveMenu);
        }, "Saves", 100, 250), new MenuOption(() -> {
            menuHandler.setCurrentMenu(optionsMenu);
        }, "Options", 100, 400), new MenuOption(() -> {
            /*
            try {
                stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            Game.getGame().setGamestate(Gamestate.INMENU);
            Game.getGame().getMenuHandler().setCurrentMenu(new FlyConfirmationMenu());

        }, "Exit", 100, 550)
        };

        //OptionsMenu
        String music;
        if(options.isMusic()) {
            music = "Music On";
        } else music = "Music Off";
        String autoSave;
        if(options.isAutoSave()) {
            autoSave = "AutoSave On";
        } else autoSave = "AutoSave Off";
        menuOptionsOptionsMenu = new MenuOption[]{new MenuOption(() -> {

                //Change Options and let music start
                options.setMusic(!options.isMusic());
                if (options.isMusic()) {
                    Game.getGame().getMenuHandler().getCurrentMenu().startMusic();
                } else {
                    Game.getGame().getSound().stop();
                }
                //Safe change in config
                safeManager.saveOptions(options);
                //reset this menu option of options menu
                String changeonOff;
                if(options.isMusic()) {
                    changeonOff = "Music On";
                } else changeonOff = "Music Off";
                Game.getGame().getMenuHandler().getCurrentMenu().getMenuOptions()[0].setText(changeonOff);
            }, music, 100, 100), new MenuOption(() -> {

            //Change Options
            options.setAutoSave(!options.isAutoSave());
            //Safe change in config
            safeManager.saveOptions(options);
            //reset this menu option of options menu
            String changeonOff;
            if(options.isAutoSave()) {
                changeonOff = "AutoSave On";
            } else changeonOff = "AutoSave Off";
            Game.getGame().getMenuHandler().getCurrentMenu().getMenuOptions()[1].setText(changeonOff);
            }, autoSave, 100, 250), new MenuOption(() -> {

                Game.getGame().getSound().stop();
                setGamestate(Gamestate.INGAME);

            }, "Option3", 100, 400), new MenuOption(() -> {
                //Sicher Options zu den Files
            safeManager.saveOptions(Game.game.getOptions());
            menuHandler.setCurrentMenu(mainMenu);
            }, "Back", 100, 550)
            };
        //SaveMenu

        //set new menuoptions
        mainMenu.setMenuOptions(menuOptionsStartmenu);
        optionsMenu.setMenuOptions(menuOptionsOptionsMenu);
        saveMenu.setMenuOptions(menuOptionsSaveMenu);

        menuHandler = new MenuHandler(mainMenu);

    }

    //Getter and setter


    public LvLHandler getLvLHandler() {
        return lvLHandler;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public static Gamestate getGamestate() {
        return gamestate;
    }

    public void setGamestate(Gamestate newgamestate) {
        gamestate = newgamestate;
        keyListener.setGamestate(gamestate);
        objectHandler.setLeft(false);
        objectHandler.setRight(false);
        objectHandler.setDown(false);
        objectHandler.setUp(false);
        objectHandler.setSpace(false);
    }

    public MenuHandler getMenuHandler() {
        return menuHandler;
    }

    public Camera getCamera() {
        return camera;
    }

    public ObjectHandler getObjectHandler() {
        return objectHandler;
    }

    public int getOutputFrames() {
        return outputFrames;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public static Game getGame() {
        return game;
    }
    public void togglePlanet(){
        this.onPlanet = !this.onPlanet;
    }

    public int getLvLInt() {
        return LvLInt;
    }

    public Game setLvLInt(int lvLInt) {
        LvLInt = lvLInt;
        return this;
    }

    public boolean isOnPlanet() {
        return onPlanet;
    }

    public Game setOnPlanet(boolean onPlanet) {
        this.onPlanet = onPlanet;
        return this;
    }
}
