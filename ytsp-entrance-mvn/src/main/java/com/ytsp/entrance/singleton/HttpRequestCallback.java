package com.ytsp.entrance.singleton;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

public class HttpRequestCallback implements KeyConflictCallback {

    private static final Logger log = Logger.getLogger(HttpRequestCallback.class);
    private URI uri;
    public HttpRequestCallback(URI uri) {
        this.uri = uri;
    }

    @Override
    public void execute(String id, String key) {
        HttpClient client = new DefaultHttpClient();
        try {
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("cid", id));
            qparams.add(new BasicNameValuePair("ckey", key));
            URI u = URIUtils.createURI(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet get = new HttpGet(u);
            client.execute(get);
        } catch (ClientProtocolException e) {
            log.error("call back FAILED", e);
        } catch (IOException e) {
            log.error("call back FAILED", e);
        } catch (URISyntaxException e) {
            log.error("call back FAILED", e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

}
