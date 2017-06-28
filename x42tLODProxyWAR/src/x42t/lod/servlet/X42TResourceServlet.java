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


public class X42TResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TResourceServlet.class);

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
		try {
			//get info from request URI
			String resourceURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());
			String resourceId = resourceURI.split("/")[1];
			String lang = X42THttpManager.getInstance().getLangFromResquest(req);
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
				//launch ASK query to blazegrah
				String triplestoreUrl = X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url");

				//create ASK query
				String uriBasePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.uri.base.path"),lang);
				String completeURI = uriBasePath + resourceURI.replaceFirst("/kos/", "/id/");
				String query = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.sparql.query.ask"), completeURI);
				//add query as parameter to URL
				String url = triplestoreUrl + "?query="+URLEncoder.encode(query, "UTF-8");


				try {
					HttpResponse response = X42THttpManager.getInstance().doGetRequest(req, resp, url, null);
					HttpEntity entity = response.getEntity();

			            // Read the contents of an entity and return it as a String.
			        String content = EntityUtils.toString(entity);
			        boolean page = false;
			        JSONParser parser = new JSONParser();
			        JSONObject obj = (JSONObject)parser.parse(content);
			  		if (obj.containsKey("boolean")){
			  			page = ((Boolean)obj.get("boolean")).booleanValue();
			  		}

					if (page){
						String basePagePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.page.url.base"),lang);
						String redirectUri = basePagePath + resourceURI.replaceFirst("/"+resourceId+"/", "/page/");
						resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
						resp.addHeader("Location", redirectUri);
					}else{
						String baseDocPath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.doc.url.base"),lang);
						String redirectUri = baseDocPath + resourceURI.replaceFirst("/"+resourceId+"/", "");
						resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
						resp.addHeader("Location", redirectUri);
					}


				} catch (Exception e) {
					throw new ServletException(e);
				}
			}else{
				//redirect to other url
				String url = req.getRequestURI().replaceFirst("/"+resourceId+"/", "/data/");
				resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
				resp.addHeader("Location", url);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}



}
