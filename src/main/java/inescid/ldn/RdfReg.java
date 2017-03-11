package inescid.ldn;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class RdfReg {
	
		public static final Property LDN_CONTAINS=ResourceFactory.createProperty("http://www.w3.org/ns/ldp#contains");
		public static final Resource IIIF_MANIFEST = ResourceFactory.createResource("http://iiif.io/api/presentation/2#Manifest");
		public static final Resource IIIF_COLLECTION = ResourceFactory.createResource("http://iiif.io/api/presentation/2#Collection");

}
