package x42t.lod.utils;

/**
 * @author Mikel Egaña Aranguren
 *
 *         Taken from http://docs.rdf4j.org/rest-api/#_content_types and http://docs.stardog.com/#_http_headers_content_type_accept
 *
 */
public enum X42TMIMEtype {

	RDFXML("application/rdf+xml"),
	Turtle("text/turtle"),
	NTriples("text/plain"),
	N3("text/rdf+n3"),
	NQuads("text/x-nquads"),
	CSV("text/csv"),
	RDFJSON("application/rdf+json"),
	JSONLD("application/ld+json"),
	TriG("application/trig"),
	TriX("application/trix"),
	BinaryRDF("application/x-binary-rdf"),
	SPARQLXMLResultsFormat("application/sparql-results+xml"),
	SPARQLJSONResultsFormat("application/sparql-results+json"),
	SPARQLBooleanResults("text/boolean"),
	SPARQLBinaryResults("application/x-binary-rdf-results-table"),
	TABVAL("text/tab-separated-values"),
	HTML("text/html");

	private String mimetypestring;

	X42TMIMEtype(String mimetypestring) {
		this.mimetypestring = mimetypestring;
	}

	public String mimetypevalue() {
		return mimetypestring;
	}


}