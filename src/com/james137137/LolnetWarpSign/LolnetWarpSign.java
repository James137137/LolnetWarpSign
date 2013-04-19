/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.james137137.LolnetWarpSign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
/**
 *
 * @author James
 */
public class LolnetWarpSign extends JavaPlugin implements Listener {
    static final Logger log = Logger.getLogger("Minecraft");
    public static Economy economy = null;
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

   @Override
    public void onEnable() {
       
       FileConfiguration config = getConfig();
       config.addDefault("WarpSignCost",1000);
       config.addDefault("NewWarpCost",9000);
       config.options().copyDefaults(true);
       saveConfig();
       
       
       
       if(!setupEconomy())
       {
           log.warning("[LolnetWarpSign] could not setup the Economy");
       }
       String version = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDescription().getVersion();
        log.log(Level.INFO, this.getName() + ":Version {0} enabled", version);
        getServer().getPluginManager().registerEvents(new LolnetWarpSignListener(this, economy), this);
   }
   
   @Override
     public void onDisable() {
        log.log(Level.INFO, "{0}: disabled", this.getName());
    }
   
   
   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        String commandName = command.getName().toLowerCase();
        String[] trimmedArgs = args;


        if (commandName.equalsIgnoreCase("LolnetWarpSign") && sender.hasPermission("LolnetWarpSign.SetPrice")) {
            Player player = (Player) sender;
            if (args.length >=1)
            {
                FileConfiguration config = getConfig();
            if (args[0].equalsIgnoreCase("setWarpSignCost"))
            {
                if (args.length == 1)
                {
                    player.sendMessage("Cost = $" + config.getInt("WarpSignCost"));
                }
                else
                {
                    config.set("WarpSignCost", args[2]);
                    saveConfig();
                    player.sendMessage("Cost is now $" + config.getInt("WarpSignCost"));
                    
                }
            }
            if (args[0].equalsIgnoreCase("setNewWarpCost"))
            {
                if (args.length == 1)
                {
                    player.sendMessage("Cost = $" + config.getInt("NewWarpCost"));
                }
                else
                {
                    config.set("WarpSignCost", args[2]);
                    saveConfig();
                    player.sendMessage("Cost is now $" + config.getInt("NewWarpCost"));
                    
                }
            }
            if (args[0].equalsIgnoreCase("Cleanup"))
            {
                CleanupInvaildWarps();
                sender.sendMessage("cleanup complete");
            }
            
            
            return true;
            } else
            {
                
            }
        }



        return false;
    }
    
    
    public void TeleportPlayer(Player player, String warpName, double Cost)
    {
            BufferedReader in;
            String world;
                double x,y,z;
                float yaw,pitch;
                
        try {
            in = new BufferedReader(new FileReader("plugins/Essentials/warps/"+warpName+".yml"));
            try {
                world = in.readLine().substring(7);
                String tempxyz;;
                tempxyz = in.readLine().substring(3);
                x = Double.parseDouble(tempxyz);
                tempxyz = in.readLine().substring(3);
                y = Double.parseDouble(tempxyz);
                tempxyz = in.readLine().substring(3);
                z = Double.parseDouble(tempxyz);
                tempxyz = in.readLine().substring(5);
                yaw = Float.parseFloat(tempxyz);
                tempxyz = in.readLine().substring(7);
                pitch = Float.parseFloat(tempxyz);
                System.out.println(tempxyz);
                in.close();
                
                
                
            } catch (IOException ex) {
                player.sendMessage(ChatColor.DARK_RED+ "Something is wrong with the warp File");
                try {
                    in.close();
                } catch (IOException ex1) {
                    Logger.getLogger(LolnetWarpSign.class.getName()).log(Level.SEVERE, null, ex1);
                }
                return;
            }
        } catch (FileNotFoundException ex ) {
            player.sendMessage(ChatColor.DARK_RED+ "Warp does not exist");
            return;
        }
        
       List<World> worlds = getServer().getWorlds();
       World teleWorld;
       boolean worldExist = false;
       boolean canTele;
       for (int j = 0; j < worlds.size(); j++) {
           if (worlds.get(j).getName().equalsIgnoreCase(world))
           {
               worldExist = true;
               break;
           }
       }
       if (worldExist)
       {
           teleWorld = getServer().getWorld(world);
           
       }
       else {
           player.sendMessage(ChatColor.DARK_RED+ "Can't find World");
           return;
       }
       if (!player.hasPermission("multiverse.access." + teleWorld.getName()))
       {
           player.sendMessage(ChatColor.DARK_RED + "you don't have permission: "+ "multiverse.access." + teleWorld.getName());
           return;
       }
        
        
        
        player.sendMessage(ChatColor.YELLOW+"Teleporting to: " + warpName);
        Location loc = new Location(teleWorld, x, y, z, yaw, pitch);
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
    
    public void CleanupInvaildWarps() {
        List<World> worlds = getServer().getWorlds();
        BufferedReader in;
        String world;
        boolean worldExist;
        File[] fileList = new File("plugins/Essentials/warps").listFiles();
        for (int i = 0; i < fileList.length; i++) {
            worldExist = false;
            try {
                in = new BufferedReader(new FileReader(fileList[i].getAbsolutePath()));
                try {
                    world = in.readLine().substring(7);
                    in.close();
                    

                    for (int j = 0; j < worlds.size(); j++) {
                        if (worlds.get(j).getName().equalsIgnoreCase(world)) {
                            worldExist = true;
                            break;
                        }
                    }
                    
                    if (!worldExist) {
                        fileList[i].delete();
                        
                    }


                } catch (IOException ex) {
                    Logger.getLogger(LolnetWarpSign.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(LolnetWarpSign.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
