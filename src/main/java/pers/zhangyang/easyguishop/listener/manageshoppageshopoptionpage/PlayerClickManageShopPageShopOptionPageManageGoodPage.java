package pers.zhangyang.easyguishop.listener.manageshoppageshopoptionpage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import pers.zhangyang.easyguishop.domain.ManageGoodPage;
import pers.zhangyang.easyguishop.domain.ManageShopPageShopOptionPage;
import pers.zhangyang.easyguishop.meta.ShopMeta;

import java.sql.SQLException;

public class PlayerClickManageShopPageShopOptionPageManageGoodPage implements Listener {

    @EventHandler
    public void onPlayerClickAllShopNextPage(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof ManageShopPageShopOptionPage)) {
            return;
        }
        int slot = event.getRawSlot();
        if (slot != 31) {
            return;
        }
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }


        Player player = (Player) event.getWhoClicked();
        ManageShopPageShopOptionPage manageShopPageShopOptionPage = (ManageShopPageShopOptionPage) holder;
        ShopMeta shopMeta = manageShopPageShopOptionPage.getShopMeta();
        ManageGoodPage manageGoodPage = new ManageGoodPage(manageShopPageShopOptionPage, player, shopMeta);
        try {
            manageGoodPage.send();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

}
