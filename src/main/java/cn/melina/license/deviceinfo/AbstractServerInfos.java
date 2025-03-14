package cn.melina.license.deviceinfo;


import cn.melina.license.LicenseCheckModelList;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @AUTHOR:LIUCHAO; 用户获取服务器的硬件信息：
 * @DATE: 2020/12/1 15:39
 */
public abstract class AbstractServerInfos {

    /**
     * 组装需要额外校验的License参数

     */
    public LicenseCheckModelList getServerInfos(){
        LicenseCheckModelList result = new LicenseCheckModelList();

        try {
            result.setIpAddress(this.getIpAddress());
            result.setMacAddress(this.getMacAddress());
            result.setCpuSerial(this.getCPUSerial());
            result.setMainBoardSerial(this.getMainBoardSerial());
        }catch (Exception e){
            System.out.println("获取服务器硬件信息失败");
          e.printStackTrace();
        }

        return result;
    }

    /**
     * 获取IP地址

     */
    protected List getIpAddress() throws Exception{
        List  result=new ArrayList(4);
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
//                            return ip.getHostAddress();
                            result.add(ip.getHostAddress());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return result;
    };

    /**
     * 获取Mac地址

     */
    protected  List getMacAddress() throws Exception{
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        StringBuilder sb = new StringBuilder();
        ArrayList<String> tmpMacList=new ArrayList();
        while(en.hasMoreElements()){
            NetworkInterface iface = en.nextElement();
            List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
            for(InterfaceAddress addr : addrs) {
                InetAddress ip = addr.getAddress();
                NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                if(network==null){continue;}
                byte[] mac = network.getHardwareAddress();
                if(mac==null){continue;}
                sb.delete( 0, sb.length() );
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                tmpMacList.add(sb.toString());
            }
        }
        if(tmpMacList.size()<=0){return tmpMacList;}
        /***去重，别忘了同一个网卡的ipv4,ipv6得到的mac都是一样的，肯定有重复，下面这段代码是。。流式处理***/
        List<String> unique = tmpMacList.stream().distinct().collect(Collectors.toList());
        return unique;
    };

    /**
     * 获取CPU序列号

     */
    protected abstract String getCPUSerial() throws Exception;

    /**
     * 获取主板序列号
     * @return java.lang.String
     */
    protected abstract String getMainBoardSerial() throws Exception;



}