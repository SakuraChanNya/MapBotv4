package me.maplef.loops;

import me.maplef.plugins.CheckOnlineTime;
import me.maplef.utils.BotOperator;
import me.maplef.utils.DatabaseOperator;
import net.mamoe.mirai.Bot;
import org.bukkit.configuration.file.FileConfiguration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class InnerGroupInvite implements Job {
    final FileConfiguration config = me.maplef.Main.getPlugin(me.maplef.Main.class).getConfig();
    private final Long opGroup = config.getLong("op-group");
    private final Long playerGroup = config.getLong("player-group");
    private final Long innerGroup = config.getLong("inner-player-group");
    final Bot bot = BotOperator.bot;

    @Override
    public void execute(JobExecutionContext context){
        Connection c = DatabaseOperator.c;

        List<String> blacklist = config.getStringList("inner-player-group-auto-manage.invite.blacklist");

        try (Statement stmt = c.createStatement();
             ResultSet res = stmt.executeQuery("SELECT * FROM PLAYER;")){
            while(res.next()){
                String playerName = res.getString("NAME");
                if(blacklist.contains(playerName)) continue;
                long playerQQ = Long.parseLong(res.getString("QQ"));

                int weeklyTime = CheckOnlineTime.check(playerName, 1), totalTime = CheckOnlineTime.check(playerName, 3);

                if(totalTime > 1000 && weeklyTime >= config.getInt("inner-player-group-auto-manage.invite.requirement") && !Objects.requireNonNull(bot.getGroup(innerGroup)).contains(playerQQ)){
                    String inviteGreeting = String.format("你好, %s, 这里是小枫4号\n" +
                            "在日常巡视中，我很高兴看到猫猫大陆又多出了一位像你一样的活跃玩家，感谢你为猫猫大陆作出的卓越贡献\n" +
                            "鉴于你在服务器内的活跃行为，特此邀请你加入猫猫大陆内群，和更多的活跃玩家一起交流\n" +
                            "同时，猫猫大陆管理组也会更重视内群玩家的意见和反馈并及时作出回应\n" +
                            "这是猫猫大陆内群群号: %d\n" +
                            "猫猫大陆内群欢迎你的到来！", playerName, innerGroup);

                    Objects.requireNonNull(Objects.requireNonNull(bot.getGroup(playerGroup)).get(playerQQ)).sendMessage(inviteGreeting);

                    BotOperator.send(opGroup, String.format("已邀请 %s 进入内群\n", playerName));
                    break;
                }
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
