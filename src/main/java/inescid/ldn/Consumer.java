package inescid.ldn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import inescid.ldn.http.FetchRequest;
import inescid.ldn.http.HttpFetcher;
import inescid.ldn.http.UrlRequest;

public class Consumer {
	HttpFetcher httpFetcher= new HttpFetcher();

	public Consumer(HttpFetcher httpFetcher) {
		super();
		this.httpFetcher = httpFetcher;
	}
	
	public List<String> listNotifications(String inboxUrl) throws InterruptedException, IOException{
		List<String> notifications=new ArrayList<String>();
		
		UrlRequest urlReq=new UrlRequest(inboxUrl, "Accept", "application/ld+json");
		FetchRequest fetched = httpFetcher.fetch(urlReq);
		if (fetched.getResponseStatusCode()==200) {
			if(fetched.getContent().getType().getMimeType().equals("application/ld+json")) {
				Model dcModelRdf = ModelFactory.createDefaultModel();
				ByteArrayInputStream bytesIs = new ByteArrayInputStream(fetched.getContent().asBytes());
				RDFDataMgr.read(dcModelRdf, bytesIs, inboxUrl, Lang.JSONLD);
				try {
					bytesIs.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e); 
				}
					
				Resource resource = dcModelRdf.getResource(inboxUrl);
				
				StmtIterator seeAlsoStms = resource.listProperties(RdfReg.LDN_CONTAINS);
				while (seeAlsoStms.hasNext()) {
					Statement st = seeAlsoStms.next();
//					System.out.println(st);
					notifications.add(st.getObject().toString());
				}
			}
		}
		
		return notifications;
	}
}
