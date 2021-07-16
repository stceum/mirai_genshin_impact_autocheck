package me.stceum.autocheck.ys;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.List;
import java.util.Map;

public final class PluginMain extends JavaPlugin {
    public static final PluginMain INSTANCE = new PluginMain(); // 必须 public static, 必须名为 INSTANCE
    private ScheduleCheck scheduleCheck;

    private PluginMain() {
        super(new JvmPluginDescriptionBuilder("me.stceum.autocheck.ys", "1.0-SNAPSHOT")
                .author("stceum")
                .name("ys_autocheck")
                .info("原神自动打卡")
                .build()
        );
    }

    @Override
    public void onEnable() {
        reloadPluginConfig(YsCheckConfig.INSTANCE);
        reloadPluginConfig(SendMessageConfig.INSTANCE);
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, new GroupCommandCheck());
        GlobalEventChannel.INSTANCE.subscribeAlways(UserMessageEvent.class, new PrivateChatCommandCheck());
        scheduleCheck = new ScheduleCheck();
    }
}
