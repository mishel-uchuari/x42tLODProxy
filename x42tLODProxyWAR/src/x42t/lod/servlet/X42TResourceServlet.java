package x42t.lod.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import x42t.lod.utils.X42THttpManager;
import x42t.lod.utils.X42TMIMEtype;
import x42t.lod.utils.X42TPropertiesManager;

/**
 * Resource Servlet
 * URL: /id/, /kos/
 * @author grozadilla
 */
public class X42TResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	private static final Logger logger = Logger.getLogger(X42TResourceServlet.class);

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
			final String resourceURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());
			final String resourceId = resourceURI.split("/")[1];
			final String lang = X42THttpManager.getInstance().getLangFromResquest(req);

			String locationURL;
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				locationURL = getLocationURLAcceptHTML(req, resp, resourceURI, resourceId, lang);
			} else {
				locationURL = getLocationURLOtherAccept(resourceURI, resourceId, lang);
			}

			logger.info("Location URL: " + locationURL + " - Accept " + req.getHeader("Accept"));
			resp.addHeader("Location", locationURL);
			resp.setStatus(HttpServletResponse.SC_SEE_OTHER);

		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("X42TResourceServlet - " + e.getMessage());
			throw new ServletException(e);
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHOS
/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get Location URL when Accept is HTML
	 * @param req
	 * @param resp
	 * @param resourceURI
	 * @param resourceId
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	private String getLocationURLAcceptHTML(final HttpServletRequest req,
								   			final HttpServletResponse resp,
								   			final String resourceURI,
								   			final String resourceId,
								   			final String lang) throws Exception {

		final Boolean page = isRedirectToPage(req, resp, resourceURI, resourceId, lang);
  		logger.debug("isRedirectToPage " + page);

		if (page) {
			//Redirect to page
			final String basePagePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.page.url.base"), lang);
			return basePagePath + resourceURI.replaceFirst("/"+resourceId+"/", "/page/");
		} else {
			//Redirect to Elda
			final String baseDocPath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.doc.url.base"), lang);
			return baseDocPath + resourceURI.replaceFirst("/"+resourceId+"/", "");
		}
	}

	/**
	 * Get Location URL when Accept is not HTML
	 * @param resourceURI
	 * @param resourceId
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	private String getLocationURLOtherAccept(final String resourceURI,
			   								 final String resourceId,
			   								 final String lang) throws Exception {
		//redirect to other url
		final String basePagePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.data.url.base"),lang);
		return basePagePath + resourceURI.replaceFirst("/"+resourceId+"/", "/data/");
	}

	/**
	 * See in triplestore. It is a redirect to page
	 * @param req
	 * @param resp
	 * @param resourceURI
	 * @param resourceId
	 * @param lang
	 * @return
	 * @throws Exception
	 */
	private Boolean isRedirectToPage(final HttpServletRequest req,
								   	 final HttpServletResponse resp,
								   	 final String resourceURI,
									 final String resourceId,
									 final String lang) throws Exception {
		//launch ASK query to blazegrah
		final String triplestoreUrl = X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url");

		//create ASK query
		final String uriBasePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.uri.base.path"), lang);
		final String completeURI = uriBasePath + resourceURI.replaceFirst("/kos/", "/id/");
		final String query = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.sparql.query.ask.isPage"), completeURI);
		//add query as parameter to URL
		final String url = triplestoreUrl + "?query="+URLEncoder.encode(query, "UTF-8");

		logger.info("DoGetRequest From Triplestore - URL : " + url + " - Accept " + req.getHeader("Accept"));
		final HttpResponse response = X42THttpManager.getInstance().doGetRequest(req, resp, url, null);
		final HttpEntity entity = response.getEntity();

        // Read the contents of an entity and return it as a String.
        final String content = EntityUtils.toString(entity);
        final JSONParser parser = new JSONParser();
        final JSONObject obj = (JSONObject)parser.parse(content);
  		if (obj.containsKey("boolean")){
  			return ((Boolean) obj.get("boolean")).booleanValue();
  		}
  		return false;
	}

}
