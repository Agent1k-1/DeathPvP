package com.agent1k;

import com.agent1k.action.AbstractAction;
import com.agent1k.action.Action;
import com.agent1k.model.Mode;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.util.*;

public final class DeathPvP extends JavaPlugin implements Listener {
    private YamlConfiguration messages, menus, arenas;
    private final Map<UUID, UUID> parties = new HashMap<>(), invitations = new HashMap<>();
    private final Map<UUID, Mode> queue = new LinkedHashMap<>();
    private final Map<UUID, Battle> battles = new HashMap<>();
    private final Map<UUID, Location> returns = new HashMap<>();
    private final Set<String> occupied = new HashSet<>();
    private final Map<String, Action> actions = new HashMap<>();
    public void onEnable() {
        saveDefaultConfig();
        for (String name : List.of("messages.yml", "arenas.yml", "menus/search.yml")) if (!new File(getDataFolder(), name).exists()) saveResource(name, false);
        load();
        actions.put("CHAT", new AbstractAction() { public void execute(Player p, String v) { p.sendMessage(color(v)); } });
        actions.put("TITLE", (p,v) -> p.sendTitle(color(v), "", 0, 30, 10));
        actions.put("SUBTITLE", (p,v) -> p.sendTitle("", color(v), 0, 30, 10));
        actions.put("ACTIONBAR", (p,v) -> p.sendActionBar(color(v)));
        actions.put("QUEUE", (p,v) -> join(p, Mode.valueOf(v)));
        actions.put("LEAVE", (p,v) -> leave(p.getUniqueId()));
        actions.put("CLOSE", (p,v) -> p.closeInventory());
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::match, 20, 20);
    }
    private void load() {
        reloadConfig();
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        menus = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "menus/search.yml"));
        arenas = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "arenas.yml"));
    }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    private void tell(Player p, String key) { tell(p,key,Map.of()); }
    private void tell(Player p, String key, Map<String,String> vars) {
        ConfigurationSection s = messages.getConfigurationSection(key);
        if (s == null) return;
        String title = s.getString("title", ""), subtitle = s.getString("subtitle", "");
        for (var e : vars.entrySet()) { title=title.replace("%"+e.getKey()+"%",e.getValue()); subtitle=subtitle.replace("%"+e.getKey()+"%",e.getValue()); }
        if (!title.isEmpty() || !subtitle.isEmpty()) p.sendTitle(color(title),color(subtitle),0,25,5);
        for (String type : List.of("chat","actionbar")) if (s.contains(type)) {
            String value=s.getString(type,"");
            for (var e:vars.entrySet()) value=value.replace("%"+e.getKey()+"%",e.getValue());
            actions.get(type.toUpperCase(Locale.ROOT)).execute(p,value);
        }
    }
    private boolean armor(Player p) {
        int count=0;
        for (ItemStack item:p.getInventory().getArmorContents()) if(item!=null && getConfig().getStringList("armor.allowed").stream().anyMatch(a->item.getType().name().startsWith(a+"_"))) count++;
        return count>=getConfig().getInt("armor.minimum-pieces",1);
    }
    private boolean available(Player p) { return p!=null && p.isOnline() && !p.isDead() && p.getGameMode()==GameMode.SURVIVAL && !battles.containsKey(p.getUniqueId()); }
    private List<UUID> group(UUID id,Mode mode) { return mode==Mode.TEAM && parties.containsKey(id) ? List.of(id,parties.get(id)) : List.of(id); }
    private void join(Player p,Mode mode) {
        UUID id=p.getUniqueId();
        if(mode==Mode.TEAM && !parties.containsKey(id)) { tell(p,"party-required");return; }
        for(UUID member:group(id,mode)) {
            Player target=Bukkit.getPlayer(member);
            if(!available(target) || queue.containsKey(member)) { tell(p,"busy");return; }
            if(!armor(target)) { tell(target,"armor");return; }
        }
        for(UUID member:group(id,mode)) { queue.put(member,mode);tell(Bukkit.getPlayer(member),"queued"); }
    }
    private void leave(UUID id) {
        Mode mode=queue.get(id);
        if(mode!=null) for(UUID member:group(id,mode)) {queue.remove(member);Player p=Bukkit.getPlayer(member);if(p!=null)tell(p,"left");}
    }
    private void open(Player p) {
        Menu holder=new Menu();
        holder.inventory=Bukkit.createInventory(holder,Math.max(9,Math.min(54,menus.getInt("size",27)/9*9)),color(menus.getString("title","DeathPvP")));
        ConfigurationSection items=menus.getConfigurationSection("items");
        if(items!=null) for(String key:items.getKeys(false)) {
            ConfigurationSection s=items.getConfigurationSection(key);
            int slot=s.getInt("slot");if(slot<0 || slot>=holder.inventory.getSize())continue;
            Material material=Material.matchMaterial(s.getString("material","STONE"));if(material==null || material.isAir())continue;
            ItemStack item=new ItemStack(material);var meta=item.getItemMeta();meta.setDisplayName(color(s.getString("name"," ")));meta.setLore(s.getStringList("lore").stream().map(this::color).toList());item.setItemMeta(meta);holder.inventory.setItem(slot,item);holder.bindings.put(slot,s.getStringList("actions"));
        }
        p.openInventory(holder.inventory);
    }
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if(command.getName().equalsIgnoreCase("deathpvp")) {
            if(!sender.hasPermission("deathpvp.admin"))return true;
            if(args.length==1 && args[0].equalsIgnoreCase("reload")) {for(Battle b:new HashSet<>(battles.values()))b.finish(false);queue.clear();load();sender.sendMessage(color(messages.getString("reload.chat","Reloaded")));}return true;
        }
        if(!(sender instanceof Player p))return true;
        if(args.length==0){open(p);return true;}
        if(args[0].equalsIgnoreCase("leave")){leave(p.getUniqueId());return true;}
        if(args.length>=2 && args[0].equalsIgnoreCase("team")) {
            UUID id=p.getUniqueId();
            if(args[1].equalsIgnoreCase("leave")){leave(id);UUID other=parties.remove(id);if(other!=null)parties.remove(other);return true;}
            if(args.length==3) {
                Player target=Bukkit.getPlayerExact(args[2]);
                if(!available(p)||!available(target)||target==p||queue.containsKey(id)||queue.containsKey(target.getUniqueId())||parties.containsKey(id)||parties.containsKey(target.getUniqueId())){tell(p,"busy");return true;}
                if(args[1].equalsIgnoreCase("add")){invitations.put(target.getUniqueId(),id);tell(target,"invite",Map.of("player",p.getName()));}
                if(args[1].equalsIgnoreCase("accept") && target.getUniqueId().equals(invitations.get(id))){invitations.remove(id);parties.put(id,target.getUniqueId());parties.put(target.getUniqueId(),id);tell(p,"team");tell(target,"team");}
            }
        }
        return true;
    }
    private Location point(ConfigurationSection s,String key) {
        List<Double> v=s.getDoubleList(key);World world=Bukkit.getWorld(s.getString("world","world"));
        return world==null||v.size()!=3?null:new Location(world,v.get(0),v.get(1),v.get(2));
    }
    private boolean safe(Location l) {return l!=null && l.getWorld().getWorldBorder().isInside(l) && l.getBlock().isPassable() && l.clone().add(0,1,0).getBlock().isPassable() && l.clone().subtract(0,1,0).getBlock().getType().isSolid() && !Set.of(Material.MAGMA_BLOCK,Material.CACTUS,Material.CAMPFIRE,Material.SOUL_CAMPFIRE).contains(l.clone().subtract(0,1,0).getBlock().getType());}
    private Location random() {
        World w=Bukkit.getWorld(getConfig().getString("random.world","world"));if(w==null)return null;
        int radius=Math.max(10,Math.min(100000,getConfig().getInt("random.radius",1000)));
        for(int i=0;i<Math.min(256,getConfig().getInt("random.attempts",64));i++) {
            int x=new Random().nextInt(radius*2)-radius,z=new Random().nextInt(radius*2)-radius;
            Location l=new Location(w,x+.5,w.getHighestBlockYAt(x,z)+1,z+.5);if(safe(l)&&safe(l.clone().add(3,0,0)))return l;
        }return null;
    }
    private void match() {
        for(UUID id:new ArrayList<>(queue.keySet()))if(!available(Bukkit.getPlayer(id)))leave(id);
        for(Mode mode:Mode.values()) {
            List<UUID> ids=queue.entrySet().stream().filter(e->e.getValue()==mode).map(Map.Entry::getKey).toList();
            if(ids.isEmpty())continue;
            List<UUID> a=group(ids.get(0),mode);
            UUID opponent=ids.stream().filter(id->!a.contains(id)).findFirst().orElse(null);if(opponent==null)continue;
            List<UUID> b=group(opponent,mode);if(mode==Mode.TEAM && (a.size()!=2||b.size()!=2))continue;
            String arena=null;Location first=null,second=null;ConfigurationSection section=null;
            if(mode==Mode.NORMAL){first=random();if(first!=null)second=first.clone().add(3,0,0);}
            else {ConfigurationSection root=arenas.getConfigurationSection("arenas");if(root!=null)for(String key:root.getKeys(false)){ConfigurationSection s=root.getConfigurationSection(key);if(s.getBoolean("enabled")&&!occupied.contains(key)&&safe(point(s,"first"))&&safe(point(s,"second"))&&point(s,"min")!=null&&point(s,"max")!=null){arena=key;section=s;first=point(s,"first");second=point(s,"second");break;}}}
            if(first==null||second==null)continue;
            Battle battle=new Battle(a,b,mode,arena,section,first,second);
            if(arena!=null)occupied.add(arena);
            for(UUID id:battle.all){queue.remove(id);battles.put(id,battle);}
            battle.runTaskTimer(this,0,20);
        }
    }
    private void combat(Player p,String key) {for(String cmd:getConfig().getStringList("combat."+key+"-commands"))Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd.replace("%player%",p.getName()));}
    private final class Battle extends BukkitRunnable {
        final List<UUID> a,b,all;final Set<UUID> alive;final Mode mode;final String arena;final ConfigurationSection region;final Location first,second;int time=getConfig().getInt("cooldown",5),tick;boolean active,finished;
        Battle(List<UUID>a,List<UUID>b,Mode mode,String arena,ConfigurationSection region,Location first,Location second){this.a=a;this.b=b;this.mode=mode;this.arena=arena;this.region=region;this.first=first;this.second=second;all=new ArrayList<>(a);all.addAll(b);alive=new HashSet<>(all);}
        public void run(){
            if(finished)return;
            if(!active){
                for(UUID id:all){Player p=Bukkit.getPlayer(id);if(p==null||p.isDead()||!armor(p)){finish(false);return;}}
                if(time>0){for(UUID id:all)tell(Bukkit.getPlayer(id),"countdown",Map.of("time",String.valueOf(time)));time--;return;}
                for(UUID id:all){Player p=Bukkit.getPlayer(id);returns.put(id,p.getLocation());if(!p.teleport(a.contains(id)?first:second)){finish(false);return;}}
                active=true;for(UUID id:all){Player p=Bukkit.getPlayer(id);combat(p,"start");tell(p,"start");}
            }else if(mode!=Mode.NORMAL && ++tick%Math.max(1,getConfig().getInt("combat.refresh-seconds",5))==0)for(UUID id:alive){Player p=Bukkit.getPlayer(id);if(p!=null)combat(p,"start");}
        }
        boolean inside(Location l){if(region==null)return true;Location min=point(region,"min"),max=point(region,"max");return min!=null&&max!=null&&l.getWorld().equals(min.getWorld())&&l.getX()>=Math.min(min.getX(),max.getX())&&l.getX()<=Math.max(min.getX(),max.getX())&&l.getY()>=Math.min(min.getY(),max.getY())&&l.getY()<=Math.max(min.getY(),max.getY())&&l.getZ()>=Math.min(min.getZ(),max.getZ())&&l.getZ()<=Math.max(min.getZ(),max.getZ());}
        void eliminate(UUID id){alive.remove(id);if(a.stream().noneMatch(alive::contains)||b.stream().noneMatch(alive::contains))finish(true);}
        void finish(boolean victory){if(finished)return;finished=true;cancel();if(arena!=null)occupied.remove(arena);for(UUID id:all){battles.remove(id);Player p=Bukkit.getPlayer(id);if(p!=null){combat(p,"end");if(victory&&alive.contains(id))tell(p,"win");if(!victory)tell(p,"cancel");Location back=returns.get(id);if(back!=null&&!p.isDead()&&(mode!=Mode.NORMAL||!victory)){if(p.teleport(back))returns.remove(id);}else if(mode==Mode.NORMAL&&victory&&!p.isDead())returns.remove(id);}}}
    }
    private static final class Menu implements InventoryHolder {Inventory inventory;final Map<Integer,List<String>> bindings=new HashMap<>();public Inventory getInventory(){return inventory;}}
    @EventHandler public void click(InventoryClickEvent e){if(e.getView().getTopInventory().getHolder() instanceof Menu m){e.setCancelled(true);if(e.getWhoClicked() instanceof Player p && e.getRawSlot()>=0 && e.getRawSlot()<m.inventory.getSize())for(String raw:m.bindings.getOrDefault(e.getRawSlot(),List.of())){String[] pair=raw.split(":",2);Action action=actions.get(pair[0].toUpperCase(Locale.ROOT));if(action!=null)try{action.execute(p,pair.length==2?pair[1]:"");}catch(IllegalArgumentException ex){getLogger().warning("Invalid menu action: "+raw);}}}
    @EventHandler public void drag(InventoryDragEvent e){if(e.getView().getTopInventory().getHolder() instanceof Menu)e.setCancelled(true);}
    @EventHandler public void move(PlayerMoveEvent e){Battle b=battles.get(e.getPlayer().getUniqueId());if(b!=null&&b.active&&e.getTo()!=null&&!b.inside(e.getTo()))e.setTo(e.getFrom());}
    @EventHandler public void teleport(PlayerTeleportEvent e){Battle b=battles.get(e.getPlayer().getUniqueId());if(b!=null&&b.active&&b.mode!=Mode.NORMAL)e.setCancelled(true);}
    @EventHandler public void command(PlayerCommandPreprocessEvent e){Battle b=battles.get(e.getPlayer().getUniqueId());if(b!=null&&b.active&&b.mode!=Mode.NORMAL)e.setCancelled(true);}
    @EventHandler public void damage(EntityDamageEvent e){if(e.getEntity() instanceof Player p){Battle b=battles.get(p.getUniqueId());if(b!=null&&!b.active)e.setCancelled(true);}}
    @EventHandler public void attack(EntityDamageByEntityEvent e){Entity source=e.getDamager();if(source instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter)source=shooter;if(source instanceof Player attacker && e.getEntity() instanceof Player victim){Battle a=battles.get(attacker.getUniqueId()),b=battles.get(victim.getUniqueId());if(a!=null||b!=null){if(a!=b || a==null || !a.active || a.a.contains(attacker.getUniqueId())==a.a.contains(victim.getUniqueId()))e.setCancelled(true);}}}
    @EventHandler public void death(PlayerDeathEvent e){leave(e.getEntity().getUniqueId());Battle b=battles.get(e.getEntity().getUniqueId());if(b!=null)b.eliminate(e.getEntity().getUniqueId());}
    @EventHandler public void respawn(PlayerRespawnEvent e){Location l=returns.remove(e.getPlayer().getUniqueId());if(l!=null)e.setRespawnLocation(l);}
    @EventHandler public void quit(PlayerQuitEvent e){UUID id=e.getPlayer().getUniqueId();leave(id);Battle b=battles.get(id);if(b!=null)b.eliminate(id);UUID other=parties.remove(id);if(other!=null)parties.remove(other);invitations.remove(id);invitations.values().removeIf(id::equals);}
    @EventHandler public void login(PlayerJoinEvent e){Location l=returns.remove(e.getPlayer().getUniqueId());if(l!=null)getServer().getScheduler().runTask(this,()->e.getPlayer().teleport(l));}
    public void onDisable(){for(Battle b:new HashSet<>(battles.values()))b.finish(false);queue.clear();}
}
