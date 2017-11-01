package x42t.lod.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import x42t.lod.utils.X42THttpManager;
import x42t.lod.utils.X42TMIMEtype;
import x42t.lod.utils.X42TPropertiesManager;

/**
 * Data Servlet
 * URL: /data/
 * Accept: always RDF
 * @author grozadilla
 */
public class X42TDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	private static final Logger logger = Logger.getLogger(X42TDataServlet.class);

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
			String resourceURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());//recuperamos la uri del recurso solicitado
			final String lang = X42THttpManager.getInstance().getLangFromResquest(req);


			final String urlTriplestore = X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"); //recuperamos la url de la triplestore

			String acceptHeader = req.getHeader("Accept");
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				//Para el caso de los HTMLs miramos la extension que tiene para ver que accept poner y le quitamos la extension
				if (X42THttpManager.getInstance().isURIWithExtension(req.getRequestURI())){
					acceptHeader = X42THttpManager.getInstance().getAcceptFromURI(req.getRequestURI());
					resourceURI = X42THttpManager.getInstance().getURIWithoutExtension(resourceURI);
				}
			}

			final String basePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.uri.base.path"), lang);
			final String completeURI = basePath + resourceURI.replaceFirst("/data/", "/id/");
			//add query as parameter to URL
			final String query = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.sparql.query.describe"), completeURI);
			final String url = urlTriplestore + "?query=" + URLEncoder.encode(query, "UTF-8");

			logger.info("DoGetRequest From Triplestore - URL : " + url + " - Accept " + acceptHeader);
			X42THttpManager.getInstance().redirectGetRequest(req, resp, url, acceptHeader);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("X42TDataServlet - " + e.getMessage());
			throw new ServletException(e);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHOS
/////////////////////////////////////////////////////////////////////////////////////////



}
