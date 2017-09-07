package com.ytsp.entrance.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


/**
 * 概要： .<br/>
 * 説明： <br/>
 * 著作権: (c) 2009 Accenture All Rights Reserved.<br/>
 * 会社名: MARUZEN CO., LTD<br/>
 * @author maying
 * <pre>
 *------------------------------------------------------------------------------------------
 * MODIFICATION HISTORY
 *------------------------------------------------------------------------------------------
 * SIRID                When          Who           Why
 *------------------------------------------------------------------------------------------
 * Initial Release    2012-2-27     氏　名     初回作成
 * </pre>
 */
public class IPAndMobileSeeker {
    public static final String REGEX_GET_PROVINCE="查询结果.*?-->(.*?)&";
    public static final String REGEX_GET_CITY="";
    public static final String REGEX_GET_TYPE="";
    public static final String REGEX_GET_MOBILE="(?is)<td.*?>您查询的手机号码段</td>" +
    		".*?<td.*?>(.*?)</td>.*?<td.*?>卡号归属地</td>.*?<td.*?>(.*?)</td>.*?<td.*?>卡&nbsp;" +
    		"类&nbsp;型</td>.*?<td.*?>(.*?)</td>";

    public static String getMobileFrom(String mobileNum){
        HttpClient client = null;
        PostMethod method = null;
        NameValuePair mobileParam = null;
        NameValuePair actionParam = null;

        int httpStatusCode;
        String htmlSource = null;
        String result = null;

        try{
            client = new HttpClient();
            client.getHostConfiguration().setHost("www.ip138.com",8080,"http");
            method = new PostMethod("/search.asp");
            mobileParam = new NameValuePair("mobile",mobileNum);
            actionParam = new NameValuePair("action","mobile");
            method.setRequestBody(new NameValuePair[]{actionParam,mobileParam});
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gb2312");
            client.executeMethod(method);
            httpStatusCode=method.getStatusLine().getStatusCode();
            if(httpStatusCode!=200){
                throw new Exception("error to get earch page content!");
            }
            htmlSource=method.getResponseBodyAsString();
            if(htmlSource!=null && !htmlSource.equals("")){
                result=parseMobileFrom(htmlSource);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            method.releaseConnection();
        }
        return result;
    }

    public static String getIPFrom(String ip){
        HttpClient client = null;
        PostMethod method = null;
        NameValuePair ipParam = null;
        NameValuePair actionParam = null;

        int httpStatusCode;
        String htmlSource = null;
        String result = null;

        try{
            client = new HttpClient();
            client.getHostConfiguration().setHost("www.ip138.com",8080,"http");
            method = new PostMethod("/ips.asp");
            ipParam = new NameValuePair("ip",ip);
            actionParam = new NameValuePair("action","2");
            method.setRequestBody(new NameValuePair[]{ipParam,actionParam});
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "gb2312");
            System.out.println("+++++++++++"+method.getPath());
            client.executeMethod(method);
            httpStatusCode=method.getStatusLine().getStatusCode();
            System.out.println(method.getStatusLine().getStatusCode());
            if(httpStatusCode!=200){
                throw new Exception("error to get search page content!");
            }
            htmlSource=method.getResponseBodyAsString();
            if(htmlSource!=null && !htmlSource.equals("")){
                result=parseIpFrom(htmlSource);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            method.releaseConnection();
        }
        return result;
    }

    public static String parseMobileFrom(String htmlSource){
        Pattern p1 = null;
        Matcher m1 = null;
System.out.println(htmlSource+"****************");
        htmlSource = htmlSource.replaceAll("<!--.*?-->", "");

System.out.println(htmlSource+"@@@@@@@@@@@@@@@@");
        p1=Pattern.compile(REGEX_GET_MOBILE);
        m1=p1.matcher(htmlSource);
        if(m1.find()){
            //System.out.println(m1.group(1)+","+m1.group(2).replace("&nbsp;", ",")+","+m1.group(3));
            return m1.group(1)+","+m1.group(2).replace("&nbsp;", ",");
        }else{
            return null;
        }
    }

    public static String parseIpFrom(String htmlSource){
        Pattern p1 = null;
        Matcher m1 = null;
System.out.println(htmlSource+"****************");
        htmlSource = htmlSource.replaceAll("<!--.*?-->", "");

System.out.println(htmlSource+"@@@@@@@@@@@@@@@@");
        p1=Pattern.compile(REGEX_GET_MOBILE);
        m1=p1.matcher(htmlSource);
        if(m1.find()){
            //System.out.println(m1.group(1)+","+m1.group(2).replace("&nbsp;", ",")+","+m1.group(3));
            return m1.group(1)+","+m1.group(2).replace("&nbsp;", ",");
        }else{
            return null;
        }
    }

    public static void main(String[] args){
        //System.out.println(IPAndMobileSeeker.getMobileFrom("15811012481"));
        System.out.println(IPAndMobileSeeker.getIPFrom("222.88.21.2"));
    }
}
