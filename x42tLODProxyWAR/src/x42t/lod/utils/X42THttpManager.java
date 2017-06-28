package x42t.lod.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

/**
 * Utilities to http connections
 * @author grozadilla
 *
 */
public class X42THttpManager {

	final static Logger logger = Logger.getLogger(X42THttpManager.class);
	private static X42THttpManager INSTANCE = null;

	/**
	 * Get a HttpManager instance
	 * @return HttpManager instance
	 */
	public synchronized static X42THttpManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new X42THttpManager();
		}
		return INSTANCE;
	}

	/**
	 * Add original request params to a new url
	 * @param theReq original request
	 * @param url new url
	 * @return url with params
	 * @throws UnsupportedEncodingException
	 */
	private static String addParamsToUrl (HttpServletRequest theReq, String url) throws UnsupportedEncodingException{
		String completeUrl = url;

		Enumeration<String> aParamsEnum = theReq.getParameterNames();
		if (aParamsEnum.hasMoreElements()){
			completeUrl = completeUrl + "?";
		}
		while (aParamsEnum.hasMoreElements()) {
			String aParamName = aParamsEnum.nextElement();
			String aParamVal = URLEncoder.encode(theReq.getParameter(aParamName), "UTF-8");
			completeUrl = completeUrl +aParamName + "=" + aParamVal;
			if (aParamsEnum.hasMoreElements()){
				completeUrl = completeUrl + "&";
			}
		}
		return completeUrl;
	}

	/**
	 * Copy original request headers to a new httpGet
	 * @param req original request
	 * @param httpget the new http connection
	 * @throws UnsupportedEncodingException
	 */
	private static void copyHeaders (HttpServletRequest req, HttpGet httpget, String acceptHeader) throws UnsupportedEncodingException{
		//headers
		Enumeration<String> aHeadersEnum = req.getHeaderNames();
		while (aHeadersEnum.hasMoreElements()) {
			String aHeaderName = aHeadersEnum.nextElement();
			String aHeaderVal = req.getHeader(aHeaderName);
			if ("accept".equals(aHeaderName)){
				aHeaderVal = acceptHeader!= null?acceptHeader:aHeaderVal;
			}
			httpget.setHeader(aHeaderName, aHeaderVal);
		}

	}

	/**
	 * Copy original request headers to a new httpPost
	 * @param req original request
	 * @param httppost the new http connection
	 * @throws UnsupportedEncodingException
	 */
	private static void copyHeaders (HttpServletRequest req, HttpPost httppost) throws UnsupportedEncodingException{
		//headers
		Enumeration<String> aheadersenum = req.getHeaderNames();
		while (aheadersenum.hasMoreElements()) {
			String aheadername = aheadersenum.nextElement();
			String aheaderval = req.getHeader(aheadername);
			if (!"content-length".equals(aheadername.toLowerCase())){
						httppost.setHeader(aheadername, aheaderval);

			}
		}

	}

	/**
	 * Copy http connection response to servlet response
	 * @param response
	 * @param theResp
	 * @throws IOException
	 */
	private static void copyResponseToServletResponse(HttpResponse response, HttpServletResponse theResp ) throws IOException{
		// set the same Headers
		/*for(Header aHeader : response.getAllHeaders()) {
			theResp.setHeader(aHeader.getName(), aHeader.getValue());
		}*/

		theResp.setLocale(response.getLocale());

		// set the content
		theResp.setContentLength((int) response.getEntity().getContentLength());
		theResp.setContentType(response.getEntity().getContentType().getValue());

		// set the same status
		theResp.setStatus(response.getStatusLine().getStatusCode());

		// redirect the output
		InputStream aInStream = null;
		OutputStream aOutStream = null;
		try {
			aInStream = response.getEntity().getContent();
			aOutStream = theResp.getOutputStream();

			IOUtils.copy(aInStream,aOutStream);

		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		finally {
			if (aInStream != null)
				aInStream.close();
			if (aOutStream != null)
				aOutStream.close();
		}
	}

	/**
	 *
	 * @param theReq
	 * @param theResp
	 * @param url
	 * @throws Exception
	 */
	public void redirectGetRequest(HttpServletRequest theReq, HttpServletResponse theResp, String url, String acceptHeader) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = null;
		HttpResponse response = null;

		if (logger.isDebugEnabled()) logger.debug("> Sparql-proxy: "+ url);

		try {
			//parameters
			String theReqUrl = addParamsToUrl(theReq, url);
			httpget = new HttpGet(theReqUrl);
			//headers
			copyHeaders(theReq, httpget, acceptHeader);
			if (logger.isDebugEnabled()) logger.debug("executing request " + httpget.getURI());
			// Create a response handler
			response = httpclient.execute(httpget);
			copyResponseToServletResponse(response, theResp);
		} catch (Exception ex) {
	        throw ex;
	    } finally {
			httpclient.getConnectionManager().closeExpiredConnections();
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 *
	 * @param theReq
	 * @param theResp
	 * @param url
	 * @throws Exception
	 */
	public void redirectPostRequest(HttpServletRequest theReq, HttpServletResponse theResp, String url) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		HttpResponse response = null;

		try {
			httppost = new HttpPost(url);
			String body = getRequestBody(theReq);
			HttpEntity entity = new ByteArrayEntity(body.getBytes());
			httppost.setEntity(entity);

			copyHeaders(theReq, httppost);
			if (logger.isDebugEnabled()) logger.debug("executing request " + httppost.getURI());

			response = httpclient.execute(httppost);

			copyResponseToServletResponse(response, theResp);
		} catch (Exception ex) {
	        throw ex;
	    } finally {
			httpclient.getConnectionManager().closeExpiredConnections();
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 *
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private static String getRequestBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}

	/**
	 *
	 * @param theReq
	 * @param theResp
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public HttpResponse doGetRequest(HttpServletRequest theReq, HttpServletResponse theResp, String url, String accept) throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = null;

		String theReqUrl = url;

		if (logger.isDebugEnabled()) logger.debug("> Sparql-proxy: "+ theReqUrl);

		try {
			//parameters
			Enumeration<String> aParamsEnum = theReq.getParameterNames();
			if (aParamsEnum.hasMoreElements()){
				theReqUrl = theReqUrl + "?";
			}
			while (aParamsEnum.hasMoreElements()) {
				String aParamName = aParamsEnum.nextElement();
				String aParamVal = theReq.getParameter(aParamName);
				if (aParamName.equals("query")){
					aParamVal = URLEncoder.encode(theReq.getParameter(aParamName), "UTF-8");
				}
				theReqUrl = theReqUrl +aParamName + "=" + aParamVal;
				if (aParamsEnum.hasMoreElements()){
					theReqUrl = theReqUrl + "&";
				}
			}



			httpget = new HttpGet(theReqUrl);

			//headers
			Enumeration<String> aHeadersEnum = theReq.getHeaderNames();
			while (aHeadersEnum.hasMoreElements()) {
				String aHeaderName = aHeadersEnum.nextElement();
				String aHeaderVal = theReq.getHeader(aHeaderName);
				if ("accept".equals(aHeaderName.toLowerCase()) && (accept == null || accept.isEmpty())){
						httpget.setHeader(aHeaderName, "application/json");
				}else{
					httpget.setHeader(aHeaderName, aHeaderVal);
				}
			}

			if (logger.isDebugEnabled()) logger.debug("executing request " + httpget.getURI());

			return httpclient.execute(httpget);


		} finally {
			httpclient.getConnectionManager().closeExpiredConnections();
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 * Gets language from Request
	 * @param theReq original request
	 * @return lang
	 */
	public String getLangFromResquest (HttpServletRequest theReq) throws IOException{
		return getLangFromHostName(theReq.getServerName());
	}

	/**
	 * Gets Lenguage from Hostname
	 * @param hostName
	 * @return lang
	 * @throws IOException
	 */
	private static String getLangFromHostName(String hostName) throws IOException  {
		String lang = "es";
		String[] pattern = X42TPropertiesManager.getInstance().getProperty("lod.hostName.pattern").split(".");
		String[] host = hostName.split(".");
		for (int i=0;i<pattern.length;i++) {
		    if (pattern[i].equals("{lang}")) {
		        lang = host[i];
		        break;
		    }
		}
		return lang;
	}

	/**
	 *
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public String getAcceptFromURI (String uri) throws IOException{
		String ext = uri.substring(uri.lastIndexOf("."));
		String acceptHeader =  X42TMIMEtype.RDFXML.mimetypevalue();
		switch (ext) {
		case ".jsonld":
			acceptHeader =  X42TMIMEtype.JSONLD.mimetypevalue();
			break;
		case ".rdf":
			acceptHeader =  X42TMIMEtype.RDFXML.mimetypevalue();
			break;
		case ".ttl":
			acceptHeader =  X42TMIMEtype.Turtle.mimetypevalue();
			break;
		default:
			break;
		}
		return acceptHeader;
	}

	/**
	 *
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public boolean isURIWithExtension (String uri) throws IOException{
		String extension = uri.substring(uri.lastIndexOf("."));
		boolean is =  false;
		switch (extension) {
		case ".jsonld":
			is =  true;
			break;
		case ".rdf":
			is =  true;
			break;
		case ".ttl":
			is =  true;
			break;
		default:
			break;
		}
		return is;
	}

	/**
	 *
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public String getURIWithoutExtension (String uri) throws IOException{
		return uri.substring(0,uri.lastIndexOf("."));
	}

}
