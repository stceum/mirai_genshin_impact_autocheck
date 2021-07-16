package me.stceum.autocheck.ys;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class GenshinImpactClient {
    private String openApi;
    private String appVersion;
    private String clientType;
    private String acceptEncoding;
    private String salt;
    private String actId;
    private String getUserGameRolesByCookie;
    private String getBbsSignRewardInfo;
    private String postSignInfo;
    private String ua;
    private String cookie;
    private String dS;
    private String xRpcDeviceId;
    private String xRpcClientType;
    private String xRpcAppVersion;
    private Map<String, String> parameters;
    private UserInfo userInfo;
    private Vector<Boolean> checkStatus;

    public GenshinImpactClient(String openApi, String appVersion, String clientType, String acceptEncoding,
                               String salt, String actId, String getUserGameRolesByCookie, String getBbsSignRewardInfo,
                               String postSignInfo, String ua, String cookie) {
        this.openApi = openApi;
        this.appVersion = appVersion;
        this.clientType = clientType;
        this.acceptEncoding = acceptEncoding;
        this.salt = salt;
        this.actId = actId;
        this.getUserGameRolesByCookie = getUserGameRolesByCookie;
        this.getBbsSignRewardInfo = getBbsSignRewardInfo;
        this.postSignInfo = postSignInfo;
        this.ua = ua;
        this.cookie = cookie;
        init();
    }

    private void init() {
        String time = Long.toString(System.currentTimeMillis()/1000);
        String stringRandom = randomString(6).toLowerCase();
        String stringMd5 = getMd5("salt=" + salt + "&t=" + time + "&r=" + stringRandom);
        dS = time + "," + stringRandom + "," + stringMd5;
        xRpcDeviceId = UUID.randomUUID().toString();
        xRpcClientType = clientType;
        xRpcAppVersion = appVersion;
        parameters = new HashMap<>();
        parameters.put("Cookie", cookie);
        parameters.put("Accept-Encoding", acceptEncoding);
        parameters.put("User-Agent", ua);
        parameters.put("x-rpc-device_id", xRpcDeviceId);
        parameters.put("x-rpc-client_type", clientType);
        parameters.put("x-rpc-app_version", appVersion);
        parameters.put("DS", dS);
    }

    public String queryUserData() {
        String requestData = getRequest(openApi , getUserGameRolesByCookie, parameters);
        JsonElement element = JsonParser.parseString(requestData);
        JsonObject root = element.getAsJsonObject();
        JsonObject data = root.getAsJsonObject("data");
        userInfo = new Gson().fromJson(data, UserInfo.class);
        return requestData;
    }

    public Vector<Boolean> getCheckStatus() {
        if (userInfo == null) {
            queryUserData();
        }
        checkStatus = new Vector<Boolean>();
        for (UserInfo.UserInfoData userInfoData: userInfo.list) {
            String checkStatusRespond = getRequest(openApi, getBbsSignRewardInfo + "act_id=" + actId +
                    "&region=" + userInfoData.region + "&uid=" + userInfoData.game_uid, parameters);
            JsonElement element = JsonParser.parseString(checkStatusRespond);
            JsonObject root = element.getAsJsonObject();
            JsonObject data = root.getAsJsonObject("data");
            CheckData checkData = new Gson().fromJson(data, CheckData.class);
//            System.out.println(checkData.is_sign);
            checkStatus.add(checkData.is_sign);
        }
        return checkStatus;
    }

    public boolean checkIfNotChecked() {
        if (checkStatus == null) {
            getCheckStatus();
        }
        for (int i = 0; i < checkStatus.size(); i++) {
            if (!checkStatus.get(i)) {
                JsonData post = new JsonData(actId, userInfo.list.get(i).region, userInfo.list.get(i).game_uid);
                postRequest(openApi + postSignInfo, new Gson().toJson(post), parameters);
            }
        }
        boolean allChecked = true;
        for (boolean each : getCheckStatus()) {
            allChecked = each && allChecked;
        }
        return allChecked;
    }

    public static String randomString(int length) {
        StringBuilder result = new StringBuilder();
        char[] charList = {
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(charList[random.nextInt(62)]);
        }
        return result.toString();
    }

    public static String getMd5(String originText) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(originText.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte aByte : bytes) {
                builder.append(Integer.toHexString((0x000000FF & aByte) | 0xFFFFFF00).substring(6));
            }

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String postRequest(String requestUrl, String post, Map<String, String> parameters) {
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            // setting variables
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setConnectTimeout(10000);
            httpUrlConnection.setReadTimeout(2000);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);
            for (String key : parameters.keySet()) {
                if (!key.isEmpty()) {
                    httpUrlConnection.setRequestProperty(key, parameters.get(key));
                }
            }
            // set post parameter
            PrintWriter printWriter = new PrintWriter(httpUrlConnection.getOutputStream());
            printWriter.write(post);
            printWriter.flush();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(httpUrlConnection.getInputStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len;
            byte[] arr = new byte[1024];
            while ((len = bufferedInputStream.read(arr)) != -1) {
                byteArrayOutputStream.write(arr, 0, len);
                byteArrayOutputStream.flush();
            }
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toString(StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getRequest(String requestUrl, String get, Map<String, String> parameters) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            URL url = new URL(requestUrl + get);
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            if (parameters!= null && !parameters.isEmpty()) {
                for (String key : parameters.keySet()) {
                    if (!key.isEmpty()) {
                        httpUrlConnection.setRequestProperty(key, parameters.get(key));
                    }
                }
            }
            if (200 == httpUrlConnection.getResponseCode()) {
                InputStream inputStream = httpUrlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    stringBuilder.append(str);
                }
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
                return stringBuilder.toString();
            } else {
                throw new Exception("Connection failed!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public class UserInfo {
        List<UserInfoData> list;
        public class UserInfoData {
            String game_biz;
            String region;
            String game_uid;
            String nickname;
            int level;
            boolean is_chosen;
            String region_name;
            boolean is_official;
        }
    }

    public class JsonData {
        String act_id;
        String region;
        String uid;
        public JsonData(String act_id, String region, String uid) {
            this.act_id = act_id;
            this.region = region;
            this.uid = uid;
        }
    }

    public class CheckData {
        int total_sign_day;
        String today;
        boolean is_sign;
        boolean first_bind;
        boolean is_sub;
        boolean month_first;
    }
}