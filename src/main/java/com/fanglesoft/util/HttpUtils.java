package com.fanglesoft.util;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;


public class HttpUtils {

    private HttpUtils(){}

    public static String sendGetRequest(String url){
        return sendGetRequest(url, "utf-8");
    }

    public static String sendGetRequest(String url, String decodeCharset){
        return sendGetRequest(url, null, "utf-8");
    }

    /**
     * 发送HTTP_GET请求
     * 该方法会自动关闭连接,释放资源
     * @param url    请求地址(含参数)
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     */
    public static String sendGetRequest(String url, Map<String, Object> opts, String decodeCharset){
        long responseLength = 0;       //响应长度
        String responseContent = null; //响应内容
        HttpClient httpClient = new DefaultHttpClient(); //创建默认的httpClient实例
        HttpGet httpGet = new HttpGet(url);           //创建org.apache.http.client.methods.HttpGet
        try{

            if(opts != null){
                if(opts.containsKey("headers")){
                    Map<String, String> headers = (Map<String, String>) opts.get("headers");
                    Set<String> headerSetKeys = headers.keySet();
                    for(String key : headerSetKeys){
                        httpGet.addHeader(key, headers.get(key).toString());
                    }
                }
            }


            HttpResponse response = httpClient.execute(httpGet); //执行GET请求
            HttpEntity entity = response.getEntity();            //获取响应实体
            if(null != entity){
                responseLength = entity.getContentLength();
                responseContent = EntityUtils.toString(entity, decodeCharset==null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity); //Consume response content
            }
            System.out.println("请求地址: " + httpGet.getURI());
            System.out.println("响应状态: " + response.getStatusLine());
            System.out.println("响应长度: " + responseLength);
            System.out.println("响应内容: " + responseContent);
        }catch(ClientProtocolException e){
            System.out.println("该异常通常是协议错误导致,比如构造HttpGet对象时传入的协议不对(将'http'写成'htp')或者服务器端返回的内容不符合HTTP协议要求等,堆栈信息如下");
            e.printStackTrace();
        }catch(ParseException e){
            e.printStackTrace();
        }catch(IOException e){
            System.out.println("该异常通常是网络原因引起的,如HTTP服务器未启动等,堆栈信息如下");
            e.printStackTrace();
        }finally{
            httpClient.getConnectionManager().shutdown(); //关闭连接,释放资源
        }
        return responseContent;
    }


    /**
     * 发送HTTP_POST请求
     * 该方法为<code>sendPostRequest(String,String,boolean,String,String)</code>的简化方法
     * 该方法在对请求数据的编码和响应数据的解码时,所采用的字符集均为UTF-8
     * 当<code>isEncoder=true</code>时,
     * 其会自动对<code>sendData</code>中的[中文][|][ ]等特殊
     * 字符进行<code>URLEncoder.encode(string,"UTF-8")</code>
     * @param isEncoder 用于指明请求数据是否需要UTF-8编码,true为需要
     */
    public static String sendPostRequest(String url, String sendData, boolean isEncoder){
        return sendPostRequest(url, sendData, isEncoder, null, null);
    }


    /**
     * 发送HTTP_POST请求
     * 该方法会自动关闭连接,释放资源
     * 当<code>isEncoder=true</code>时,其会自动对<code>sendData</code>中的[中文][|][ ]等特殊字符进行<code>URLEncoder.encode(string,encodeCharset)</code>
     * @param url        请求地址
     * @param sendData      请求参数,若有多个参数则应拼接成param11=value11&22=value22&33=value33的形式后,传入该参数中
     * @param isEncoder     请求数据是否需要encodeCharset编码,true为需要
     * @param encodeCharset 编码字符集,编码请求数据时用之,其为null时默认采用UTF-8解码
     * @param decodeCharset 解码字符集,解析响应数据时用之,其为null时默认采用UTF-8解码
     * @return 远程主机响应正文
     */
    public static String sendPostRequest(String url, String sendData, boolean isEncoder, String encodeCharset, String decodeCharset){
        String responseContent = null;
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);
        //httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
        try{
            if(isEncoder){
                List formParams = new ArrayList();
                for(String str : sendData.split("&")){
                    formParams.add(new BasicNameValuePair(str.substring(0,str.indexOf("=")), str.substring(str.indexOf("=")+1)));
                }
                httpPost.setEntity(new StringEntity(URLEncodedUtils.format(formParams, encodeCharset==null ? "UTF-8" : encodeCharset)));
            }else{
                httpPost.setEntity(new StringEntity(sendData));
            }

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                responseContent = EntityUtils.toString(entity, decodeCharset==null ? "UTF-8" : decodeCharset);
                EntityUtils.consume(entity);
            }
        }catch(Exception e){
            System.out.println("与[" + url + "]通信过程中发生异常,堆栈信息如下");
            e.printStackTrace();
        }finally{
            httpClient.getConnectionManager().shutdown();
        }
        return responseContent;
    }

//
//    public Map<String, Object> postJSON(Map<String, Object> data) {
//        Map<String, Object> ret = new HashMap<String, Object>();
//        ret.put()
//        return {
//                dataType: 'json',
//                method: 'POST',
//                data: JSON.stringify(data),
//                headers: {
//            'Content-Type': 'application/json',
//                    'Accept': 'application/json'
//        }
//  };
//    }


}
