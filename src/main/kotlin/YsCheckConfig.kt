package me.stceum.autocheck.ys

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import java.util.*

object YsCheckConfig : AutoSavePluginConfig("ys_check_config") {
    var openApi: String by value()
    var appVersion : String by value();
    var clientType : String by value();
    var acceptEncoding : String by value();
    var salt : String by value();
    var actId : String by value();
    var getUserGameRolesByCookie : String by value();
    var getBbsSignRewardInfo : String by value();
    var postSignInfo : String by value()
    var ua : String by value()
    var cookies : Map<Long, Map<String, String>> by value()
}

object SendMessageConfig : AutoSavePluginConfig("send_message_config") {
    var targetGroups : MutableList<Long> by value(mutableListOf(-1L))
    var botQq : Long by value()
    var checkTime : String by value("06:00:00")
}