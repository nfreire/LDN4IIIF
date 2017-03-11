package inescid.ldn;

import java.io.IOException;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.microdata.MicrodataExtractorFactory;
import org.apache.any23.writer.CountingTripleHandler;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;

import inescid.ldn.http.FetchRequest;
import inescid.ldn.http.HttpFetcher;
import inescid.ldn.http.UrlRequest;

public class InboxChecker {
	
	HttpFetcher httpFetcher;
	Any23 any23=new Any23();

	public InboxChecker(HttpFetcher httpFetcher) {
		super();
		this.httpFetcher = httpFetcher;
	}
	
	public String checkForInbox(String sourceUrl, InboxDiscoveryMethod source) throws InterruptedException, IOException {
		if(source==null) {
			String ret=checkForInbox(sourceUrl, InboxDiscoveryMethod.HTTP_HEADER);
			if(ret==null)
				ret=checkForInbox(sourceUrl, InboxDiscoveryMethod.RDF_MICRODATA);
			return ret;
		}
		
		FetchRequest fetched = httpFetcher.fetch(new UrlRequest(sourceUrl));
//		for(Header h: fetched.getResponse().getAllHeaders()) {
//			System.out.println(h.getName()+" - "+h.getValue());
//		}
		if(source!=null) {
			switch (source) {
			case HTTP_HEADER:
				for(Header h: fetched.getResponse().getHeaders("Link")) {
//					System.out.println(h.getName()+" - "+h.getValue());
					HeaderElement[] hElements = BasicHeaderValueParser.INSTANCE.parseElements(h.getValue(), null);
					for(HeaderElement hEl: hElements) {
//						System.out.println( "v "+hEl.getValue());
//						System.out.println( "n "+hEl.getName());
//						System.out.println( hEl.getParameterCount());
						NameValuePair hElParam=hEl.getParameterByName("rel");
						if(hElParam!=null && hElParam.getValue().equals("http://www.w3.org/ns/ldp#inbox")) {
//							System.out.println( hElParam.getName()+" -- "+hElParam.getValue() );
							return hEl.getName().replaceFirst("^\\s*<\\s*", "").replaceFirst("\\s*>\\s*$", "");
						}
					}
					
				}
				System.out.println();
				break;
			case RDF_MICRODATA:
				try {
					
					String[] inboxUrl=new String[1];
					any23.extract(fetched.getContent().asString(), sourceUrl, fetched.getContent().getType().getMimeType(), fetched.getContent().getType().getCharset().name(), new CountingTripleHandler() {
						@Override
						public void receiveTriple(Resource arg0, IRI arg1, Value arg2, IRI arg3, ExtractionContext arg4)
								throws TripleHandlerException {
							if(inboxUrl[0]!=null)
								return;
//							System.out.println("Triple: "+ arg0.toString());
//							System.out.println("1 : "+ arg1.toString());
//							System.out.println("2: "+ arg2.toString());
//							System.out.println("3: "+ arg3);
							if(arg1!=null && arg1.stringValue().equals("http://www.w3.org/ns/ldp#inbox")) 
								inboxUrl[0]=arg2.stringValue();
						}
					});
					return inboxUrl[0];
				} catch (ExtractionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println(
//				fetched.getContent().asString()
//				);
				break;
			}
		} else {
			
		}
		return null;
	}

	

}
