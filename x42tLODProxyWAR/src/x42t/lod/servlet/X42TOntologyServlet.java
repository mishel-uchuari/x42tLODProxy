package x42t.lod.servlet;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import x42t.lod.utils.X42THttpManager;
import x42t.lod.utils.X42TMIMEtype;
import x42t.lod.utils.X42TPropertiesManager;


public class X42TOntologyServlet extends HttpServlet {

	private static final long serialVersionUID = 1031422249396784970L;

	final static Logger logger = Logger.getLogger(X42TOntologyServlet.class);

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String requestURI = req.getRequestURI().substring(req.getRequestURI().indexOf(req.getContextPath())+ req.getContextPath().length());
		String[] uriData = requestURI.split("/");
		String ontologyName = uriData[2];
		String entityName = requestURI.replaceFirst("/def/" + ontologyName, "");
		entityName = entityName.length()>0 ? entityName.replaceFirst("/", ""): entityName;
		String lang = X42THttpManager.getInstance().getLangFromResquest(req);
		try {
			if (req.getHeader("Accept").contains(X42TMIMEtype.HTML.mimetypevalue())){
					if (ontologyName.endsWith(".owl")){
						ontologyName = ontologyName.substring(0, ontologyName.lastIndexOf(".owl"));
						goToDefinitionOwl(req, resp, ontologyName);
					}else if (ontologyName.endsWith(".html")){
						ontologyName = ontologyName.substring(0, ontologyName.lastIndexOf(".html"));
						goToDefinitionHtml(req, resp, ontologyName);
					}else{
						redirectToDefinitionHtml(req, resp, lang, ontologyName, entityName);
					}
			}else{
				//habra que comprobar las ontologias permitidas
				goToDefinitionOwl(req, resp, ontologyName);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}


	/**
	 * Redirects to Ontology definition page
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void redirectToDefinitionHtml(HttpServletRequest req, HttpServletResponse resp, String lang, String ontology, String entity) throws Exception {
		String page = MessageFormat.format(X42TPropertiesManager.getInstance().getProperty("lod.webroot"),lang) + "def/"+ontology+".html" + (entity.length()==0?"":"#"+entity);
		resp.setStatus(HttpServletResponse.SC_SEE_OTHER);
		resp.addHeader("Location", page);

	}

	/**
	 * Redirects to Ontology definition OWL file
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void goToDefinitionOwl(HttpServletRequest req, HttpServletResponse resp, String ontology) throws Exception {
		getServletContext().getRequestDispatcher("/owl/"+ontology+".owl").forward
           (req, resp);
	}

	/**
	 * Redirects to Ontology definition OWL file
	 * @param req the request
	 * @param resp the response
	 * @throws Exception exception
	 */
	private void goToDefinitionHtml(HttpServletRequest req, HttpServletResponse resp, String ontology) throws Exception {
		getServletContext().getRequestDispatcher("/pages/"+ontology+".html").forward
           (req, resp);
	}
}
