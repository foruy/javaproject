package de.blinkt.openvpn.core;

import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
//import java.net.NetworkInterface;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import de.blinkt.openvpn.R;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class VPNClient {
    private static VPNClient instance;

    private VPNClient() {}

    private static JSONObject doPost(Context context, String path, JSONObject body) {
        JSONObject result = null;
        HttpURLConnection conn = null;
        InputStreamReader in = null;
        try {
            //URL url = new URL(R.string.remote_url + path);
            URL url = new URL(context.getResources().getString(R.string.remote_url) + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            if (body != null)
                dos.writeBytes(body.toString());
            dos.flush();
            dos.close();
            in = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = new JSONObject(strBuffer.toString());
            reader.close();

        } catch (Exception e) {
            System.out.println("###########ERROR#####");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

   synchronized public static VPNClient getInstance() {
        if (instance == null) {
            instance = new VPNClient();
        }
        return instance;
    }

    public String login(Context cxt, String username, String password) {
        String user = "";
        //Map<String, String> user = new HashMap<String, String>();
        //android.os.Build.MANUFACTURER 
        System.out.println("######Device start########");
        System.out.println(android.os.Build.MANUFACTURER);
        //android.os.Build.HARDWARE
        System.out.println(android.os.Build.HARDWARE);
        //android.os.Build.DEVICE
        System.out.println(android.os.Build.DEVICE);
        //android.os.Build.DISPLAY
        System.out.println(android.os.Build.DISPLAY);
        System.out.println("######Device end########");
        try {
            JSONObject userObj = new JSONObject();
            JSONObject userInfo = new JSONObject();
            userInfo.put("username", username);
            userInfo.put("password", password);
            userObj.put("userinfo", userInfo);
            JSONObject devInfo = new JSONObject();
            devInfo.put("device_id", getMACAddress(cxt));
            devInfo.put("device_name", android.os.Build.MANUFACTURER);
            devInfo.put("device_type", "android");
            userObj.put("devinfo", devInfo);
            JSONObject response = doPost(cxt, "login", userObj);
            user = response.getString("user_id");
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public NetworkConfig network(Context cxt, String userId) {
        NetworkConfig config = null;
        JSONObject netObj = new JSONObject();
        try {
            netObj.put("user_id", userId);
            JSONObject response = doPost(cxt, "network", netObj);
            JSONArray networks = response.getJSONArray("networks");
            config = new NetworkConfig(response.getString("ca"),
                                       response.getString("cert"),
                                       response.getString("key"));
            for (int i = 0; i < networks.length(); i++) {
                JSONObject item = networks.getJSONObject(i);
                Network net = new Network(item.getString("id"),
                                          item.getString("name"),
                                          item.getString("address"),
                                          item.getInt("port"));
                config.putNetworks(net);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    public List<Device> device(Context cxt, String userId, String netId) {
        List<Device> devices = new ArrayList<Device>();
        JSONObject devObj = new JSONObject();
        try {
            devObj.put("user_id", userId);
            devObj.put("device_id", getMACAddress(cxt));
            String path = String.format("%s/%s", "device", netId);
            JSONObject response = doPost(cxt, path, devObj);
            JSONArray deviceArray = response.getJSONArray("devices");
            for (int i = 0; i < deviceArray.length(); i++) {
                JSONObject item = deviceArray.getJSONObject(i);
                Device device = new Device(item.getString("device_id"),
                                           item.getString("device_name"),
                                           item.getBoolean("device_state"));
                devices.add(device);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return devices;
    }

    public Device update(Context cxt, String userId, String netId) {
        Device device = new Device();
        JSONObject upObj = new JSONObject();
        try {
            upObj.put("user_id", userId);
            upObj.put("device_id", getMACAddress(cxt));
            upObj.put("network_id", netId);
            JSONObject response = doPost(cxt, "update", upObj);
            JSONObject deviceObj = response.getJSONObject("device");
            device.setId(deviceObj.getString("device_id"));
            device.setName(deviceObj.getString("device_name"));
            device.setState(deviceObj.getBoolean("device_state"));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return device;
    }

    public static String getMACAddress(Context cxt) {
        String macaddr = "";
        //Context context = cxt.getApplicationContext();
        WifiManager wifiMgr = (WifiManager) cxt.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        if (null != info) {
            macaddr = info.getMacAddress();
        } else {
            //macaddr = new String(NetworkInterface.getHardwareAddress());
            macaddr = android.os.Build.HARDWARE;
        }
        System.out.println("Mac: " + macaddr);
        macaddr = "d4:85:64:b5:e4:6b";

        return macaddr;
    }

    public String getUserInfo(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getString("UserId", null);
    }

    public void setUserInfo(Context c, String user) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        Editor prefsedit = prefs.edit();
        prefsedit.putString("UserId", user);
        prefsedit.apply();
    }
}
