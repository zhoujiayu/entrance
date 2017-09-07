package com.ytsp.entrance.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ytsp.db.enums.MobileTypeEnum;
import com.ytsp.entrance.system.IConstants;



public class URLParse {
	 	private String host;
	    private String port;
	    private String part;
	    private static long index;
//	    private static String hostDilianMp4 = "videob.ikan.cn";
	    private static String hostYunduanMp4 = "videof.ikan.cn";
	    //TODO 新的CDN域名
	    private static String newHostYunduanMp4 = "videoxy1.ikan.cn";
	    
	    private static final String CDNKey = "EdGVYYjdvCgDfczh";
	    
	    private static String hostHuhangMp4 = "videod.ikan.cn";
		public static void main(String[] args) throws Exception {
			System.err.println(new Date(1423219106000l));
			//http://videof.ikan.cn/20150423/530F58A8-A096-132D-E88F-18FF6210A150-720p.mp4
			System.err.println(makeURL("Disk01_6000g/20151210/9FC66A7D-AD71-9C6B-25F5-DEB43722F2EC-720p.mp4"));
			System.out.println(newMakeURL(newHostYunduanMp4,"Disk01_6000g/20151210/9FC66A7D-AD71-9C6B-25F5-DEB43722F2EC-720p.mp4"));
		}
		
		
		/**
		 * 新CDN云端网络防盗链URL
		 * @param host
		 * @param uri
		 * @return
		 * @throws Exception
		 */
		private static String newMakeURL(String host,String uri) throws Exception{
			long time = System.currentTimeMillis()/1000+60;
			long timestamp = System.currentTimeMillis()/1000;
			String timeS = Long.toHexString(time);
			String urlPath = MD5.code("yunduanwanx6588"+"/"+uri+timeS)+"/"+timeS+"/"+uri;
			return "http://"+host+"/"+urlPath+"?sign="+MD5.code("/"+urlPath+"|"+timestamp+"|"+CDNKey)+"&timestamp="+timestamp;
		}
		
		/**
		 * 云端网络防盗链URL
		 * @param host
		 * @param uri
		 * @return
		 * @throws Exception
		 */
		private static String makeURL1(String host,String uri) throws Exception{
			long time = System.currentTimeMillis()/1000+3600*24*7;
			String timeS = Long.toHexString(time);
			return "http://"+host+"/"+MD5.code("yunduanwanx6588"+"/"+uri+timeS)+"/"+timeS+"/"+uri;
		}
		
		public static synchronized String makeURL(String uri) throws Exception{
//			if(++index%2==0)
//				return makeURL1( hostYunduanMp4, uri);
//			else 
//				return makeURL1( hostHuhangMp4, uri);
			return makeURL1(hostYunduanMp4, uri);
//			return makeURL2( hostYunduanMp4, uri);
		}

		private static String makeURL2(String host, String uri) {
			return  "http://"+host+"/"+uri;
		}


		/**
		 * 帝联防盗链URL
		 * @param host
		 * @param uri
		 * @return
		 * @throws Exception
		 */
		private static String makeURL3(String host,String uri) throws Exception{
			long time = System.currentTimeMillis()/1000+3600*12;
//			String timeS = Long.toHexString(time);
			return "http://"+host+"/"+uri+"?t="+time+"&key="+MD5.code("dilianwanx6588"+time+"/"+uri);
		}

		static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		/**
		 * 蓝讯防盗链
		 * @param host
		 * @param uri
		 * @return
		 * @throws Exception
		 */
		private static String makeURL4(String host,String uri) throws Exception{
			// TODO Auto-generated method stub
//			String timeS = sdf.format(new Date());
//			return "http://"+host+"/"+timeS+"/"+MD5.code("lanxunwanx6588"+timeS+"/"+uri)+"/"+uri;
			return  "http://"+host+"/"+uri;
		}
		
	    public URLParse(String url)
	    {
	        int idx = url.indexOf("://");
	        if (idx == -1)
	        {
	            idx = 0;
	        }
	        else
	        {
	            idx += 3;
	        }
	        int idx2 = url.indexOf('/', idx);
	        host = url.substring(idx, idx2);
	        int idx3 = host.indexOf(':');
	        if (idx3 == -1)
	        {
	            port = "80";
	        }
	        else
	        {
	            port = host.substring(idx3 + 1);
	            host = host.substring(0, idx3);
	        }
	        part = url.substring(idx2);
	    }

	    public String getHost()
	    {
	        return host;
	    }

	    public String getPort()
	    {
	        return port;
	    }

	    public String getPath()
	    {
	        return part;
	    }
	    
	    
	/**
	* 功能描述:
	* 参数：@param uri
	* 参数：@return
	* 参数：@throws Exception
	* 返回类型:String
	 */
	public static synchronized String makeHttpsURL(String uri) throws Exception {
		long time = System.currentTimeMillis() / 1000 + 3600 * 24 * 7;
		String timeS = Long.toHexString(time);
		return "https://" + hostYunduanMp4 + "/"
				+ MD5.code("yunduanwanx6588" + "/" + uri + timeS) + "/" + timeS
				+ "/" + uri;
	}
	
	/**
	* 功能描述:根据版本号和平台获取https视频信息
	* 参数：@param uri
	* 参数：@param version
	* 参数：@param platform
	* 参数：@return
	* 参数：@throws Exception
	* 返回类型:String
	 */
	public static synchronized String makeHttpsURLByVersion(String uri,
			String version, String platform) throws Exception {
		//ios平台
		if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.iphone)) {
			int startVer = Util.convert2Num(IConstants.IPHONE_HTTPS_VERSION);
			int verNumber = Util.convert2Num(version);
			if (verNumber > startVer) {
				return makeHttpsURL(uri);
			} else {
				return makeURL(uri);
			}
		} else if ((MobileTypeEnum.valueOf(platform) == MobileTypeEnum.ipad)) {
			int startVer = Util.convert2Num(IConstants.IPAD_HTTPS_VERSION);
			int verNumber = Util.convert2Num(version);
			if (verNumber > startVer) {
				return makeHttpsURL(uri);
			} else {
				return makeURL(uri);
			}
		} else {
			return makeURL(uri);
		}
	}
	    
}
