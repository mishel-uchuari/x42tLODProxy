package x42t.lod.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import x42t.lod.utils.X42THttpManager;
import x42t.lod.utils.X42TMIMEtype;
import x42t.lod.utils.X42TPropertiesManager;

/**
 * Ontology Servlet
 * URL: /def/
 * @author grozadilla
 */
public class X42TOntologyServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	private static final Logger logger = Logger.getLogger(X42TOntologyServlet.class);


/////////////////////////////////////////////////////////////////////////////////////////
// DO GET
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void doGet(final HttpServletRequest req,
					  final HttpServletResponse resp)
					  throws ServletException, IOException {
		try {
			logger.info("Invoked Url " + req.getRequestURI() + " - Accept " + req.getHeader("Accept"));

			//get info from request URI
			final String requestURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());
			final String lang = X42THttpManager.getInstance().getLangFromResquest(req);

			//get ontologyName
			final String[] uriData = requestURI.split("/");
			final String ontologyName = uriData[2];
			logger.info("ontologyName: " + ontologyName);


			final String apache_folder = X42TPropertiesManager.getInstance().getProperty("lod.apache.folder");

			if (req.getHeader("Accept").contains(X42TMIMEtype.RDFXML.mimetypevalue())
					|| req.getHeader("Accept").contains(X42TMIMEtype.RDFJSON.mimetypevalue())
					|| req.getHeader("Accept").contains(X42TMIMEtype.BinaryRDF.mimetypevalue())){
				String finalOntologyName = ontologyName;
				if (ontologyName.lastIndexOf(".") > -1)
					finalOntologyName = ontologyName.substring(0, ontologyName.lastIndexOf(".")) + ".owl";
				else
					finalOntologyName = ontologyName + ".owl";
				logger.info("effective ontologyName: " + finalOntologyName + " - " + ontologyName.lastIndexOf("."));
				processOWLExtension(req, resp, apache_folder, finalOntologyName);
			} else {
				logger.info("effective ontologyName: " + ontologyName);
				if (ontologyName.endsWith(".owl")){
					processOWLExtension(req, resp, apache_folder, ontologyName);
				} else if (ontologyName.endsWith(".html")) {
					processHTMLExtension(req, resp, apache_folder, ontologyName);
				} else {
					processOtherExtension(req, resp, requestURI, lang, ontologyName);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("X42TOntologyServlet - " + e.getMessage());
			throw new ServletException(e);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
// PROCESS EXTENSIONS
/////////////////////////////////////////////////////////////////////////////////////////
	private void processOWLExtension(final HttpServletRequest req,
									 final HttpServletResponse resp,
									 final String apache_folder,
									 final String ontologyName) throws Exception {
		final String locationURL = X42TPropertiesManager.getInstance().getProperty("lod.def.url.base")
				 								+ apache_folder + "/owl/" + ontologyName;
		logger.info("processOWLExtension LocationURL: " + locationURL);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
		resp.addHeader("Location", locationURL);
	}

	private void processHTMLExtension(final HttpServletRequest req,
						     		  final HttpServletResponse resp,
						     		  final String apache_folder,
						     		  final String ontologyName) throws Exception {
		final String locationURL = X42TPropertiesManager.getInstance().getProperty("lod.def.url.base")
												+ apache_folder + "/pages/" + ontologyName;
		logger.info("processHTMLExtension LocationURL: " + locationURL);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
		resp.addHeader("Location", locationURL);
	}

	private void processOtherExtension(final HttpServletRequest req,
   		  							   final HttpServletResponse resp,
   		  							   final String requestURI,
   		  							   final String lang,
   		  							   final String ontologyName) throws Exception {
		String entityName = requestURI.replaceFirst("/def/" + ontologyName, "");
		entityName = entityName.length() > 0 ? entityName.replaceFirst("/", ""): entityName;
		String extension = ".owl";
		if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
			extension = ".html";
		}
		logger.info("processOtherExtension extension: " + extension);
		final String locationURL = X42TPropertiesManager.getInstance().getProperty("lod.def.url.base")
												+ "/def/" + ontologyName + extension
												+ (entityName.length()==0?"":"#"+entityName);
		logger.info("processOtherExtension LocationURL: " + locationURL);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
		resp.addHeader("Location", locationURL);
	}

}
