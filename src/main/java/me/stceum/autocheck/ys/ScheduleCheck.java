package me.stceum.autocheck.ys;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.QuoteReply;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleCheck {
    ScheduleCheck() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay  = getTimeMillis(SendMessageConfig.INSTANCE.getCheckTime()) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;

        executor.scheduleAtFixedRate(
                new CheckTask(),
                initDelay,
                oneDay,
                TimeUnit.MILLISECONDS);
    }

    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    class CheckTask implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Long id : SendMessageConfig.INSTANCE.getTargetGroups()) {
                Bot.getInstance(SendMessageConfig.INSTANCE.getBotQq()).getGroup(id).sendMessage("[Genshin Impact Auto Check]: Started");
            }
            StringBuilder report = new StringBuilder("Schedule Check Result:");
            Map<Long, Map<String, String>> cookies = YsCheckConfig.INSTANCE.getCookies();
            for (Long eachQqId : cookies.keySet()) {
                StringBuilder privateReport = new StringBuilder("Schedule Check Result:");
                privateReport.append("\n").append(Long.toString(eachQqId)).append(" : ");
                report.append("\n").append(Long.toString(eachQqId)).append(" : ");
                Map<String, String> specificCookies;
                specificCookies = cookies.get(eachQqId);
                for (String cookie : specificCookies.values()) {
                    String boolResult = Boolean.toString(
                            new GenshinImpactClient(YsCheckConfig.INSTANCE.getOpenApi(),
                                    YsCheckConfig.INSTANCE.getAppVersion(),
                                    YsCheckConfig.INSTANCE.getClientType(),
                                    YsCheckConfig.INSTANCE.getAcceptEncoding(),
                                    YsCheckConfig.INSTANCE.getSalt(),
                                    YsCheckConfig.INSTANCE.getActId(),
                                    YsCheckConfig.INSTANCE.getGetUserGameRolesByCookie(),
                                    YsCheckConfig.INSTANCE.getGetBbsSignRewardInfo(),
                                    YsCheckConfig.INSTANCE.getPostSignInfo(),
                                    YsCheckConfig.INSTANCE.getUa(),
                                    cookie
                            ).checkIfNotChecked());
                    report.append("\n").append(boolResult);
                    privateReport.append("\n").append(boolResult);
                }
                Bot.getInstance(SendMessageConfig.INSTANCE.getBotQq()).getFriend(eachQqId)
                        .sendMessage(privateReport.toString());
            }
            for (Long id : SendMessageConfig.INSTANCE.getTargetGroups()) {
                Bot.getInstance(SendMessageConfig.INSTANCE.getBotQq()).getGroup(id).sendMessage(report.toString());
            }
        }
    }
}