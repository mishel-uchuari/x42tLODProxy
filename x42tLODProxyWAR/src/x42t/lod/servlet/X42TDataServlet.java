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


public class X42TDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TDataServlet.class);

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

	
		String url = X42TPropertiesManager.getInstance().getProperty("lod.triplestore.url"); //recuperamos la url de la triplestore
		String resourceURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());//recuperamos la uri del recurso solicitado

		String acceptHeader = null;
		if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
			//tendremos que recuperar la extensión de la url. y segun eso decidir que mostrar.
			if (X42THttpManager.getInstance().isURIWithExtension(req.getRequestURI())){
				acceptHeader = X42THttpManager.getInstance().getAcceptFromURI(req.getRequestURI());
				resourceURI = X42THttpManager.getInstance().getURIWithoutExtension(resourceURI);
			}
		}

		String lang = X42THttpManager.getInstance().getLangFromResquest(req);
		String basePath = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.uri.base.path"),lang);
		String completeURI = basePath + resourceURI.replaceFirst("/data/", "/id/");
		//add query as parameter to URL
		String query = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.sparql.query.describe"), completeURI);
		url = url + "?query="+URLEncoder.encode(query, "UTF-8");


		try {
			X42THttpManager.getInstance().redirectGetRequest(req, resp, url, acceptHeader);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}



}
