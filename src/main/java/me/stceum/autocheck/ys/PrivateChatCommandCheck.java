package me.stceum.autocheck.ys;

import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.message.data.QuoteReply;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class PrivateChatCommandCheck implements Consumer<UserMessageEvent> {
    @Override
    public void accept(UserMessageEvent event) {
        String baseCommand = "ys";
        String helpInfo = "This is the help info.";
        if (event.getMessage().contentToString().startsWith("!" + baseCommand)) {
            String[] commandParameters = event.getMessage().contentToString().split(" ");
            if (!commandParameters[0].equals("!" + baseCommand)) return;
            if (commandParameters.length == 1) {
                event.getSender().sendMessage(helpInfo);
                return;
            }
            // main info
            Map<Long, Map<String, String>> cookies = YsCheckConfig.INSTANCE.getCookies();
            Map<String, String> specificCookies;
            Long qqId = event.getSender().getId();
            String[] savedCookiesList;
            // switch case
            switch (commandParameters[1]) {
                /*========list========*/
                case "list":
                    StringBuilder savedCookies = new StringBuilder("saved cookies: ");
                    savedCookies.append("\n").append(Long.toString(qqId)).append(" : \n");
                    int serialNum = 1;
                    savedCookiesList = cookies.get(qqId).keySet().toArray(new String[0]);
                    Arrays.sort(savedCookiesList, Collections.reverseOrder());
                    for (String time : savedCookiesList) {
                        savedCookies.append(Integer.toString(serialNum)).append(" : ").append(time);
                        serialNum++;
                    }
                    event.getSender().sendMessage(savedCookies.toString());
                    break;
                /*=======add========*/
                case "add":
                    if (commandParameters.length == 2) {
                        event.getSender().sendMessage("Error: More than one parameters needed, while 0 provided");
                        return;
                    }
                    if (!cookies.containsKey(qqId)) {
                        specificCookies = new HashMap<>();
                        cookies.put(qqId, specificCookies);
                    } else {
                        specificCookies = cookies.get(qqId);
                    }
                    int added = 0;
                    String[] cookieList = event.getMessage().contentToString().split("#");
                    for (int i = 1; i < cookieList.length; i++) {
                        if (!specificCookies.values().contains(cookieList[i])) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                            specificCookies.put(simpleDateFormat.format(new Date()), cookieList[i]);
                            added++;
                        }
                    }
                    System.out.println(specificCookies);
                    cookies.put(event.getSender().getId(), specificCookies);
                    YsCheckConfig.INSTANCE.setCookies(cookies);
                    event.getSender().sendMessage(new QuoteReply(event.getSource())
                            .plus(Integer.toString(added) + " success, "
                                    + Integer.toString(cookieList.length - 1 - added) + " existed."));
                    break;
                /*========remove========*/
                case "remove":
                    StringBuilder strReport = new StringBuilder("Report:");
                    cookies = YsCheckConfig.INSTANCE.getCookies();
                    // lack parameters
                    if (commandParameters.length == 2) {
                        event.getSender().sendMessage("Error: More than one parameters needed, while 0 provided");
                        return;
                    }
                    // qq id does not exist
                    if (!cookies.containsKey(qqId)) {
                        event.getSender().sendMessage(Long.toString(qqId) + " does not exist.");
                    } else {
                        specificCookies = cookies.get(qqId);
                        // pointer out of bounds
                        for (int i = 2; i < commandParameters.length; i++) {
                            int tar = Integer.parseInt(commandParameters[i]);
                            savedCookiesList = cookies.get(qqId).keySet().toArray(new String[0]);
                            Arrays.sort(savedCookiesList, Collections.reverseOrder());
                            if (0 < tar && tar <= savedCookiesList.length) {
                                specificCookies.remove(savedCookiesList[tar - 1]);
                                strReport.append("\n").append(Integer.toString(tar)).append(" removed.");
                            } else {
                                strReport.append("\n").append(Integer.toString(tar)).append(" does not exist.");
                            }
                        }
                    }
//                    event.getGroup().sendMessage("Constructing......");
                    break;
                case "remove-all":
                    // qq id does not exist
                    if (!cookies.containsKey(qqId)) {
                        event.getSender().sendMessage(new QuoteReply(event.getSource())
                                .plus(Long.toString(qqId) + " does not exist."));
                    } else {
                        cookies.remove(qqId);
                        YsCheckConfig.INSTANCE.setCookies(cookies);
                        event.getSender().sendMessage(new QuoteReply(event.getSource())
                                .plus(Long.toString(qqId) + " removed."));
                    }
                    break;
                case "run":
                    event.getSender().sendMessage("Starting......");
                    StringBuilder runReport = new StringBuilder("Report:");
                    if (commandParameters.length == 2) {
                        if (cookies.containsKey(qqId)) {
                            specificCookies = cookies.get(qqId);
                            for (String cookie : specificCookies.values()) {
                                runReport.append("\n").append(Boolean.toString(
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
                                        ).checkIfNotChecked()));
                            }
                            event.getSender().sendMessage(runReport.toString());
                        } else {
                            event.getSender().sendMessage(new QuoteReply(event.getSource()).plus("null"));
                        }
                    } else {
                        for (int i = 2; i < commandParameters.length; i++) {
                            Long eachQqId = Long.parseLong(commandParameters[i]);
                            runReport.append("\n").append(commandParameters[i])
                                    .append(" : ");
                            specificCookies = cookies.get(eachQqId);
                            for (String cookie : specificCookies.values()) {
                                runReport.append(Boolean.toString(
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
                                        ).checkIfNotChecked()));
                            }
                        }
                        event.getSender().sendMessage(runReport.toString());
                    }
                    break;
                case "run-all":
                    event.getSender().sendMessage("Starting......");
                    StringBuilder runAllReport = new StringBuilder("Report:");
                    for (Long eachQqId : cookies.keySet()) {
                        runAllReport.append("\n").append(Long.toString(eachQqId)).append(" : ");
                        specificCookies = cookies.get(eachQqId);
                        for (String cookie : specificCookies.values()) {
                            runAllReport.append(Boolean.toString(
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
                                    ).checkIfNotChecked()));
                        }
                    }
                    event.getSender().sendMessage(new QuoteReply(event.getSource()).plus(runAllReport));
                    break;
                case "help":
                default:
                    event.getSender().sendMessage(helpInfo);
                    break;
            }
        }
    }
}
