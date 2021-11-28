package tech.yfshadaow;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Tag extends JavaPlugin implements Listener {
    World world;
    List<Player> players;
    long gameTime;

    @EventHandler
    public void onButtonClicked(PlayerInteractEvent pie) {
        if (!pie.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (!pie.getClickedBlock().getType().equals(Material.OAK_BUTTON)) {
            return;
        }
        if (pie.getClickedBlock().getLocation().equals(new Location(world,-5, 203, -1002))) {
            Game game = new Game(this,gameTime);
            game.runTask(this);
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
    public void onEnable() {
        this.world = Bukkit.getWorld("world");
        this.players = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this);
        gameTime = 6000;
        Sign sign = (Sign) world.getBlockAt(-5,204,-1002).getState();
        sign.setLine(2,"当前时间为 " + gameTime/1200 + " 分钟");
        sign.update();
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll((Plugin)this);
        if (players.size() > 0) {
            for (Player p : players) {
                p.teleport(new Location(world, 0.5,89.0,0.5));
                Bukkit.getPluginManager().callEvent(new PlayerChangeGameEvent(p));
            }
        }
    }

}
