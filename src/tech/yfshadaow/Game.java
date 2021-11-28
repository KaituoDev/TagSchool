package tech.yfshadaow;


import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class Game extends BukkitRunnable implements Listener {
    World world;
    Scoreboard scoreboard;
    Scoreboard tag;
    Tag plugin;
    List<Player> players;
    List<Player> humans;
    List<Player> devils;
    Random random;
    List<Integer> taskIds;
    long startTime;
    long gameTime;
    Team team;
    ItemStack diamond; ItemStack red; ItemStack heart; ItemStack clock; ItemStack emerald; ItemStack sugar;
    Location[] locations;

    public Game(Tag plugin, long gameTime) {
        this.plugin = plugin;
        this.players = plugin.players;
        this.humans = new ArrayList<>();
        this.devils = new ArrayList<>();
        this.random = new Random();
        this.taskIds = new ArrayList<>();
        this.world = Bukkit.getWorld("world");
        this.gameTime = gameTime;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.tag = Bukkit.getScoreboardManager().getNewScoreboard();
        tag.registerNewObjective("tag","dummy","鬼抓人");
        tag.getObjective("tag").setDisplaySlot(DisplaySlot.SIDEBAR);
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
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent pde) {
        if (!players.contains(pde.getEntity())) {
            return;
        }
        devils.remove(pde.getEntity());
        humans.remove(pde.getEntity());
        for (Player p: players) {
            p.sendMessage("§f" + pde.getEntity().getName() + " §c的灵魂被收割了！");
        }
    }
    @EventHandler
    public void freezeGui(EntityDamageByEntityEvent edbee) {
        if (!(edbee.getDamager() instanceof Player)) {
            return;
        }
        if (!(edbee.getEntity() instanceof Player)) {
            return;
        }
        if (humans.contains(edbee.getDamager())) {
            edbee.setCancelled(true);
        }
        if (devils.contains(edbee.getDamager()) && humans.contains(edbee.getEntity())) {
            ((Player)edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,40,254,false,false));
            ((Player)edbee.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.JUMP,40,190,false,false));
        }
    }
    @EventHandler
    public void cancelPickup (PlayerPickupItemEvent ppie) {
        Player p = ppie.getPlayer();
        if (humans.contains(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.CLOCK)
                    || ppie.getItem().getItemStack().getType().equals(Material.EMERALD)) {
                ppie.setCancelled(true);
            }
        } else if (devils.contains(p)) {
            if (ppie.getItem().getItemStack().getType().equals(Material.DIAMOND)
                    || ppie.getItem().getItemStack().getType().equals(Material.RED_DYE)
                    || ppie.getItem().getItemStack().getType().equals(Material.HEART_OF_THE_SEA)) {
                ppie.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        if (!pie.getAction().equals(Action.RIGHT_CLICK_AIR) && !pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {return;}// 不是右键
        if (pie.getItem() == null) {return;}//没有物品
        if (pie.getItem().getItemMeta() == null) {return;}//没有meta
        Player executor = pie.getPlayer();
        if (!(players.contains(executor))) {return;}//不在gzr里
        //这里开始添加内容

        switch (pie.getItem().getItemMeta().getDisplayName()) {
            case "冰冻鬼5秒" :
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b身边的鬼被冰冻5秒！");
                for (Player p : devils) {
                    if (p.getLocation().distance(executor.getLocation()) < 6) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,100,254,false,false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,100,190,false,false));
                    }
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
                for (Player p : players) {
                    p.sendMessage("§b所有人发光5秒！");
                    if (humans.contains(p)) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,0,false,false));
                    }
                }
                break;
            case "加速":
                pie.getItem().setAmount(pie.getItem().getAmount() - 1);
                executor.sendMessage("§b获得加速！");
                executor.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100,0,false,false));
                break;
        }
    }
    @EventHandler
    public void preventRegen(EntityRegainHealthEvent erhe) {
        if (!(erhe.getEntity() instanceof Player)) {
            return;
        }
        if (!(players.contains(erhe.getEntity()))) {
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
    public void onPlayerChangeGame(PlayerChangeGameEvent pcge) {
        players.remove(pcge.getPlayer());
        humans.remove(pcge.getPlayer());
        devils.remove(pcge.getPlayer());
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent pqe) {
        players.remove(pqe.getPlayer());
        humans.remove(pqe.getPlayer());
        devils.remove(pqe.getPlayer());
        pqe.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld("world");
        team = tag.registerNewTeam("tag");
        team.setNameTagVisibility(NameTagVisibility.NEVER);
        team.setCanSeeFriendlyInvisibles(false);
        team.setAllowFriendlyFire(true);
        for (Entity e :world.getNearbyEntities(new Location(world, -5,202,-1008),10,10,10) ) {
            if (e instanceof Player) {
                Player p = (Player) e;
                if (scoreboard.getTeam("tagR").hasPlayer(p)) {
                    devils.add(p);
                    players.add(p);
                    team.addPlayer(p);
                } else if (scoreboard.getTeam("tagB").hasPlayer(p)) {
                    humans.add(p);
                    players.add(p);
                    team.addPlayer(p);
                }
            }
        }
        if (players.size() < 2) {
            for (Player p : players) {
                p.sendMessage("§c至少需要2人才能开始游戏！");
            }
            players.clear();
            humans.clear();
            team.unregister();

        } else if (humans.size() == 0) {
            for (Player p : players) {
                p.sendMessage("§c至少需要1个人类才能开始游戏！");
            }
            players.clear();
            humans.clear();
            team.unregister();
        } else if (devils.size() == 0) {
            for (Player p : players) {
                p.sendMessage("§c至少需要1个鬼才能开始游戏！");
            }
            players.clear();
            humans.clear();
            team.unregister();
        } else {
            for (int i = -9; i <= -1; i++) {
                world.getBlockAt(i, 199,-1003).setType(Material.AIR);
            }
            startTime = getTime(world);
            world.getBlockAt(-5,203,-1002).setType(Material.AIR);
            Bukkit.getScheduler().runTask(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a游戏还有 5 秒开始",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                    p.getInventory().clear();
                }
            });
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a游戏还有 4 秒开始",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },20);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a游戏还有 3 秒开始",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },40);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a游戏还有 2 秒开始",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },60);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a游戏还有 1 秒开始",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },80);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.getPluginManager().registerEvents(this,plugin);
                for (Player p : humans) {
                    p.teleport(new Location(world, -26,52,-1044));
                }
                for (Player p: players) {
                    p.setScoreboard(tag);
                    p.sendTitle("§b鬼将在20秒后现身！",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,2f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,999999,0,false,false));
                }

            }, 100);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a5",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },400);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a4",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },420);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a3",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },440);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a2",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },460);
            Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                for (Player p : players) {
                    p.sendTitle("§a1",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,1f);
                }
            },480);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Block block = world.getBlockAt(-10,204,-1007);
                block.setType(Material.OAK_BUTTON);
                BlockData data = block.getBlockData().clone();
                ((Directional)data).setFacing(BlockFace.EAST);
                block.setBlockData(data);
                for (Player p: devils) {
                    p.teleport(new Location(world, -26,52,-1044));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,999999,4,false,false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,999999,1,false,false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,999999,0,false,false));
                    p.getInventory().setItem(EquipmentSlot.HEAD,new ItemStack(Material.SKELETON_SKULL));
                }
                for (Player p : players) {
                    p.sendTitle("§e游戏开始！",null,2,16,2);
                    p.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_HARP,1f,2f);
                }

            }, 500);

            taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                for (Player p: players) {
                    if (devils.contains(p)) {
                        for (Player victim : players) {
                            if (humans.contains(victim)) {
                                if (p.getLocation().distance(victim.getLocation()) < 10) {
                                    victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                    Bukkit.getScheduler().runTaskLater(plugin,() -> {
                                        victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                    }, 3);
                                }
                                if (p.getLocation().distance(victim.getLocation()) < 5) {
                                    Bukkit.getScheduler().runTaskLater(plugin,() -> {
                                        victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                    }, 10);
                                    Bukkit.getScheduler().runTaskLater(plugin,() -> {
                                        victim.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASEDRUM,SoundCategory.BLOCKS,2f,0f);
                                    }, 13);
                                }

                            }
                        }
                    }
                }
            },500,20));

            taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                for (Player p: players) {
                    p.sendMessage("§a道具已刷新！");
                }
                for (Location loc : locations) {
                    double spawnChance = random.nextDouble();
                    if (spawnChance < 0.5) {
                        double spawnNo = random.nextDouble();
                        if (spawnNo < (1f/36*10)) {
                            world.dropItem(loc,diamond);
                        } else if (spawnNo < 1f/36*20) {
                            world.dropItem(loc,red);
                        } else if (spawnNo < 1f/36*25) {
                            world.dropItem(loc,clock);
                        } else if (spawnNo < 1f/36*28) {
                            world.dropItem(loc,emerald);
                        } else if (spawnNo < 1f/36*33) {
                            world.dropItem(loc,sugar);
                        } else {
                            world.dropItem(loc,heart);
                        }
                    }
                }
            },1100,1200));


            taskIds.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->{
                long time = getTime(world);
                if (time - startTime > gameTime) {
                    List<Player> humansCopy = new ArrayList<>(humans);
                    List<Player> playersCopy = new ArrayList<>(players);
                    for (Player p : humansCopy) {
                        spawnFirework(p.getLocation());
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },8);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },16);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },24);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },32);
                    }
                    for (Player p : playersCopy) {
                        p.sendTitle("§e时间到，人类获胜！",null,5,50, 5);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.teleport(new Location(world,-5,202,-1008));
                            Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p));
                        },100);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                        for (int i = -9; i <= -1; i++) {
                            world.getBlockAt(i, 199,-1003).setType(Material.REDSTONE_BLOCK);
                        }
                        for (Entity e: world.getNearbyEntities(new Location(world, 0,128,-1000),200,200,200)) {
                            if (e instanceof Item) {
                                e.remove();
                            }
                        }
                        world.getBlockAt(-10,204,-1007).setType(Material.AIR);
                        Block block = world.getBlockAt(-5,203,-1002);
                        block.setType(Material.OAK_BUTTON);
                        BlockData data = block.getBlockData().clone();
                        ((Directional)data).setFacing(BlockFace.NORTH);
                        block.setBlockData(data);
                        HandlerList.unregisterAll(this);
                    },100);
                    players.clear();
                    humans.clear();
                    devils.clear();
                    team.unregister();
                    List<Integer> taskIdsCopy = new ArrayList<>(taskIds);
                    taskIds.clear();
                    for (int i : taskIdsCopy) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                    return;
                }
                if (humans.size() <= 0) {
                    List<Player> devilsCopy = new ArrayList<>(devils);
                    List<Player> playersCopy = new ArrayList<>(players);
                    for (Player p : devilsCopy) {
                        spawnFirework(p.getLocation());
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },8);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },16);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },24);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },32);
                    }
                    for (Player p : playersCopy) {
                        p.sendTitle("§e无人幸存，鬼获胜！",null,5,50, 5);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.teleport(new Location(world,-5,202,-1008));
                            Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p));
                        },100);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                        for (int i = -9; i <= -1; i++) {
                            world.getBlockAt(i, 199,-1003).setType(Material.REDSTONE_BLOCK);
                        }
                        for (Entity e: world.getNearbyEntities(new Location(world, 0,128,-1000),200,200,200)) {
                            if (e instanceof Item) {
                                e.remove();
                            }
                        }
                        world.getBlockAt(-10,204,-1007).setType(Material.AIR);
                        Block block = world.getBlockAt(-5,203,-1002);
                        block.setType(Material.OAK_BUTTON);
                        BlockData data = block.getBlockData().clone();
                        ((Directional)data).setFacing(BlockFace.NORTH);
                        block.setBlockData(data);
                        HandlerList.unregisterAll(this);
                    },100);
                    players.clear();
                    humans.clear();
                    devils.clear();
                    team.unregister();
                    List<Integer> taskIdsCopy = new ArrayList<>(taskIds);
                    taskIds.clear();
                    for (int i : taskIdsCopy) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                    return;
                }
                if (devils.size() <= 0) {
                    List<Player> humansCopy = new ArrayList<>(humans);
                    List<Player> playersCopy = new ArrayList<>(players);
                    for (Player p : humansCopy) {
                        spawnFirework(p.getLocation());
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },8);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },16);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },24);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            spawnFirework(p.getLocation());
                        },32);
                    }
                    for (Player p : playersCopy) {
                        p.sendTitle("§e鬼不复存在，人类获胜！",null,5,50, 5);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            p.teleport(new Location(world,-5,202,-1008));
                            Bukkit.getPluginManager().callEvent(new PlayerEndGameEvent(p));
                        },100);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, ()-> {
                        for (int i = -9; i <= -1; i++) {
                            world.getBlockAt(i, 199,-1003).setType(Material.REDSTONE_BLOCK);
                        }
                        for (Entity e: world.getNearbyEntities(new Location(world, 0,128,-1000),200,200,200)) {
                            if (e instanceof Item) {
                                e.remove();
                            }
                        }
                        world.getBlockAt(-10,204,-1007).setType(Material.AIR);
                        Block block = world.getBlockAt(-5,203,-1002);
                        block.setType(Material.OAK_BUTTON);
                        BlockData data = block.getBlockData().clone();
                        ((Directional)data).setFacing(BlockFace.NORTH);
                        block.setBlockData(data);
                        HandlerList.unregisterAll(this);
                    },100);
                    players.clear();
                    humans.clear();
                    devils.clear();
                    team.unregister();
                    List<Integer> taskIdsCopy = new ArrayList<>(taskIds);
                    taskIds.clear();
                    for (int i : taskIdsCopy) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                    return;
                }
                switch((int)(time - startTime)) {
                    case 1200:
                    case 2400:
                    case 4800:
                    case 7200:
                        for (Player p : humans) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,200,0,false,false));
                        }
                        break;
                    default:
                        break;
                }
                tag.getObjective("tag").getScore("剩余人数").setScore(humans.size());
                tag.getObjective("tag").getScore("剩余时间").setScore((int)((gameTime - (time - startTime)) / 20));
            },500,1));
        }
    }
    public static void spawnFirework(Location location){
        Location loc = location;
        loc.setY(loc.getY() + 0.9);
        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.LIME).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }
    public long getTime(World world) {
        return ((CraftWorld)world).getHandle().worldData.getTime();
    }

}