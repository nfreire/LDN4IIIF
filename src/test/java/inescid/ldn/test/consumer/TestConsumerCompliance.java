package inescid.ldn.test.consumer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import inescid.ldn.Consumer;
import inescid.ldn.InboxChecker;
import inescid.ldn.InboxDiscoveryMethod;
import inescid.ldn.http.FetchRequest;
import inescid.ldn.http.HttpFetcher;
import inescid.ldn.http.UrlRequest;

public class TestConsumerCompliance {
	public enum Test {HD, BD, LJLC, LJLE,	NAnnounce,	NChangeLog,	NCitation, NAssessing, NComment, NRsvp};
	
	public static void main(String[] args) {
		Map<Test, String> testResults=new HashMap<TestConsumerCompliance.Test, String>();
		
		HttpFetcher httpFetcher= new HttpFetcher();
		
		Consumer c=new Consumer(httpFetcher);
		
		InboxChecker checker=new InboxChecker(httpFetcher);
		
		String inboxUrlTargetA=null;
		try {
			inboxUrlTargetA = checker.checkForInbox("https://linkedresearch.org/ldn/tests/discover-inbox-link-header", null);
			testResults.put(Test.HD, inboxUrlTargetA);
			System.out.println("Target A inbox URL:\n"+inboxUrlTargetA);
		
			List<String> notifs = c.listNotifications(inboxUrlTargetA);
			String ljl="";
			
			for(String ntfUrl: notifs) {
				if(!ljl.isEmpty())
					ljl+=" ";
				ljl+=ntfUrl;
				
				System.out.println(ntfUrl);
				UrlRequest urlReq=new UrlRequest(ntfUrl, "Accept", "application/ld+json");
				FetchRequest fetched = httpFetcher.fetch(urlReq);
				if (fetched.getResponseStatusCode()==200) {
					if(fetched.getContent().getType().getMimeType().equals("application/ld+json")) {
						testResults.put(getTestOfNotification(ntfUrl), fetched.getContent().asString());
					}
				}
			}
			testResults.put(Test.LJLC, ljl);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("-----------------");
		String inboxUrlTargetB; 
		try {
			inboxUrlTargetB = checker.checkForInbox("https://linkedresearch.org/ldn/tests/discover-inbox-rdf-body", null);
			testResults.put(Test.BD, inboxUrlTargetB);
			System.out.println("Target B inbox URL:\n"+inboxUrlTargetB);
			
			List<String> notifs = c.listNotifications(inboxUrlTargetB);
			String ljl="";
			
			for(String ntfUrl: notifs) {
				if(!ljl.isEmpty())
					ljl+=" ";
				ljl+=ntfUrl;
				System.out.println(ntfUrl);
				UrlRequest urlReq=new UrlRequest(ntfUrl, "Accept", "application/ld+json");
				FetchRequest fetched = httpFetcher.fetch(urlReq);
				if (fetched.getResponseStatusCode()==200) {
					if(fetched.getContent().getType().getMimeType().equals("application/ld+json")) {
						testResults.put(getTestOfNotification(ntfUrl), fetched.getContent().asString());
					}
				}
			}
			testResults.put(Test.LJLE, ljl);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
		for(Test t: Test.values()) {
			System.out.println(t+"\n"+testResults.get(t)+"\n");
		}
		
	}

	private static Test getTestOfNotification(String notificationUrl) {
		if(notificationUrl.endsWith("announce"))
			return Test.NAnnounce;
		else if(notificationUrl.endsWith("changelog"))
			return Test.NChangeLog;
		else if(notificationUrl.endsWith("citation"))
			return Test.NCitation;
		else if(notificationUrl.endsWith("assessing"))
			return Test.NAssessing;
		else if(notificationUrl.endsWith("comment"))
			return Test.NComment;
		else if(notificationUrl.endsWith("rsvp"))
			return Test.NRsvp;
		return null;
	}
}
