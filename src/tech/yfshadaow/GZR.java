package tech.yfshadaow;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class GZR extends JavaPlugin implements Listener {
    ItemStack diamond; ItemStack red; ItemStack heart; ItemStack clock; ItemStack emerald; ItemStack sugar;
    Location[] locations;
    World world;
    boolean isGameRunning;
    Scoreboard gzr;
    Scoreboard scoreboard;
    long gameTime;
    long startTime;
    Set<Player> playersInGame;

    @EventHandler
    public void freezeGui(EntityDamageByEntityEvent edbee) {
        if (!isGameRunning) {
            return;
        }
        if (!(edbee.getDamager() instanceof Player)) {
            return;
        }
        if (!(edbee.getEntity() instanceof Player)) {
            return;
        }
        if (checkIsGui((Player)edbee.getDamager())) {
            ((Player)edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,40,254));
            ((Player)edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP,40,190));
        }
    }

    @EventHandler
    public void unhidePlayer(PlayerDeathEvent pde) {
        if (!pde.getEntity().getScoreboardTags().contains("gzr")) {
            return;
        }
    }

    @EventHandler
    public void unhidePlayer(PlayerQuitEvent pqe) {
        if (!pqe.getPlayer().getScoreboardTags().contains("gzr")) {
            return;
        }
    }

    @EventHandler
    public void cancelPickup (PlayerPickupItemEvent ppie) {
        Player p = ppie.getPlayer();
        if (checkIsRen(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.CLOCK)
                    || ppie.getItem().getItemStack().getType().equals(Material.EMERALD)) {
                ppie.setCancelled(true);
            }
        } else if (checkIsGui(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.DIAMOND)
                    || ppie.getItem().getItemStack().getType().equals(Material.RED_DYE)
                    || ppie.getItem().getItemStack().getType().equals(Material.HEART_OF_THE_SEA)) {
                ppie.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        if (!isGameRunning) {
            return;
        }
        if (!pie.getAction().equals(Action.RIGHT_CLICK_AIR) && !pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {return;}// 不是右键
        if (pie.getItem() == null) {return;}//没有物品
        if (pie.getItem().getItemMeta() == null) {return;}//没有meta
        Player executor = pie.getPlayer();
        if (!executor.getScoreboardTags().contains("gzr")) {return;}//不在gzr里
        //这里开始添加内容

        switch (pie.getItem().getItemMeta().getDisplayName()) {
            case "冰冻鬼5秒" :
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b身边的鬼被冰冻5秒！");
                for (Player p : playersInGame) {
                    if (p.getLocation().distance(executor.getLocation()) > 6) {
                        continue;
                    }
                    if (!checkIsGui(p)) {
                        continue;
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,254));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,100,190));
                }
                break;
            case "回满血" :
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c回复全部生命值！");
                executor.setHealth(executor.getMaxHealth());
                break;
            case "隐身10秒" :
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c隐身10秒！");
                executor.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,200,0,false,false));
                break;
            case "延时15秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§c延时15秒！");
                startTime += 300;
                break;
            case "人发光5秒":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                for (Player p : playersInGame) {
                    if (p == null) {
                        continue;
                    }
                    if (!p.isOnline()) {
                        continue;
                    }
                    p.sendMessage("§b所有人发光5秒！");
                    if (!scoreboard.getTeam("gzrB").hasPlayer(p)) {
                        continue;
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,0));
                }
                break;
            case "加速":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b获得加速！");
                executor.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100,0));
                break;
        }
    }

    @EventHandler
    public void preventRegen(EntityRegainHealthEvent erhe) {
        if (!isGameRunning) {
            return;
        }
        if (!(erhe.getEntity() instanceof Player)) {
            return;
        }
        if (!erhe.getEntity().getScoreboardTags().contains("gzr")) {
            return;
        }
        if (!((Player) erhe.getEntity()).getGameMode().equals(GameMode.ADVENTURE)) {
            return;
        }
        if (erhe.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED)) {
            erhe.setCancelled(true);
        } else if (erhe.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.EATING)) {
            erhe.setCancelled(true);
        }
    }


    @EventHandler
    public void setGameTime(PlayerInteractEvent pie) {
        if (pie.getClickedBlock() == null) {
            return;
        }
        Location location = pie.getClickedBlock().getLocation();
        long x = location.getBlockX(); long y = location.getBlockY(); long z = location.getBlockZ();
        if (x == -5 && y == 204 && z == -1002) {
            switch ((int)gameTime) {
                case 3600:
                case 6000:
                case 8400:
                    gameTime += 2400;
                    break;
                case 10800:
                    gameTime = 3600;
                    break;
                default:
                    break;
            }
            Sign sign = (Sign) pie.getClickedBlock().getState();
            sign.setLine(2,"当前时间为 " + gameTime/1200 + " 分钟");
            sign.update();
        }
    }

    @EventHandler
    public void onGameStart(ServerCommandEvent sce) {
        if (sce.getCommand().equals("function gzr:out")) {
            if (!isGameRunning) {//开始游戏了
                isGameRunning = true;
                for (Player p : Bukkit.getOnlinePlayers()) { //添加所有游戏中玩家
                    if (!p.getScoreboardTags().contains("gzr")) {
                        continue;
                    } else if (!p.getGameMode().equals(GameMode.ADVENTURE)) {
                        continue;
                    }
                    playersInGame.add(p);
                }
                startTime = getTime(world);
                gzr.getObjective("gzr").setDisplaySlot(DisplaySlot.SIDEBAR);


                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                    for (Player p: playersInGame) {
                        if (checkIsGui(p)) {
                            for (Player victim : playersInGame) {
                                if (checkIsRen(victim)) {
                                    if (p.getLocation().distance(victim.getLocation()) < 10) {
                                        victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                        Bukkit.getScheduler().runTaskLater(this,() -> {
                                            victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                        }, 3);
                                    }
                                    if (p.getLocation().distance(victim.getLocation()) < 5) {
                                        Bukkit.getScheduler().runTaskLater(this,() -> {
                                            victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                        }, 10);
                                        Bukkit.getScheduler().runTaskLater(this,() -> {
                                            victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                        }, 13);
                                    }

                                }
                            }
                        }
                    }
                },20,20);


                Random random = new Random();
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this,() -> {
                    for (Player p: playersInGame) {
                        if (p == null) {
                            continue;
                        }
                        if (!p.isOnline()) {
                            continue;
                        }
                        p.sendMessage("§a道具已刷新！");
                    }
                    for (Location loc : locations) {
                        double spawnChance = random.nextDouble();
                        if (spawnChance < 0.5) {
                            double spawnNo = random.nextDouble();
                            if (spawnNo < (1f/36*10)) {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(diamond);
                            } else if (spawnNo < 1f/36*20) {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(red);
                            } else if (spawnNo < 1f/36*25) {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(clock);
                            } else if (spawnNo < 1f/36*28) {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(emerald);
                            } else if (spawnNo < 1f/36*33) {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(sugar);
                            } else {
                                ((Chest)(world.getBlockAt(loc).getState())).getBlockInventory().addItem(heart);
                            }
                        }
                    }
                },600,1200);


                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                    long time = getTime(world);
                    if (time - startTime > gameTime) {
                        Bukkit.getScheduler().cancelTasks(this);
                        world.getBlockAt(-5,201,-997).setType(Material.REDSTONE_BLOCK);
                        world.getBlockAt(-9,201,-995).setType(Material.AIR);
                        gzr.clearSlot(DisplaySlot.SIDEBAR);
                        playersInGame.clear();
                        Bukkit.getScheduler().runTaskLater(this,() -> isGameRunning = false, 1);
                        return;
                    }
                    int[] renAndGuiNumber = getRenAndGuiNumber();
                    int renNumber = renAndGuiNumber[0];
                    int guiNumber = renAndGuiNumber[1];
                    if (renNumber <= 0) {
                        Bukkit.getScheduler().cancelTasks(this);
                        world.getBlockAt(-5,201,-997).setType(Material.REDSTONE_BLOCK);
                        world.getBlockAt(-9,201,-995).setType(Material.AIR);
                        gzr.clearSlot(DisplaySlot.SIDEBAR);
                        playersInGame.clear();
                        Bukkit.getScheduler().runTaskLater(this,() -> isGameRunning = false, 1);
                        return;
                    }
                    if (guiNumber <= 0) {
                        Bukkit.getScheduler().cancelTasks(this);
                        world.getBlockAt(-5,201,-997).setType(Material.REDSTONE_BLOCK);
                        world.getBlockAt(-9,201,-995).setType(Material.AIR);
                        gzr.clearSlot(DisplaySlot.SIDEBAR);
                        playersInGame.clear();
                        Bukkit.getScheduler().runTaskLater(this,() -> isGameRunning = false, 1);
                        return;
                    }
                    switch((int)(time - startTime)) {
                        case 1200:
                        case 2400:
                        case 4800:
                        case 7200:
                            for (Player p : playersInGame) {
                                if (!checkIsRen(p)) {
                                    continue;
                                }
                                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,200,0));
                            }
                            break;
                        default:
                            break;
                    }
                    gzr.getObjective("gzr").getScore("剩余人数").setScore(renNumber);
                    gzr.getObjective("gzr").getScore("剩余时间").setScore((int)((gameTime - (time - startTime)) / 20));
                },1,1);
            }
        }
    }




    public void onEnable() {
        world = Bukkit.getWorld("world");

        locations = new Location[] {
                new Location(world,-8.5,64,-1043.5),
                new Location(world,-38.5,61,-1011.5),
                new Location(world,-56.5,61,-1074.5),
                new Location(world,-16.5,52,-1074.5),
                new Location(world,-54.5,52,-1075.5),
                new Location(world,-65.5,52,-1016.5),
                new Location(world,-7.5,70,-1067.5),
                new Location(world,-50.5,70,-1019.5),
                new Location(world,-66.5,70,-1071.5),
                new Location(world,-31.5,79,-1041.5)};
        diamond = new ItemStack(Material.DIAMOND, 1);
        ItemMeta meta1 = diamond.getItemMeta();
        meta1.setDisplayName("冰冻鬼5秒");
        diamond.setItemMeta(meta1);

        red = new ItemStack(Material.RED_DYE, 1);
        ItemMeta meta2 = red.getItemMeta();
        meta2.setDisplayName("回满血");
        red.setItemMeta(meta2);

        heart = new ItemStack(Material.HEART_OF_THE_SEA, 1);
        ItemMeta meta3 = heart.getItemMeta();
        meta3.setDisplayName("隐身10秒");
        heart.setItemMeta(meta3);

        clock = new ItemStack(Material.CLOCK, 1);
        ItemMeta meta4 = clock.getItemMeta();
        meta4.setDisplayName("延时15秒");
        clock.setItemMeta(meta4);

        emerald = new ItemStack(Material.EMERALD, 1);
        ItemMeta meta5 = emerald.getItemMeta();
        meta5.setDisplayName("人发光5秒");
        emerald.setItemMeta(meta5);

        sugar = new ItemStack(Material.SUGAR, 1);
        ItemMeta meta6 = sugar.getItemMeta();
        meta6.setDisplayName("加速");
        sugar.setItemMeta(meta6);

        Bukkit.getPluginManager().registerEvents(this, this);
        isGameRunning = false;
        gzr = Bukkit.getScoreboardManager().getMainScoreboard();
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        gameTime = 6000;
        playersInGame = new HashSet<>();
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public long getTime(World world) {
        return ((CraftWorld)world).getHandle().worldData.getTime();
    }

    public int[] getRenAndGuiNumber() {
        if (!isGameRunning) {
            return new int[]{0,0};
        }
        int renNumber = 0;
        int guiNumber = 0;
        for (Player p : playersInGame) {
            if (checkIsRen(p)) {
                renNumber += 1;
            } else if (checkIsGui(p)) {
                guiNumber += 1;
            }
        }
        return new int[]{renNumber,guiNumber};
    }

    public boolean checkIsGui(Player p) {
        if (p == null) {
            return false;
        }
        if (!p.isOnline()) {
            return false;
        }
        if (p.getLocation().getY() > 100) {
            return false;
        }
        if (!p.getScoreboardTags().contains("gzr")) {
            return false;
        }
        if (!p.getGameMode().equals(GameMode.ADVENTURE)) {
            return false;
        }
        if (scoreboard.getTeam("gzrR").hasPlayer(p)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIsRen(Player p) {
        if (p == null) {
            return false;
        }
        if (!p.isOnline()) {
            return false;
        }
        if (p.getLocation().getY() > 100) {
            return false;
        }
        if (!p.getScoreboardTags().contains("gzr")) {
            return false;
        }
        if (!p.getGameMode().equals(GameMode.ADVENTURE)) {
            return false;
        }
        if (scoreboard.getTeam("gzrB").hasPlayer(p)) {
            return true;
        } else {
            return false;
        }
    }

}
