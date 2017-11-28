package x42t.lod.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import x42t.lod.utils.X42TPropertiesManager;

/**
 * Ontology Servlet
 * URL: /def/
 * @author grozadilla
 */
public class X42TKosServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	private static final Logger logger = Logger.getLogger(X42TKosServlet.class);


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

			//get fileName
			final String[] uriData = requestURI.split("/");
			final String fileName = uriData[2];
			logger.info("fileName: " + fileName);

			final String content_manager_url = X42TPropertiesManager.getInstance().getProperty("lod.contentmanager.url");
			processKosRequest(req, resp, content_manager_url, fileName);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("X42TKosServlet - " + e.getMessage());
			throw new ServletException(e);
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	// PROCESS KOS REQUEST
	/////////////////////////////////////////////////////////////////////////////////////////
	private void processKosRequest(final HttpServletRequest req,
									 final HttpServletResponse resp,
									 final String content_manager_url,
									 final String fileName) throws Exception {
		final String locationURL = content_manager_url + "/" + fileName;
		logger.info("processKosRequest LocationURL: " + locationURL);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
		resp.addHeader("Location", locationURL);
	}

}
