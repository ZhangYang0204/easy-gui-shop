package pers.zhangyang.easyguishop.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pers.zhangyang.easyguishop.enumration.ShopCommentPageStatsEnum;
import pers.zhangyang.easyguishop.exception.NotExistNextException;
import pers.zhangyang.easyguishop.exception.NotExistPreviousException;
import pers.zhangyang.easyguishop.exception.NotExistShopException;
import pers.zhangyang.easyguishop.meta.ShopCommentMeta;
import pers.zhangyang.easyguishop.meta.ShopMeta;
import pers.zhangyang.easyguishop.service.GuiService;
import pers.zhangyang.easyguishop.service.impl.GuiServiceImpl;
import pers.zhangyang.easyguishop.util.PageUtil;
import pers.zhangyang.easyguishop.util.ReplaceUtil;
import pers.zhangyang.easyguishop.util.TimeUtil;
import pers.zhangyang.easyguishop.util.TransactionInvocationHandler;
import pers.zhangyang.easyguishop.yaml.GuiYaml;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

public class ShopCommentPage implements InventoryHolder {

    private final Inventory inventory;
    private final List<ShopCommentMeta> shopCommentMetaList = new ArrayList<>();
    private final InventoryHolder previousHolder;
    private final Player player;
    private int pageIndex;
    private ShopCommentPageStatsEnum stats;
    private String searchContent;
    private ShopMeta shopMeta;

    public ShopCommentPage(InventoryHolder previousHolder, Player player, ShopMeta shopMeta) {
        this.player = player;
        this.shopMeta = shopMeta;
        String title = GuiYaml.INSTANCE.getString("gui.title.shopCommentPage");
        if (title == null) {
            this.inventory = Bukkit.createInventory(this, 54);
        } else {
            this.inventory = Bukkit.createInventory(this, 54, ChatColor.translateAlternateColorCodes('&', title));
        }
        stats = ShopCommentPageStatsEnum.NORMAL;
        initMenuBarWithoutChangePage();
        this.previousHolder = previousHolder;
    }

    public void send() throws SQLException {
        this.stats = ShopCommentPageStatsEnum.NORMAL;
        this.searchContent = null;
        this.pageIndex = 0;
        refresh();

    }

    public void searchByCommenterName(@NotNull String ownerName) throws SQLException {
        this.stats = ShopCommentPageStatsEnum.SEARCH_COMMENTER_NAME;
        this.searchContent = ownerName;
        this.pageIndex = 0;
        refresh();
    }

