package me.maplef.plugins;

import me.maplef.MapbotPlugin;
import me.maplef.exceptions.NoPermissionException;
import me.maplef.Main;
import me.maplef.utils.BotOperator;
import me.maplef.utils.CU;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StopServer implements MapbotPlugin {
    static final FileConfiguration config = me.maplef.Main.getPlugin(me.maplef.Main.class).getConfig();
    static final FileConfiguration messages = Main.getPlugin(Main.class).getMessageConfig();
    private static final Long opGroup = config.getLong("op-group");

    private static boolean stopFlag = false;
    private static final String msgStart = messages.getString("message-prefix");

    public static void stopLater(int time){
        stopFlag = true;

        new BukkitRunnable(){
            int countdown = time;

            @Override
            public void run() {
                if(countdown == 0 && stopFlag){
                    Bukkit.getServer().broadcastMessage(CU.t(msgStart + "&c&l服务器正在关闭..."));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stpall catland-shelter");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    cancel(); return;
                }
                if(countdown % 10 == 0 || (countdown <= 10 && countdown > 0))
                    Bukkit.getServer().broadcastMessage(CU.t(msgStart + String.format("&c&l服务器还有 %d 秒关闭", countdown)));

                countdown--;

                if(!stopFlag) cancel();
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
    }

    public static boolean stopCancel(){
        if(stopFlag){
            stopFlag = false;
            Bukkit.getServer().broadcastMessage(CU.t(msgStart + "&e&l关服操作被管理员取消"));
            return true;
        } else {
            return false;
        }
    }

    public static MessageChain stopLater(Long groupID, Long senderID, String[] args) throws Exception{
        if(!Objects.requireNonNull(BotOperator.bot.getGroup(opGroup)).contains(senderID))
            throw new NoPermissionException();
        if(stopFlag) throw new Exception("已存在一个正在进行的关服定时任务");

        stopFlag = true;

        int time = 60;
        if(args.length > 0){
            try{
                time = Integer.parseInt(args[0]);
                if(time < 30) throw new Exception("请给定一个不小于 30 的整数");
            } catch (NumberFormatException e){
                throw new Exception("请给定一个不小于 30 的整数");
            }
        }

        int finalTime = time;
        new BukkitRunnable(){
            int countdown = finalTime;

            @Override
            public void run() {
                if(countdown == 0 && stopFlag){
                    Bukkit.getServer().broadcastMessage(CU.t(msgStart + "&c&l服务器正在关闭..."));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stpall catland-shelter");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                    cancel(); return;
                }
                if(countdown % 10 == 0 || (countdown <= 10 && countdown > 0))
                    Bukkit.getServer().broadcastMessage(CU.t(msgStart + String.format("&c&l服务器还有 %d 秒关闭", countdown)));

                countdown--;

                if(!stopFlag) cancel();
            }
        }.runTaskTimer(Main.getPlugin(Main.class), 0, 20);

        String msg = String.format(" 服务器将在 %d 秒后关闭，如需取消请使用 #stopcancel", time);
        return new MessageChainBuilder().append(new At(senderID)).append(msg).build();
    }

    public static MessageChain stopCancel(Long groupID, Long senderID, String[] args) throws Exception{
        if(!Objects.requireNonNull(BotOperator.bot.getGroup(opGroup)).contains(senderID))
            throw new NoPermissionException();

        if(stopFlag){
            stopFlag = false;
            Bukkit.getServer().broadcastMessage(CU.t(msgStart + "&e&l关服操作被管理员取消"));
            return new MessageChainBuilder().append(new At(senderID)).append(" 关服定时任务已取消").build();
        } else
            return new MessageChainBuilder().append(new At(senderID)).append(" 目前没有正在进行的关服定时任务").build();
    }

    @Override
    public MessageChain onEnable(Long groupID, Long senderID, String[] args) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> register() throws NoSuchMethodException{
        Map<String, Object> info = new HashMap<>();
        Map<String, Method> commands = new HashMap<>();
        Map<String, String> usages = new HashMap<>();

        commands.put("stopserver", StopServer.class.getMethod("stopLater", Long.class, Long.class, String[].class));
        commands.put("关服", StopServer.class.getMethod("stopLater", Long.class, Long.class, String[].class));
        commands.put("stopcancel", StopServer.class.getMethod("stopCancel", Long.class, Long.class, String[].class));
        commands.put("取消关服", StopServer.class.getMethod("stopCancel", Long.class, Long.class, String[].class));

        usages.put("stopserver", "#stopserver [时间/秒] - 设定一个停服倒计时");
        usages.put("关服", "#关服 [时间/秒] - 设定一个停服倒计时");
        usages.put("stopcancel", "#stopcancel - 取消停服倒计时");
        usages.put("取消关服", "#取消关服 - 取消停服倒计时");

        info.put("name", "StopServer");
        info.put("commands", commands);
        info.put("usages", usages);
        info.put("author", "Maplef");
        info.put("description", "进行停服相关操作");
        info.put("version", "1.3");

        return info;
    }
}
