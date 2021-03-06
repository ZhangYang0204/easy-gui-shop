package pers.zhangyang.easyguishop.listener.managegoodpagegoodoptionpage;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pers.zhangyang.easyguishop.domain.ManageGoodPageGoodOptionPage;
import pers.zhangyang.easyguishop.yaml.MessageYaml;
import pers.zhangyang.easylibrary.annotation.EventListener;
import pers.zhangyang.easylibrary.annotation.GuiDiscreteButtonHandler;
import pers.zhangyang.easylibrary.util.MessageUtil;

@EventListener
public class PlayerClickManageGoodPageGoodOptionPageSetGoodItemPrice implements Listener {

    @GuiDiscreteButtonHandler(guiPage = ManageGoodPageGoodOptionPage.class, slot = {14})
    public void onPlayerClickAllShopNextPage(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("EasyGuiShop.setGoodItemPrice")) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notPermissionWhenSetGoodItemPrice"));
            return;
        }

        ManageGoodPageGoodOptionPage manageGoodPageGoodOptionPage = (ManageGoodPageGoodOptionPage) holder;
        new PlayerInputAfterClickManageGoodPageGoodOptionPageSetGoodItemPrice(player, manageGoodPageGoodOptionPage.getOwner(), manageGoodPageGoodOptionPage);

    }

}