    public void refresh() throws SQLException {
        GuiService guiService = (GuiService) new TransactionInvocationHandler(GuiServiceImpl.INSTANCE).getProxy();
        this.shopMeta = guiService.getShop(this.shopMeta.getUuid());
        if (this.shopMeta == null) {
            if (previousHolder instanceof AllShopPageShopOptionPage) {
                ((AllShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof CollectedShopPageShopOptionPage) {
                ((CollectedShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof ManageShopPageShopOptionPage) {
                ((ManageShopPageShopOptionPage) previousHolder).send();
            }
            return;
        }
        shopCommentMetaList.clear();
        try {
            shopCommentMetaList.addAll(guiService.listShopComment(shopMeta.getUuid()));
        } catch (NotExistShopException e) {
            if (previousHolder instanceof AllShopPageShopOptionPage) {
                ((AllShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof CollectedShopPageShopOptionPage) {
                ((CollectedShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof ManageShopPageShopOptionPage) {
                ((ManageShopPageShopOptionPage) previousHolder).send();
            }
            return;
        }

        if (stats.equals(ShopCommentPageStatsEnum.SEARCH_COMMENTER_NAME)) {
            shopCommentMetaList.removeIf(shopCommentMeta -> !Objects.requireNonNull(Bukkit.getOfflinePlayer(UUID.fromString(shopCommentMeta.getCommenterUuid())).getName()).contains(searchContent));
        }
        int maxIndex = PageUtil.computeMaxPageIndex(shopCommentMetaList.size(), 45);
        if (pageIndex > maxIndex) {
            this.pageIndex = maxIndex;
        }


        refreshContent();
        if (pageIndex > 0) {
            ItemStack previous = GuiYaml.INSTANCE.getButton("gui.button.shopCommentPage.previous");
            inventory.setItem(45, previous);
        } else {
            inventory.setItem(45, null);
        }
        if (pageIndex < maxIndex) {
            ItemStack next = GuiYaml.INSTANCE.getButton("gui.button.shopCommentPage.next");
            inventory.setItem(53, next);
        } else {
            inventory.setItem(53, null);
        }
        player.openInventory(this.inventory);
    }

    //根据shopMetaList渲染当前页的0-44
    private void refreshContent() {
        for (int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }
        int pageMax = PageUtil.page(pageIndex, 45, new ArrayList<>(shopCommentMetaList)).size();
        //设置内容
        for (int i = 45 * pageIndex; i < 45 + 45 * pageIndex; i++) {
            if (i >= pageMax + 45 * pageIndex) {
                break;
            }
            ShopCommentMeta shopCommentMeta = shopCommentMetaList.get(i);
            OfflinePlayer commenter = Bukkit.getOfflinePlayer(UUID.fromString(shopCommentMeta.getCommenterUuid()));
            HashMap<String, String> rep = new HashMap<>();
            rep.put("{commenter_name}", commenter.getName());
            rep.put("{comment_time}", TimeUtil.getTimeFromTimeMill(shopCommentMeta.getCommentTime()));
            ItemStack itemStack = GuiYaml.INSTANCE.getButton("gui.button.shopCommentPage.shopComment");
            Gson gson = new Gson();
            Type stringListType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> stringList = gson.fromJson(shopCommentMeta.getContent(), stringListType);
            ReplaceUtil.formatLore(itemStack, "{(content)}", stringList);
            ReplaceUtil.replaceDisplayName(itemStack, rep);
            ReplaceUtil.replaceLore(itemStack, rep);
            inventory.setItem(i - 45 * pageIndex, itemStack);
        }
    }

    //渲染当前页的菜单(不包括翻页)
    private void initMenuBarWithoutChangePage() {
        ItemStack back = GuiYaml.INSTANCE.getButton("gui.button.shopCommentPage.back");
        inventory.setItem(49, back);


        ItemStack search = GuiYaml.INSTANCE.getButton("gui.button.shopCommentPage.searchByCommenterName");
        inventory.setItem(50, search);
    }

    public void nextPage() throws NotExistNextException, SQLException {
        GuiService guiService = (GuiService) new TransactionInvocationHandler(GuiServiceImpl.INSTANCE).getProxy();
        this.shopMeta = guiService.getShop(this.shopMeta.getUuid());
        if (this.shopMeta == null) {
            if (previousHolder instanceof AllShopPageShopOptionPage) {
                ((AllShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof CollectedShopPageShopOptionPage) {
                ((CollectedShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof ManageShopPageShopOptionPage) {
                ((ManageShopPageShopOptionPage) previousHolder).send();
            }
            return;
        }
        shopCommentMetaList.clear();
        try {
            shopCommentMetaList.addAll(guiService.listShopComment(shopMeta.getUuid()));
        } catch (NotExistShopException e) {
            if (previousHolder instanceof AllShopPageShopOptionPage) {
                ((AllShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof CollectedShopPageShopOptionPage) {
                ((CollectedShopPageShopOptionPage) previousHolder).send();
            }
            if (previousHolder instanceof ManageShopPageShopOptionPage) {
                ((ManageShopPageShopOptionPage) previousHolder).send();
            }
            return;
        }
        int maxIndex = PageUtil.computeMaxPageIndex(shopCommentMetaList.size(), 45);
        if (maxIndex <= pageIndex) {
            throw new NotExistNextException();
        }
        this.pageIndex++;
        refresh();
    }

    public void previousPage() throws NotExistPreviousException, SQLException {
        if (0 >= pageIndex) {
            throw new NotExistPreviousException();
        }
        this.pageIndex--;
        refresh();
    }

    public ShopMeta getShopMeta() {
        return shopMeta;
    }


    public InventoryHolder getPreviousHolder() {
        return previousHolder;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
