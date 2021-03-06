package pers.zhangyang.easyguishop.listener.allgoodpagegoodoptionpage;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import pers.zhangyang.easyguishop.domain.AllGoodPageGoodOptionPage;
import pers.zhangyang.easyguishop.exception.*;
import pers.zhangyang.easyguishop.meta.GoodMeta;
import pers.zhangyang.easyguishop.meta.TradeRecordMeta;
import pers.zhangyang.easyguishop.service.GuiService;
import pers.zhangyang.easyguishop.service.impl.GuiServiceImpl;
import pers.zhangyang.easyguishop.yaml.MessageYaml;
import pers.zhangyang.easyguishop.yaml.SettingYaml;
import pers.zhangyang.easylibrary.base.FiniteInputListenerBase;
import pers.zhangyang.easylibrary.other.playerpoints.PlayerPoints;
import pers.zhangyang.easylibrary.other.vault.Vault;
import pers.zhangyang.easylibrary.util.*;

import java.util.UUID;

public class PlayerInputAfterClickAllGoodPageGoodOptionPageTradeGood extends FiniteInputListenerBase {

    private final GoodMeta goodMeta;
    private final AllGoodPageGoodOptionPage allGoodPageGoodOptionPage;

    public PlayerInputAfterClickAllGoodPageGoodOptionPageTradeGood(Player player, OfflinePlayer owner, GoodMeta goodMeta, AllGoodPageGoodOptionPage manageShopPage) {
        super(player, owner, manageShopPage, 1);
        this.allGoodPageGoodOptionPage = manageShopPage;
        this.goodMeta = goodMeta;
        MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.howToTrade"));
    }

