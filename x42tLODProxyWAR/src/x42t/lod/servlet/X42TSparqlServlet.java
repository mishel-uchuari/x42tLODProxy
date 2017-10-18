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
 * @author grozadilla
 *
 */
public class X42TSparqlServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TSparqlServlet.class);


	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				if(logger.isDebugEnabled()){
				    logger.debug("Load Yasgui component");
				}
				goToEndpoint(req, resp);
			}else{
				X42THttpManager.getInstance().redirectGetRequest(req, resp, X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"), null);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				goToEndpoint(req, resp);
			}else{
				X42THttpManager.getInstance().redirectPostRequest(req,resp, X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"));
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Redirects to Sparql endpoint page
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void goToEndpoint(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		//Open yasgui component
		req.setAttribute("url", X42TPropertiesManager.getInstance().getProperty("lod.sparql.endpoint"));
		getServletContext().getRequestDispatcher("/pages/endpoint.jsp").forward
           (req, resp);
	}

}