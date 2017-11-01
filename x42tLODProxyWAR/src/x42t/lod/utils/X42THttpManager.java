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
	private static String addParamsToUrl (final HttpServletRequest theReq, final String url) throws UnsupportedEncodingException{
		String completeUrl = url;

		final Enumeration<String> aParamsEnum = theReq.getParameterNames();
		if (aParamsEnum.hasMoreElements()){
			completeUrl = completeUrl + "?";
		}
		while (aParamsEnum.hasMoreElements()) {
			final String aParamName = aParamsEnum.nextElement();
			final String aParamVal = URLEncoder.encode(theReq.getParameter(aParamName), "UTF-8");
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
	private static void copyHeaders (final HttpServletRequest req, final HttpGet httpget, final String acceptHeader) throws UnsupportedEncodingException{
		//headers
		final Enumeration<String> aHeadersEnum = req.getHeaderNames();
		while (aHeadersEnum.hasMoreElements()) {
			final String aHeaderName = aHeadersEnum.nextElement();
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
	private static void copyHeaders (final HttpServletRequest req, final HttpPost httppost) throws UnsupportedEncodingException{
		//headers
		final Enumeration<String> aheadersenum = req.getHeaderNames();
		while (aheadersenum.hasMoreElements()) {
			final String aheadername = aheadersenum.nextElement();
			final String aheaderval = req.getHeader(aheadername);
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
	private static void copyResponseToServletResponse(final HttpResponse response, final HttpServletResponse theResp ) throws IOException{
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
		catch(final IOException ioe) {
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
	public void redirectGetRequest(final HttpServletRequest theReq, final HttpServletResponse theResp, final String url, final String acceptHeader) throws Exception {
		final HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = null;
		HttpResponse response = null;

		if (logger.isDebugEnabled()) logger.debug("> Sparql-proxy: "+ url);

		try {
			//parameters
			final String theReqUrl = addParamsToUrl(theReq, url);
			httpget = new HttpGet(theReqUrl);
			//headers
			copyHeaders(theReq, httpget, acceptHeader);
			if (logger.isDebugEnabled()) logger.debug("executing request " + httpget.getURI());
			// Create a response handler
			response = httpclient.execute(httpget);
			copyResponseToServletResponse(response, theResp);
		} catch (final Exception ex) {
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
	public void redirectPostRequest(final HttpServletRequest theReq, final HttpServletResponse theResp, final String url) throws Exception {
		final HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;
		HttpResponse response = null;

		try {
			httppost = new HttpPost(url);
			final String body = getRequestBody(theReq);
			final HttpEntity entity = new ByteArrayEntity(body.getBytes());
			httppost.setEntity(entity);

			copyHeaders(theReq, httppost);
			if (logger.isDebugEnabled()) logger.debug("executing request " + httppost.getURI());

			response = httpclient.execute(httppost);

			copyResponseToServletResponse(response, theResp);
		} catch (final Exception ex) {
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
	private static String getRequestBody(final HttpServletRequest request) throws IOException {

	    String body = null;
	    final StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        final InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            final char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (final IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (final IOException ex) {
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
	public HttpResponse doGetRequest(final HttpServletRequest theReq, final HttpServletResponse theResp, final String url, final String accept) throws Exception {
		final HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = null;

		String theReqUrl = url;

		if (logger.isDebugEnabled()) logger.debug("> Sparql-proxy: "+ theReqUrl);

		try {
			//parameters
			final Enumeration<String> aParamsEnum = theReq.getParameterNames();
			if (aParamsEnum.hasMoreElements()){
				theReqUrl = theReqUrl + "?";
			}
			while (aParamsEnum.hasMoreElements()) {
				final String aParamName = aParamsEnum.nextElement();
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
			final Enumeration<String> aHeadersEnum = theReq.getHeaderNames();
			while (aHeadersEnum.hasMoreElements()) {
				final String aHeaderName = aHeadersEnum.nextElement();
				final String aHeaderVal = theReq.getHeader(aHeaderName);
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
	public String getLangFromResquest (final HttpServletRequest theReq) throws IOException{
		return getLangFromHostName(theReq.getServerName());
	}

	/**
	 * Gets Lenguage from Hostname
	 * @param hostName
	 * @return lang
	 * @throws IOException
	 */
	private static String getLangFromHostName(final String hostName) throws IOException  {
		String lang = "es";
		final String[] pattern = X42TPropertiesManager.getInstance().getProperty("lod.hostName.pattern").split(".");
		final String[] host = hostName.split(".");
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
	public String getAcceptFromURI (final String uri) throws IOException{
		final String ext = uri.substring(uri.lastIndexOf("."));
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
	public boolean isURIWithExtension (final String uri) throws IOException{
		final String extension = uri.substring(uri.lastIndexOf("."));
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
	public String getURIWithoutExtension (final String uri) throws IOException{
		return uri.substring(0,uri.lastIndexOf("."));
	}

}
