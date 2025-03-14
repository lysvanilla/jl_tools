package cn.melina.license;

import java.io.Serializable;

/**
 * @AUTHOR:LIUCHAO; 自定义需要校验的License参数
 * @DATE: 2020/12/1 15:33
 */
public class LicenseCheckModel implements Serializable {

    private static final long serialVersionUID = 8600137500316662317L;

    /**
     * 可被允许的IP地址
     */
    private String ipAddress;


    /**
     * 可被允许的MAC地址
     */
    private String macAddress;



    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }


    public String getCpuSerial() {
        return cpuSerial;
    }

    public void setCpuSerial(String cpuSerial) {
        this.cpuSerial = cpuSerial;
    }

    public String getMainBoardSerial() {
        return mainBoardSerial;
    }

    public void setMainBoardSerial(String mainBoardSerial) {
        this.mainBoardSerial = mainBoardSerial;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}