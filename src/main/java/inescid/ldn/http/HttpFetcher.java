package inescid.ldn.http;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import javax.sound.midi.Soundbank;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


public class HttpFetcher {
	CloseableHttpClient httpClient;
	
//	Vector<Long> requestTimeStats=null;
//	Vector<Long> requestTimeStats=new Vector<>();
	
	public HttpFetcher() {
		init();
	}
	
	public void init() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(41);

		httpClient = 
				HttpClients.custom()
				.setSSLHostnameVerifier(new NoopHostnameVerifier())
				.setConnectionManager(cm) 
				.build();
	}	
	
	public void close() throws Exception {
		httpClient.close();
	}
	
	public FetchRequest fetch(UrlRequest url) throws InterruptedException, IOException {
		FetchRequest fetchResult=new FetchRequest(url);
		HttpGet request = new HttpGet(url.getUrl());
		fetchResult.addHeaders(request);
		CloseableHttpResponse response = httpClient.execute(request);
		fetchResult.setResponse(response);
		return fetchResult;
	}

}
