package gameDA.gui.menus.submenus;


import gameDA.gui.menus.Menu;
import gameDA.gui.menus.MenuOption;


import java.awt.*;


public class MainMenu extends Menu {
        public MainMenu(MenuOption[] menuOptions){
        super(menuOptions,0);
    }

    @Override
    public void render(Graphics g) {
        for(int i = 0; i < getMenuOptions().length; i++) {
            MenuOption op = getMenuOptions()[i];
            op.render(g);
        }
    }
    @Override
    public void updateMenu() {
    }

}