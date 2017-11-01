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
 * This servlet resolves sparql calls
 * URL: /sparql/
 * @author grozadilla
 *
 */
public class X42TSparqlServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TSparqlServlet.class);

/////////////////////////////////////////////////////////////////////////////////////////
// DO GET
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			logger.info("Invoked Url " + req.getRequestURI() + " - Accept " + req.getHeader("Accept"));

			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				logger.debug("Load Yasgui component");
				goToEndpoint(req, resp);
			} else {
				X42THttpManager.getInstance().redirectGetRequest(req,
													resp,
													X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"),
													req.getHeader("Accept"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("X42TSparqlServlet - " + e.getMessage());
			throw new ServletException(e);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
// DO POST
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		try {
			logger.info("Invoked Url " + req.getRequestURI() + " - Accept " + req.getHeader("Accept"));

			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				logger.debug("Load Yasgui component");
				goToEndpoint(req, resp);
			} else {
				X42THttpManager.getInstance().redirectPostRequest(req,
													resp,
													X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"));
			}
		} catch (final Exception e) {
			throw new ServletException(e);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Redirects to Sparql endpoint page
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void goToEndpoint(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
		//Open yasgui component
		req.setAttribute("url", X42TPropertiesManager.getInstance().getProperty("lod.sparql.endpoint"));
		getServletContext().getRequestDispatcher("/pages/endpoint.jsp").forward(req, resp);
	}

}