package pers.zhangyang.easyguishop.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.zhangyang.easyguishop.dao.*;
import pers.zhangyang.easyguishop.exception.*;
import pers.zhangyang.easyguishop.meta.*;
import pers.zhangyang.easyguishop.service.GuiService;
import pers.zhangyang.easyguishop.yaml.SettingYaml;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiServiceImpl implements GuiService {


    @Override
    public List<ShopMeta> listShop() {
        List<ShopMeta> shopMetaList = new ShopDao().list();
        shopMetaList.sort((o1, o2) -> {
            int star1 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o1.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o1.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o1.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            int star2 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o2.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o2.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o2.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            return star2 - star1;
        });
        return shopMetaList;
    }

    @Override
    public List<ShopMeta> listPlayerShop(String ownerUuid) {
        List<ShopMeta> shopMetaList = new ShopDao().listByOwnerUuid(ownerUuid);
        shopMetaList.sort((o1, o2) -> {
            int star1 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o1.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o1.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o1.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            int star2 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o2.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o2.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o2.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            return star2 - star1;
        });
        return shopMetaList;
    }

    @Override
    public List<ShopMeta> listPlayerCollectedShop(String playerUuid) {
        List<ShopMeta> shopMetaList = new ShopDao().list();
        List<ShopCollectorMeta> shopCollectorMetaList = new ShopCollectorDao().listByCollectorUuid(playerUuid);
        shopMetaList.removeIf(shopMeta -> {
            for (ShopCollectorMeta s : shopCollectorMetaList) {
                if (s.getShopUuid().equals(shopMeta.getUuid())) {
                    return false;
                }
            }
            return true;
        });
        shopMetaList.sort((o1, o2) -> {
            int star1 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o1.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o1.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o1.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            int star2 = Math.addExact(Math.addExact(
                            Math.multiplyExact(o2.getPopularity(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.popularity")),
                            Math.multiplyExact(o2.getCollectAmount(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.collectAmount"))),
                    Math.multiplyExact(o2.getPageView(), SettingYaml.INSTANCE.getHotValueCoefficient("setting.hotValueCoefficient.pageView")));
            return star2 - star1;
        });
        return shopMetaList;
    }

    @Override
    public List<ShopCommentMeta> listPlayerComment(String playerUuid) {
        List<ShopCommentMeta> shopCommentMetaList = new ShopCommentDao().listByPlayerUuid(playerUuid);
        Collections.reverse(shopCommentMetaList);
        return shopCommentMetaList;
    }

    @Override
    public List<ShopCommentMeta> listShopComment(String shopUuid) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        List<ShopCommentMeta> shopCommentMetaList = new ShopCommentDao().listByShopUuid(shopUuid);
        Collections.reverse(shopCommentMetaList);
        return shopCommentMetaList;
    }

    @Override
    public List<IconMeta> listIcon() {
        List<IconMeta> iconMetaList = new IconDao().list();
        Collections.reverse(iconMetaList);
        return iconMetaList;
    }

    @Override
    public List<IconMeta> listPlayerIcon(String ownerUuid) {
        List<IconMeta> shopMetaList = new IconDao().list();
        List<IconOwnerMeta> iconOwnerMetaList = new IconOwnerDao().listByOwnerUuid(ownerUuid);
        shopMetaList.removeIf(iconMeta -> {
            for (IconOwnerMeta i : iconOwnerMetaList) {
                if (i.getIconUuid().equals(iconMeta.getUuid())) {
                    return false;
                }
            }
            return true;
        });
        Collections.reverse(shopMetaList);
        return shopMetaList;
    }

    @Override
    public List<GoodMeta> listShopGood(String shopUuid) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        List<GoodMeta> goodMetaList = new GoodDao().listByShopUuid(shopUuid);
        Collections.reverse(goodMetaList);
        return goodMetaList;
    }

    @Override
    public List<ItemStockMeta> listPlayerItemStock(String playerUuid) {
        List<ItemStockMeta> itemStockMetaList = new ItemStockDao().listByPlayerUuid(playerUuid);
        Collections.reverse(itemStockMetaList);
        return itemStockMetaList;
    }

    @Override
    public List<TradeRecordMeta> listPlayerTradeRecord(String playerUuid) {
        List<TradeRecordMeta> tradeRecordMetaList = new TradeRecordDao().listByCustomerUuidOrMerchantUuid(playerUuid);
        Collections.reverse(tradeRecordMetaList);
        return tradeRecordMetaList;
    }

    @Override
    public void createShop(@NotNull ShopMeta shopMeta) throws DuplicateShopException {
        if (new ShopDao().getByName(shopMeta.getName()) != null || new ShopDao().getByUuid(shopMeta.getUuid()) != null) {
            throw new DuplicateShopException();
        }
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void viewShop(@NotNull String shopUuid, int amount) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        if (amount <= Integer.MAX_VALUE - shopMeta.getPageView()) {
            shopMeta.setPageView(shopMeta.getPageView() + amount);
        } else {
            shopMeta.setPageView(Integer.MAX_VALUE);
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void deleteShop(String shopName) throws NotExistShopException, ShopNotEmptyException {
        ShopMeta shopMeta = new ShopDao().getByName(shopName);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        if (!new GoodDao().listByShopUuid(shopMeta.getUuid()).isEmpty()) {
            throw new ShopNotEmptyException();
        }
        new ShopCommentDao().deleteByShopUuid(shopMeta.getUuid());
        new ShopDao().deleteByUuid(shopMeta.getUuid());
    }

    @Override
    public void addShopDescription(String shopUuid, String d) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());

        Gson gson = new Gson();
        Type stringListType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> stringList = gson.fromJson(shopMeta.getDescription(), stringListType);
        if (stringList != null) {
            stringList.add(d);
        } else {
            stringList = Collections.singletonList(d);
        }
        shopMeta.setShopDescription(gson.toJson(stringList));
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void removeShopDescription(String shopUuid) throws NotExistShopException, NotExistLineException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        Gson gson = new Gson();
        Type stringListType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> stringList = gson.fromJson(shopMeta.getDescription(), stringListType);
        if (stringList == null || stringList.isEmpty()) {
            throw new NotExistLineException();
        }
        stringList.remove(stringList.size() - 1);
        shopMeta.setShopDescription(gson.toJson(stringList));
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void updateShopDescription(String shopUuid, int lineIndex, String d) throws NotExistShopException, NotExistLineException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());

        Gson gson = new Gson();
        Type stringListType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> stringList = gson.fromJson(shopMeta.getDescription(), stringListType);
        if (stringList == null || stringList.size() < lineIndex + 1) {
            throw new NotExistLineException();
        }
        stringList.set(lineIndex, d);
        shopMeta.setShopDescription(gson.toJson(stringList));
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void resetShopDescription(String shopUuid) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setShopDescription(null);
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void setShopLocation(String shopUuid, String locationData) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setLocation(locationData);
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void resetShopLocation(String shopUuid) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setLocation(null);
        new ShopDao().insert(shopMeta);
    }

    @Override
    public void collectShop(ShopCollectorMeta shopCollectorMeta) throws DuplicateShopCollectorException, NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopCollectorMeta.getShopUuid());
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        ShopCollectorMeta shopCollectorMetaOld = new ShopCollectorDao().getByCollectorAndShopUuid(shopCollectorMeta.getCollectorUuid(),
                shopCollectorMeta.getShopUuid());
        if (shopCollectorMetaOld != null) {
            throw new DuplicateShopCollectorException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setCollectAmount(shopMeta.getCollectAmount() + 1);
        new ShopDao().insert(shopMeta);
        new ShopCollectorDao().insert(shopCollectorMeta);
    }

    @Override
    public void cancelCollectShop(String playerUuid, String shopUuid) throws NotExistShopCollectorException, NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        ShopCollectorMeta shopCollectorMeta = new ShopCollectorDao().getByCollectorAndShopUuid(playerUuid, shopUuid);
        if (shopCollectorMeta == null) {
            throw new NotExistShopCollectorException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setCollectAmount(shopMeta.getCollectAmount() - 1);
        new ShopDao().insert(shopMeta);
        new ShopCollectorDao().deleteByCollectorAndShopUuid(playerUuid, shopUuid);
    }

    @Override
    public void createShopComment(ShopCommentMeta shopCommentMeta) throws DuplicateShopCommenterException {
        if (new ShopCommentDao().getByUuid(shopCommentMeta.getUuid()) != null) {
            throw new DuplicateShopCommenterException();
        }
        new ShopCommentDao().insert(shopCommentMeta);
    }

    @Override
    public void deleteShopComment(String commentUuid) throws NotExistShopCommentException {
        if (new ShopCommentDao().getByUuid(commentUuid) == null) {
            throw new NotExistShopCommentException();
        }
        new ShopCommentDao().deleteByUuid(commentUuid);
    }

    @Override
    public @Nullable ShopCollectorMeta getShopCollector(String shopUuid, String collectorUuid) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        return new ShopCollectorDao().getByCollectorAndShopUuid(collectorUuid, shopUuid);
    }

    @Override
    public ShopMeta getShop(String shopUuid) {
        return new ShopDao().getByUuid(shopUuid);

    }

    @Override
    public GoodMeta getGood(String goodUuid) {
        return new GoodDao().getByUuid(goodUuid);
    }


    @Override
    public void buyIcon(String playerUuid, String iconUuid, IconMeta oldIconMeta) throws NotExistIconException, StateChangeException, NotMoreIconException, DuplicateIconOwnerException {
        IconOwnerMeta iconOwnerMeta = new IconOwnerMeta(iconUuid, playerUuid);
        IconMeta iconMeta = new IconDao().getByUuid(iconUuid);
        if (iconMeta == null) {
            throw new NotExistIconException();
        }
        if (!iconMeta.equals(oldIconMeta)) {
            throw new StateChangeException();
        }

        if (!iconMeta.isSystem()) {
            if (iconMeta.getStock() <= 0) {
                throw new NotMoreIconException();
            }
        }
        if (new IconOwnerDao().getByIconUuidAndOwnerUuid(iconOwnerMeta.getIconUuid(), iconOwnerMeta.getOwnerUuid()) != null) {
            throw new DuplicateIconOwnerException();
        }
        if (!iconMeta.isSystem()) {
            iconMeta.setStock(iconMeta.getStock() - 1);
            new IconDao().deleteByUuid(iconMeta.getUuid());
            new IconDao().insert(iconMeta);
        }
        new IconOwnerDao().insert(iconOwnerMeta);
    }


    @Override
    public void useShopIcon(String iconUuid, String shopUuid) throws NotExistIconException, NotExistShopException {
        IconMeta iconMeta = new IconDao().getByUuid(iconUuid);
        if (iconMeta == null) {
            throw new NotExistIconException();
        }
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        shopMeta.setIconUuid(iconUuid);
        new ShopDao().deleteByUuid(shopUuid);
        new ShopDao().insert(shopMeta);

    }

    @Override
    public void resetShopIcon(String shopUuid) throws NotExistShopException {

        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        shopMeta.setIconUuid(null);
        new ShopDao().deleteByUuid(shopUuid);
        new ShopDao().insert(shopMeta);
    }

    @Override
    public IconMeta getIcon(String iconUuid) {
        return new IconDao().getByUuid(iconUuid);
    }

    @Override
    public TradeRecordMeta getTradeRecord(String uuid) {
        return new TradeRecordDao().getByUuid(uuid);
    }

    @Override
    public void createGood(GoodMeta goodMeta) throws DuplicateGoodException {

        if (new GoodDao().getByUuid(goodMeta.getUuid()) != null || new GoodDao().getByNameAndShopUuid(goodMeta.getName(), goodMeta.getShopUuid()) != null) {
            throw new DuplicateGoodException();
        }
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void deleteGood(String goodName, String shopUuid) throws NotExistGoodException, GoodNotEmptyException {
        GoodMeta goodMeta = new GoodDao().getByNameAndShopUuid(goodName, shopUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (goodMeta.getStock() != 0) {
            throw new GoodNotEmptyException();
        }
        new GoodDao().deleteByUuid(goodMeta.getUuid());
    }

    @Override
    public void setGoodPlayerPointsPrice(String goodUuid, int price) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        goodMeta.setPlayerPointsPrice(price);
        goodMeta.setVaultPrice(null);
        goodMeta.setItemPrice(null);
        goodMeta.setCurrencyItemStack(null);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void setGoodItemPrice(String goodUuid, int price, String currencyData) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        goodMeta.setItemPrice(price);
        goodMeta.setCurrencyItemStack(currencyData);
        goodMeta.setPlayerPointsPrice(null);
        goodMeta.setVaultPrice(null);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void setGoodVaultPrice(String goodUuid, double price) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        goodMeta.setVaultPrice(price);
        goodMeta.setPlayerPointsPrice(null);
        goodMeta.setItemPrice(null);
        goodMeta.setCurrencyItemStack(null);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void setGoodLimitTime(String goodUuid, Integer time) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        goodMeta.setLimitTime(time);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void depositItemStock(String playerUuid, String itemStack, int amount) {
        ItemStockMeta itemStockMeta = new ItemStockDao().getByPlayerUuidAndItemStack(playerUuid, itemStack);
        if (itemStockMeta == null) {
            itemStockMeta = new ItemStockMeta(playerUuid, itemStack, 0);
            itemStockMeta.setAmount(itemStockMeta.getAmount() + amount);
            new ItemStockDao().insert(itemStockMeta);
            return;
        }
        if (amount > Integer.MAX_VALUE - itemStockMeta.getAmount()) {
            itemStockMeta.setAmount(Integer.MAX_VALUE);
        } else {
            itemStockMeta.setAmount(itemStockMeta.getAmount() + amount);
        }
        new ItemStockDao().deleteByPlayerUuidAndItemStack(playerUuid, itemStack);
        new ItemStockDao().insert(itemStockMeta);
    }

    @Override
    public void takeItemStock(String playerUuid, String itemStack, int amount) throws NotExistItemStockException, NotMoreItemStockException {
        ItemStockMeta itemStockMeta = new ItemStockDao().getByPlayerUuidAndItemStack(playerUuid, itemStack);
        if (itemStockMeta == null) {
            throw new NotExistItemStockException();
        }
        if (itemStockMeta.getAmount() - amount < 0) {
            throw new NotMoreItemStockException();
        }
        itemStockMeta.setAmount(itemStockMeta.getAmount() - amount);
        new ItemStockDao().deleteByPlayerUuidAndItemStack(playerUuid, itemStack);
        new ItemStockDao().insert(itemStockMeta);
    }

    @Override
    public void createItemStock(ItemStockMeta itemStockMeta0) throws DuplicateItemStockException {
        ItemStockMeta itemStockMeta = new ItemStockDao().getByPlayerUuidAndItemStack(itemStockMeta0.getPlayerUuid(), itemStockMeta0.getItemStack());
        if (itemStockMeta != null) {
            throw new DuplicateItemStockException();
        }
        new ItemStockDao().insert(itemStockMeta0);
    }

    @Override
    public void deleteItemStock(String playerUuid, String itemStack) throws ItemStockNotEmptyException, NotExistItemStockException {
        ItemStockMeta itemStockMeta = new ItemStockDao().getByPlayerUuidAndItemStack(playerUuid, itemStack);
        if (itemStockMeta == null) {
            throw new NotExistItemStockException();
        }
        if (itemStockMeta.getAmount() != 0) {
            throw new ItemStockNotEmptyException();
        }
        new ItemStockDao().deleteByPlayerUuidAndItemStack(playerUuid, itemStack);
    }

    @Override
    public ItemStockMeta getItemStock(String playerUuid, String itemStack) {
        return new ItemStockDao().getByPlayerUuidAndItemStack(playerUuid, itemStack);

    }

    @Override
    public void changeGoodTransactionType(String goodUuid) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (goodMeta.getType().equalsIgnoreCase("??????")) {
            goodMeta.setType("??????");
        } else if (goodMeta.getType().equalsIgnoreCase("??????")) {
            goodMeta.setType("??????");
        }
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void trade(String goodUuid, int amount, GoodMeta old) throws NotExistGoodException, StateChangeException, NotMoreGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (!old.equals(goodMeta)) {
            throw new StateChangeException();
        }
        if (goodMeta.isSystem()) {
            return;
        }


        if (goodMeta.getType().equalsIgnoreCase("??????") && !goodMeta.isSystem()) {
            if (amount > Integer.MAX_VALUE - goodMeta.getStock()) {
                goodMeta.setStock(Integer.MAX_VALUE);
            } else {
                goodMeta.setStock(goodMeta.getStock() + amount);
            }

        } else if (goodMeta.getType().equalsIgnoreCase("??????") && !goodMeta.isSystem()) {
            if (goodMeta.getStock() - amount < 0) {
                throw new NotMoreGoodException();
            }
            goodMeta.setStock(goodMeta.getStock() - amount);
        }
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }


    @Override
    public void createTradeRecord(TradeRecordMeta tradeRecordMeta) throws DuplicateTradeRecordException {
        if (new TradeRecordDao().getByUuid(tradeRecordMeta.getUuid()) != null) {
            throw new DuplicateTradeRecordException();
        }
        new TradeRecordDao().insert(tradeRecordMeta);
    }

    @Override
    public void setGoodName(String goodUuid, String name) throws NotExistGoodException, DuplicateGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (new GoodDao().getByNameAndShopUuid(name, goodMeta.getShopUuid()) != null) {
            throw new DuplicateGoodException();
        }
        goodMeta.setName(name);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void setShopName(String shopUuid, String name) throws NotExistShopException, DuplicateShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        if (new ShopDao().getByName(name) != null) {
            throw new DuplicateShopException();
        }
        shopMeta.setName(name);
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        new ShopDao().insert(shopMeta);
    }


    @Override
    public void depositGood(String goodUuid, int amount) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (amount > Integer.MAX_VALUE - goodMeta.getStock()) {
            goodMeta.setStock(Integer.MAX_VALUE);
        } else {
            goodMeta.setStock(goodMeta.getStock() + amount);
        }
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void takeGood(String goodUuid, int amount) throws NotMoreGoodException, NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        if (goodMeta.getStock() - amount < 0) {
            throw new NotMoreGoodException();
        }
        goodMeta.setStock(goodMeta.getStock() - amount);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }


    @Override
    public boolean hasItemStock(String playerUuid, String itemStack, int amount) {
        ItemStockMeta itemStockMeta = new ItemStockDao().getByPlayerUuidAndItemStack(playerUuid, itemStack);
        if (itemStockMeta == null) {
            return false;
        }
        return itemStockMeta.getAmount() >= amount;
    }

    @Override
    public void resetGoodPrice(String goodUuid) throws NotExistGoodException {
        GoodMeta goodMeta = new GoodDao().getByUuid(goodUuid);
        if (goodMeta == null) {
            throw new NotExistGoodException();
        }
        goodMeta.setPlayerPointsPrice(null);
        goodMeta.setCurrencyItemStack(null);
        goodMeta.setVaultPrice(null);
        goodMeta.setItemPrice(null);
        new GoodDao().deleteByUuid(goodMeta.getUuid());
        new GoodDao().insert(goodMeta);
    }

    @Override
    public void setShopDescription(String shopUuid, String descriptionJson) throws NotExistShopException {
        ShopMeta shopMeta = new ShopDao().getByUuid(shopUuid);
        if (shopMeta == null) {
            throw new NotExistShopException();
        }
        new ShopDao().deleteByUuid(shopMeta.getUuid());
        shopMeta.setShopDescription(descriptionJson);
        new ShopDao().insert(shopMeta);
    }


}
