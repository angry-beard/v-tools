package com.v.core.base.utils;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author angry_beard
 * @date 2021/8/16 10:21 上午
 */
@Slf4j
public class IpUtil {

    private IpUtil() {
    }

    /**
     * 获得服务器的IP地址
     */
    public static String getLocalIP() {
        String sIP = "";
        InetAddress ip = null;
        try {
            boolean bFindIP = false;
            Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                if (bFindIP) {
                    break;
                }
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration<?> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = (InetAddress) ips.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                        bFindIP = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获得服务器的IP地址异常，detail:", e);
        }
        if (null != ip) {
            sIP = ip.getHostAddress();
        }
        return sIP;
    }

    /**
     * 获得服务器的IP地址(多网卡)
     */
    public static List<String> getLocalIPS() {
        InetAddress ip;
        List<String> ipList = Lists.newArrayList();
        try {
            Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                Enumeration<?> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ip = (InetAddress) ips.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
                        ipList.add(ip.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            log.error("获得服务器的IP地址(多网卡)，detail:", e);
        }
        return ipList;
    }

    /**
     * 获得服务器的mac地址
     */
    public static String getMacId() {
        byte[] mac = null;
        try {
            //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
        } catch (Exception e) {
            log.error("获得服务器的IP地址(多网卡)，detail:", e);
        }
        //下面代码是把mac地址拼装成String
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Objects.requireNonNull(mac).length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        //把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }

    public static void main(String[] args) {
        System.out.println(getLocalIPS());
        System.out.println(getLocalIP());
        System.out.println(getMacId());
    }
}
