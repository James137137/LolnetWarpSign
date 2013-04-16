/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LolnetWarpSign;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    Economy economy;
    FileConfiguration config; 

    public LolnetWarpSignListener(LolnetWarpSign aThis, Economy economy) {
        this.LolnetWarpSign = aThis;
        this.economy = economy;
        config = aThis.getConfig();
    }

    @EventHandler
    public void onPlayerBreakSign (BlockBreakEvent event)
    {
        
        if (event.isCancelled() || event.getPlayer().hasPermission("LolnetWarpSign.BreakWarp.bypass"))
        {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        int mat = block.getTypeId();
        if ((mat == Material.SIGN_POST.getId()) || (mat == Material.WALL_SIGN.getId())) {
            Sign lolnetSign = (Sign) block.getState();
            if (!lolnetSign.getLine(3).equalsIgnoreCase(ChatColor.GREEN +player.getName()) && lolnetSign.getLine(3).substring(0, 2).equals(""+ChatColor.GREEN))
            {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled() || player.hasPermission("LolnetWarpSign.SetWarp.bypass") || !player.hasPermission("essentials.setwarp")) {
            return;
        }

        String message = event.getMessage();
        
        String Command = "";
        String warpName = "[none]";
        for (int i = 1; i < 10; i++) {
            if (message.charAt(i) == ' ') {
                warpName = message.substring(i + 1, message.length());
                break;
            }
            Command += message.charAt(i);
        }
        
        if (Command.equalsIgnoreCase("setwarp")) {
            if (economy.getBalance(player.getName()) >= config.getInt("NewWarpCost")) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader("plugins/Essentials/warps/" + warpName + ".yml"));
                    player.sendMessage(ChatColor.DARK_RED + "Warp already exist");
                    event.setCancelled(true);
                    return;
                } catch (FileNotFoundException ex) {
                    if (warpName.length() > 15) {
                        player.sendMessage(ChatColor.RED + "Warp Name is too long please use a shorter name");
                        event.setCancelled(true);
                        return;
                    }
                    economy.withdrawPlayer(player.getName(), config.getInt("NewWarpCost"));
                    player.sendMessage(ChatColor.GREEN + "$" + config.getInt("NewWarpCost") + " has been taken off you.");
                }
            } else {
                    player.sendMessage(ChatColor.RED + "you need $" +config.getInt("NewWarpCost"));
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
            if (lolnetSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_BLUE + "[MyWarp]") && player.hasPermission("essentials.signs.use.warp")) {
                LolnetWarpSign.TeleportPlayer(player, lolnetSign.getLine(1).toLowerCase(), mat);
                return;
            }


            if ((lolnetSign.getLine(0).equalsIgnoreCase("[myWarp]") || lolnetSign.getLine(0).equalsIgnoreCase("[Warp]")) 
                    && player.hasPermission("LolnetWarpSign.activate")) {
                
                
                if (economy.getBalance(player.getName()) >= config.getInt("WarpSignCost")) {
                    try {
                        BufferedReader in = new BufferedReader(new FileReader("plugins/Essentials/warps/" + lolnetSign.getLine(1).toLowerCase() + ".yml"));
                        lolnetSign.setLine(0, ChatColor.DARK_BLUE + "[MyWarp]");
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
                    player.sendMessage(ChatColor.RED + "you need $" +config.getInt("WarpSignCost"));
                }


            }

        }
    }
}