    @Override
    public void run() {
        int amount;
        try {
            amount = Integer.parseInt(messages[0]);
        } catch (NumberFormatException e) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.invalidNumber"));
            return;
        }
        if (amount < 0) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.invalidNumber"));
            return;
        }


        GuiService guiService = (GuiService) new TransactionInvocationHandler(new GuiServiceImpl()).getProxy();

        //????????????????????????????????????????????????  ?????????????????????????????????????????????
        //?????????????????? ????????????????????????  ???????????????????????????     ???????????? ????????????  ????????????

        //????????????
        Integer limitTime = goodMeta.getLimitTime();
        if (limitTime != null && limitTime * 1000 + goodMeta.getCreateTime() < System.currentTimeMillis()) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.exceedLimitTime"));
            return;
        }

        //????????????????????????
        Double vaultPrice = goodMeta.getVaultPrice();
        Integer itemPrice = goodMeta.getItemPrice();
        Integer playerPointsPrice = goodMeta.getPlayerPointsPrice();
        Economy vault = Vault.hook();
        PlayerPointsAPI playerPoints = PlayerPoints.hook();
        OfflinePlayer merchant = Bukkit.getOfflinePlayer(UUID.fromString(allGoodPageGoodOptionPage.getShopMeta().getOwnerUuid()));

        if (vaultPrice == null && itemPrice == null && playerPointsPrice == null) {
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notSetPrice"));
            return;
        }


        if (goodMeta.getType().equalsIgnoreCase("??????")) {
            //??????????????????????????????
            int have = PlayerUtil.computeItemHave(ItemStackUtil.itemStackDeserialize(goodMeta.getGoodItemStack()), player);
            if (have < amount) {
                MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughGoodWhenTradeGood"));
                return;
            }

            if (vaultPrice != null) {
                if (vault == null) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notHookVault"));
                    return;
                }
                double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.vault");
                double beforeTax = vaultPrice * amount;
                double tax = beforeTax * taxRate;
                double afterTax = beforeTax - tax;
                //????????????????????????
                if (!goodMeta.isSystem() && !vault.has(merchant, beforeTax)) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreVaultWhenTradeGood"));
                    return;
                }
                //??????
                try {
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            merchant.getUniqueId().toString(), goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(),
                            System.currentTimeMillis(), goodMeta.getType(), taxRate);
                    tradeRecordMeta.setGoodVaultPrice(goodMeta.getVaultPrice());
                    guiService.createTradeRecord(tradeRecordMeta);
                    allGoodPageGoodOptionPage.send();
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (DuplicateTradeRecordException | NotMoreGoodException ignored) {
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }
                //????????????
                vault.depositPlayer(owner, afterTax);
                if (!goodMeta.isSystem()) {
                    vault.withdrawPlayer(merchant, beforeTax);
                }
            }
            if (playerPointsPrice != null) {
                if (playerPoints == null) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notHookPlayerPoints"));
                    return;
                }
                double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.playerPoints");
                int beforeTax = playerPointsPrice * amount;
                int tax = (int) Math.round(beforeTax * taxRate);
                int afterTax = beforeTax - tax;
                //?????????
                if (!goodMeta.isSystem() && playerPoints.look(merchant.getUniqueId()) < beforeTax) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMorePlayerPointsWhenTradeGood"));
                    return;
                }

                try {
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            merchant.getUniqueId().toString(), goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(),
                            System.currentTimeMillis(), goodMeta.getType(), taxRate);
                    tradeRecordMeta.setGoodPlayerPointsPrice(goodMeta.getPlayerPointsPrice());
                    guiService.createTradeRecord(tradeRecordMeta);
                    allGoodPageGoodOptionPage.send();
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (DuplicateTradeRecordException | NotMoreGoodException ignored) {
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }
                playerPoints.give(owner.getUniqueId(), afterTax);

                if (!goodMeta.isSystem()) {
                    playerPoints.take(merchant.getUniqueId(), beforeTax);
                }
            }
            if (itemPrice != null) {

                try {
                    double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.item");
                    int beforeTax = itemPrice * amount;
                    int tax = (int) Math.round(beforeTax * taxRate);
                    int afterTax = beforeTax - tax;
                    //?????????
                    if (!goodMeta.isSystem() && !guiService.hasItemStock(merchant.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), beforeTax)) {
                        MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreItemStockWhenTradeGood"));
                        return;
                    }
                    //??????
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            allGoodPageGoodOptionPage.getShopMeta().getOwnerUuid(),
                            goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(), System.currentTimeMillis(), goodMeta.getType(),
                            taxRate);
                    tradeRecordMeta.setGoodItemPrice(goodMeta.getItemPrice());
                    tradeRecordMeta.setGoodCurrencyItemStack(goodMeta.getCurrencyItemStack());
                    guiService.createTradeRecord(tradeRecordMeta);
                    allGoodPageGoodOptionPage.send();

                    //??????

                    if (!goodMeta.isSystem()) {
                        guiService.takeItemStock(merchant.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), beforeTax);
                    }
                    guiService.depositItemStock(owner.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), afterTax);
                } catch (NotMoreItemStockException | NotMoreGoodException | DuplicateTradeRecordException | NotExistItemStockException ignored) {
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }
            }
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.tradeGood"));
            //??????????????????
            PlayerUtil.removeItem(player, ItemStackUtil.itemStackDeserialize(goodMeta.getGoodItemStack()), amount);

        } else if (goodMeta.getType().equalsIgnoreCase("??????")) {
            //????????????????????????
            int space = PlayerUtil.checkSpace(player, ItemStackUtil.itemStackDeserialize(goodMeta.getGoodItemStack()));
            if (space < amount) {
                MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughSpaceWhenTradeGood"));
                return;
            }

            if (vaultPrice != null) {
                if (vault == null) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notHookVault"));
                    return;
                }
                double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.vault");
                double beforeTax = vaultPrice * amount;
                double tax = beforeTax * taxRate;
                double afterTax = beforeTax - tax;
                //??????????????????
                if (!vault.has(owner, beforeTax)) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughVaultWhenTradeGood"));
                    return;
                }
                //??????
                try {
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            merchant.getUniqueId().toString(), goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(),
                            System.currentTimeMillis(), goodMeta.getType(), taxRate);
                    tradeRecordMeta.setGoodVaultPrice(goodMeta.getVaultPrice());
                    guiService.createTradeRecord(tradeRecordMeta);
                    allGoodPageGoodOptionPage.send();
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (DuplicateTradeRecordException ignored) {
                } catch (NotMoreGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreGood"));
                    return;
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }
                vault.withdrawPlayer(owner, beforeTax);
                if (!goodMeta.isSystem()) {
                    vault.depositPlayer(merchant, afterTax);
                }

            }
            if (playerPointsPrice != null) {
                if (playerPoints == null) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notHookPlayerPoints"));
                    return;
                }

                double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.playerPoints");
                int beforeTax = playerPointsPrice * amount;
                int tax = (int) Math.round(beforeTax * taxRate);
                int afterTax = beforeTax - tax;
                //?????????
                if (playerPoints.look(owner.getUniqueId()) < beforeTax) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughPlayerPointsWhenTradeGood"));
                    return;
                }


                try {
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            merchant.getUniqueId().toString(), goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(),
                            System.currentTimeMillis(), goodMeta.getType(), taxRate);
                    tradeRecordMeta.setGoodPlayerPointsPrice(goodMeta.getPlayerPointsPrice());
                    guiService.createTradeRecord(tradeRecordMeta);
                    allGoodPageGoodOptionPage.send();
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (DuplicateTradeRecordException ignored) {
                } catch (NotMoreGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreGood"));
                    return;
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }
                playerPoints.take(owner.getUniqueId(), beforeTax);

                if (!goodMeta.isSystem()) {
                    playerPoints.give(merchant.getUniqueId(), afterTax);
                }

            }
            if (itemPrice != null) {
                double taxRate = SettingYaml.INSTANCE.getTax("setting.tax.item");
                int beforeTax = itemPrice * amount;
                int tax = (int) Math.round(beforeTax * taxRate);
                int afterTax = beforeTax - tax;
                //????????????
                try {
                    //?????????
                    if (!guiService.hasItemStock(owner.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), beforeTax)) {
                        MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notEnoughItemStockWhenTradeGood"));
                        return;
                    }
                    //??????
                    guiService.trade(goodMeta.getUuid(), amount, goodMeta);
                    TradeRecordMeta tradeRecordMeta = new TradeRecordMeta(UuidUtil.getUUID(), owner.getUniqueId().toString(),
                            merchant.getUniqueId().toString(), goodMeta.getGoodItemStack(), amount, goodMeta.isSystem(),
                            System.currentTimeMillis(), goodMeta.getType(), taxRate);
                    tradeRecordMeta.setGoodItemPrice(goodMeta.getItemPrice());
                    tradeRecordMeta.setGoodCurrencyItemStack(goodMeta.getCurrencyItemStack());
                    guiService.createTradeRecord(tradeRecordMeta);
                    //??????
                    guiService.takeItemStock(owner.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), beforeTax);

                    if (!goodMeta.isSystem()) {
                        guiService.depositItemStock(merchant.getUniqueId().toString(), goodMeta.getCurrencyItemStack(), afterTax);
                    }
                    allGoodPageGoodOptionPage.send();
                } catch (NotMoreItemStockException | DuplicateTradeRecordException | NotExistItemStockException ignored) {
                } catch (NotExistGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notExistGood"));
                    return;
                } catch (NotMoreGoodException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.notMoreGood"));
                    return;
                } catch (StateChangeException e) {
                    MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.stateChange"));
                    return;
                }

            }

            //??????????????????
            PlayerUtil.addItem(player, ItemStackUtil.itemStackDeserialize(goodMeta.getGoodItemStack()), amount);
            MessageUtil.sendMessageTo(player, MessageYaml.INSTANCE.getStringList("message.chat.tradeGood"));

        }
    }
}
