package cn.melina.license;

import cn.hutool.core.date.DateUtil;
import cn.hutool.log.Log;
import cn.melina.license.deviceinfo.AbstractServerInfos;
import cn.melina.license.deviceinfo.LinuxServerInfos;
import cn.melina.license.deviceinfo.WindowsServerInfos;
import cn.sunline.util.BasicInfo;
import de.schlichtherle.license.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * VerifyLicense
 * @author melina
 */
@Slf4j
public class VerifyLicense {
	public static String base_path = BasicInfo.BASE_PATH;
	//common param
	private static String PUBLICALIAS = "publiccert";
	private static String STOREPWD = "sunline123";
	private static String SUBJECT = "sunlinetools";
	private static String licPath = base_path+"sunlinetools.lic";
	private static String pubPath = "publicCerts.store";
	
	public void setParam(String propertiesPath) {
		// 获取参数
		Properties prop = new Properties();
		InputStream in = getClass().getResourceAsStream(propertiesPath);
		try {
			prop.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PUBLICALIAS = prop.getProperty("PUBLICALIAS");
		STOREPWD = prop.getProperty("STOREPWD");
		SUBJECT = prop.getProperty("SUBJECT");
		licPath = base_path+prop.getProperty("licPath");
		pubPath = prop.getProperty("pubPath");
	}
	public boolean verifymain() {
		VerifyLicense vLicense = new VerifyLicense();
		//获取参数
		//vLicense.setParam("./param.properties");
		return verify();
	}

		public boolean verify() {
		/************** 证书使用者端执行 ******************/

		LicenseManager licenseManager = LicenseManagerHolder
				.getLicenseManager(initLicenseParams());
		String expire_date_str = "";

		// 安装证书
		try {
			LicenseContent content = licenseManager.install(new File(licPath));
			expire_date_str = DateUtil.formatDate(content.getNotAfter());// 获取失效日期
			log.info("客户端安装license成功,有效期为:[{}]", expire_date_str);
			//System.out.println(content.getNotAfter());
			//System.out.println("客户端安装证书成功!");
		} catch (Exception e) {
			if ( null !=e.getMessage() && e.getMessage().indexOf("Expired") > -1) {
				log.error("license已过期,请联系管理员获取新的license");
				//System.out.println("license已过期,请联系管理员获取新的license");
			}else{
				log.error("license安装失败");
				//System.out.println("license安装失败!");
			}
			e.printStackTrace();
			//log.error("客户端证书验证失效");
			//System.out.println("客户端证书安装失败!");
			return false;
		}
		// 验证证书
		try {
			LicenseContent licenseContent = licenseManager.verify();
			Boolean result= verifyLicenseCheckModel(licenseContent);
			if (result){
				log.info("license验证成功，有效期为:[{}]！",expire_date_str);
			}else{
				log.error("客户端证书验证IP或者MAC失败");
				return false;
			}
			//System.out.println("客户端验证证书成功!");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("客户端证书验证失效");
			//System.out.println("客户端证书验证失效!");
			return false;
		}
		return true;
	}

	// 返回验证证书需要的参数
	private static LicenseParam initLicenseParams() {
		Preferences preference = Preferences
				.userNodeForPackage(VerifyLicense.class);
		CipherParam cipherParam = new DefaultCipherParam(STOREPWD);

		KeyStoreParam privateStoreParam = new DefaultKeyStoreParam(
				VerifyLicense.class, pubPath, PUBLICALIAS, STOREPWD, null);
		LicenseParam licenseParams = new DefaultLicenseParam(SUBJECT,
				preference, privateStoreParam, cipherParam);
		return licenseParams;
	}

	private Boolean verifyLicenseCheckModel(LicenseContent licenseContent) {
		//证书中的本机信息
		LicenseCheckModel licenseContentExtra = (LicenseCheckModel) licenseContent.getExtra();
		String ipAddress_str = licenseContentExtra.getIpAddress();
		String macAddress_str = licenseContentExtra.getMacAddress();
		if (ipAddress_str.equals("*") && macAddress_str.equals("*")){
			return true;
		}
		//操作系统类型
		String osName = System.getProperty("os.name");
		osName = osName.toLowerCase();
		AbstractServerInfos abstractServerInfos = null;
		//根据不同操作系统类型选择不同的数据获取方法
		if (osName.startsWith("windows")) {
			abstractServerInfos = new WindowsServerInfos();
		} else if (osName.startsWith("linux")) {
			abstractServerInfos = new LinuxServerInfos();
		}else{//其他服务器类型
			abstractServerInfos = new LinuxServerInfos();
		}

		//实际的本机信息
		LicenseCheckModelList serverInfos =  abstractServerInfos.getServerInfos();
		return compareFields(licenseContentExtra,serverInfos);
	}

	private Boolean compareFields(LicenseCheckModel licenseContentExtra, LicenseCheckModelList serverInfos) {
		String ipAddress_str = licenseContentExtra.getIpAddress();
		String macAddress_str = licenseContentExtra.getMacAddress();
		List<String> ipAddress = Arrays.asList(ipAddress_str.split(","));
		List<String> macAddress = Arrays.asList(macAddress_str.split(","));
		String mainBoardSerial = licenseContentExtra.getMainBoardSerial();
		String cpuSerial = licenseContentExtra.getCpuSerial();

		List<String> ipAddress1 = serverInfos.getIpAddress();
		List<String> macAddress1 = serverInfos.getMacAddress();
		String mainBoardSerial1 = serverInfos.getMainBoardSerial();
		String cpuSerial1 = serverInfos.getCpuSerial();
		boolean ipAddressFlag=false;
		boolean macAddressFlag=false;
		if (ipAddress_str.equals("*")){
			ipAddressFlag = true;
		}else{
			for (int i = 0; i < ipAddress.size(); i++) {
				if (ipAddress1.contains(ipAddress.get(i))) {
					ipAddressFlag=true;
					break;
				}
			}
		}

		if (macAddress_str.equals("*")){
			macAddressFlag = true;
		}else {
			for (int i = 0; i < macAddress.size(); i++) {
				if (macAddress1.contains(macAddress.get(i))) {
					macAddressFlag = true;
					break;
				}
			}
		}
		/*for (Object address : ipAddress) {
			for (Object address1 : ipAddress1) {
				if (address.equals(address1)){
					ipAddressFlag=true;
					break;
				}
			}
		}
		for (Object address : macAddress) {
			for (Object address1 : macAddress1) {
				if (address.equals(address1)){
					macAddressFlag=true;
					break;
				}
			}
		}*/
		return ipAddressFlag && macAddressFlag
				//&& mainBoardSerial.equals(mainBoardSerial1)
				//&& cpuSerial.equals(cpuSerial1)
				;
	}
}