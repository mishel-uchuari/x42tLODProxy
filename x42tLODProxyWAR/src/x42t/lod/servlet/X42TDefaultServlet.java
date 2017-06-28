package x42t.lod.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * This servlet resolves sparql calls
 * @author grozadilla
 *
 */
public class X42TDefaultServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TDefaultServlet.class);


	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			goToWeb(req, resp);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}



	/**
	 * Redirects to initial page
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void goToWeb(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		getServletContext().getRequestDispatcher("/pages/lod_euskadi.html").forward
           (req, resp);
	}

}
