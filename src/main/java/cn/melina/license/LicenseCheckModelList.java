package cn.melina.license;


import java.io.Serializable;
import java.util.List;

/**
 * @AUTHOR:LIUCHAO; 自定义需要校验的License参数
 * @DATE: 2020/12/1 15:33
 */
public class LicenseCheckModelList implements Serializable {

    private static final long serialVersionUID = 8600137500316662317L;

    /**
     * 可被允许的IP地址
     */
    private List<String> ipAddress;


    /**
     * 可被允许的MAC地址
     */
    private List<String> macAddress;



    /**
     * 可被允许的CPU序列号
     */
    private String cpuSerial;

    /**
     * 可被允许的主板序列号
     */
    private String mainBoardSerial;


    public List<String> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<String> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<String> getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(List<String> macAddress) {
        this.macAddress = macAddress;
    }

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


}