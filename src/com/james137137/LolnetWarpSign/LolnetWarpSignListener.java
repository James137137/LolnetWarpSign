/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LolnetWarpSign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author James
 */
class LolnetWarpSignListener implements Listener {

    private LolnetWarpSign LolnetWarpSign;
    private static File[] fileList;
    static final Logger log = Logger.getLogger("Minecraft");
    Economy economy;
    FileConfiguration config;
    private boolean griefPreventionEnable = false;

    public LolnetWarpSignListener(LolnetWarpSign aThis, Economy economy) {
        this.LolnetWarpSign = aThis;
        this.economy = economy;
        config = aThis.getConfig();
        if (aThis.getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
            griefPreventionEnable = true;
        }
    }

    @EventHandler
    public void onPlayerBreakSign(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer().hasPermission("LolnetWarpSign.BreakWarp.bypass")) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        int mat = block.getTypeId();
        if ((mat == Material.SIGN_POST.getId()) || (mat == Material.WALL_SIGN.getId())) {
            Sign lolnetSign = (Sign) block.getState();
            String Line3 = lolnetSign.getLine(3);
            if (Line3.length() <= 2) {
                return;
            }
            if (!Line3.equalsIgnoreCase(ChatColor.GREEN + player.getName()) && Line3.substring(0, 2).equals("" + ChatColor.GREEN)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().contains("setwarp") || event.getMessage().contains("createwarp")) {
            Player player = event.getPlayer();
            if (event.isCancelled() || player.hasPermission("LolnetWarpSign.SetWarp.bypass") || !player.hasPermission("essentials.setwarp")) {
                return;
            }

            String message = event.getMessage();


            String Command = "";
            String warpName = "";
            for (int i = 1; i < message.length(); i++) {
                if (message.charAt(i) == ' ') {
                    warpName = message.substring(i + 1, message.length());
                    break;
                }
                Command += message.charAt(i);
            }

            if (Command.equalsIgnoreCase("setwarp") || Command.equalsIgnoreCase("createwarp")) {


                if (warpName.length() < 3) {
                    player.sendMessage(ChatColor.RED + "Invaild warp name. it must have a minium of 3 charaters");
                    event.setCancelled(true);
                    return;
                }
                if (invalidWarpName(warpName)) {
                    player.sendMessage(ChatColor.RED + "Invaild warp name.");
                    event.setCancelled(true);
                    return;
                }
                if (griefPreventionEnable) {
                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
                    if (claim == null) {
                        player.sendMessage(ChatColor.RED + "You must be in a claim to /setwarp");
                        event.setCancelled(true);
                        return;
                    } else if (claim.allowBuild(player) != null) {
                        player.sendMessage(ChatColor.RED + "You must have permission to build in this claim to /setwarp");
                        event.setCancelled(true);
                        return;
                    }
                }




                if (economy.getBalance(player.getName()) >= config.getInt("NewWarpCost")) {

                    fileList = new File("plugins/Essentials/warps").listFiles();
                    for (int i = 0; i < fileList.length; i++) {
                        if (fileList[i].getName().equalsIgnoreCase(warpName + ".yml")) {
                            player.sendMessage(ChatColor.DARK_RED + "Warp already exist");
                            event.setCancelled(true);
                            return;
                        }
                    }
                    

                    if (warpName.length() > 15) {
                        player.sendMessage(ChatColor.RED + "Warp Name is too long please use a shorter name");
                        event.setCancelled(true);
                        return;
                    }
                    log.log(Level.INFO, "{0} has set a new warp called: {1} at location: {2},{3},{4}",
                            new Object[]{player.getName(), warpName, player.getLocation().getBlockX(),
                        player.getLocation().getBlockY(), player.getLocation().getBlockZ()});
                    economy.withdrawPlayer(player.getName(), config.getInt("NewWarpCost"));
                    player.sendMessage(ChatColor.GREEN + "$" + config.getInt("NewWarpCost") + " has been taken off you.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "you need $" + config.getInt("NewWarpCost"));
                event.setCancelled(true);
                return;
            }

        }


    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        int mat = block.getTypeId();
        if ((mat == Material.SIGN_POST.getId()) || (mat == Material.WALL_SIGN.getId())) {
            Sign lolnetSign = (Sign) block.getState();
            if ((lolnetSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[MyWarp]") || lolnetSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[PrivateWarp]")) && player.hasPermission("essentials.signs.use.warp")) {
                if (lolnetSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[MyWarp]")) {
                    LolnetWarpSign.TeleportPlayer(player, lolnetSign.getLine(1).toLowerCase(), mat);
                } else {
                    String Line3 = lolnetSign.getLine(3);
                    if (Line3.equalsIgnoreCase(ChatColor.GREEN + player.getName()) || player.hasPermission("LolnetWarpSign.WarpSign.bypass")) {
                        LolnetWarpSign.TeleportPlayer(player, lolnetSign.getLine(1).toLowerCase(), mat);
                    } else {
                        player.sendMessage(ChatColor.RED + "This is not your Warp Sign");
                    }
                }

                return;
            }


            if ((lolnetSign.getLine(0).equalsIgnoreCase("[myWarp]") || lolnetSign.getLine(0).equalsIgnoreCase("[Warp]")
                    || lolnetSign.getLine(0).equalsIgnoreCase("[PrivateWarp]"))
                    && player.hasPermission("LolnetWarpSign.activate")) {

                if (griefPreventionEnable && !player.hasPermission("LolnetWarpSign.WarpSign.bypass")) {
                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(lolnetSign.getLocation(), true, null);
                    if (claim == null) {
                        player.sendMessage(ChatColor.RED + "that sign must be in a claim to activate that sign");
                        event.setCancelled(true);
                        return;
                    } else if (claim.allowBuild(player) != null) {
                        player.sendMessage(ChatColor.RED + "You must have permission to build in this claim to activate that sign");
                        event.setCancelled(true);
                        return;
                    }
                }


                if (economy.getBalance(player.getName()) >= config.getInt("WarpSignCost")) {
                    try {
                        BufferedReader in = new BufferedReader(new FileReader("plugins/Essentials/warps/" + lolnetSign.getLine(1).toLowerCase() + ".yml"));
                        String WarpName = lolnetSign.getLine(1).toLowerCase();
                        if (WarpName.length() < 1) {
                            player.sendMessage(ChatColor.DARK_RED + "Warp does not exist");
                            return;
                        }
                        if (lolnetSign.getLine(0).equalsIgnoreCase("[PrivateWarp]")) {
                            lolnetSign.setLine(0, ChatColor.DARK_BLUE + "[PrivateWarp]");
                        } else {
                            lolnetSign.setLine(0, ChatColor.DARK_BLUE + "[MyWarp]");
                        }
                        lolnetSign.setLine(3, ChatColor.GREEN + player.getName());
                        lolnetSign.update();
                        if (!player.hasPermission("LolnetWarpSign.WarpSign.bypass")) {

                            economy.withdrawPlayer(player.getName(), config.getInt("WarpSignCost"));
                            player.sendMessage(ChatColor.GREEN + "$" + config.getInt("WarpSignCost") + " has been taken off you.");
                        }

                    } catch (FileNotFoundException ex) {
                        player.sendMessage(ChatColor.DARK_RED + "Warp does not exist");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "you need $" + config.getInt("WarpSignCost"));
                }


            }

        }
    }
    
    private boolean invalidWarpName(String warpName)
            {
                boolean success = false;
                Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                Pattern u = Pattern.compile("_");
                if ((p.matcher(warpName).find()) && (!u.matcher(warpName).find()))
                {
                    return true;
                }
                
                return false;
            }
}
