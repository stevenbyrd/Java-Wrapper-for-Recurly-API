package Recurly.Client;


import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.RecurlyException;
import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.RecurlyUtil;
import com.cloudsnap.WorkflowCore.ClientLibraries.Recurly.Util.ResponsePage;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class RecurlyClient
{
	public	static String			apiKey;
	private static RecurlyClient	staticClient;

	public static RecurlyClient getInstance() throws RecurlyException
	{
		if(staticClient == null)
		{
			if(RecurlyUtil.nullOrEmpty(apiKey))
			{
				throw new RecurlyException("The recurly client must be configured with an api key before an instance can be created!");
			}

			staticClient = new RecurlyClient(apiKey);
		}

		return staticClient;
	}

	private RecurlyClient(String apiKey)
	{
		this.apiKey = apiKey;
	}


	private ResponsePage request(HttpRequestBase method, Map<String, String> headers)
	{
		ResponsePage retVal = new ResponsePage();

		try
		{
			DefaultHttpClient 	client 			= new DefaultHttpClient();
			HttpResponse 		response		= client.execute(this.configureHttpRequestHeaders(method, headers));
			String 				responseBody	= (response.getEntity() == null) ? "" : EntityUtils.toString(response.getEntity());

			retVal.setResponseText(responseBody);
			retVal.setPageHeaders(response.getHeaders("Link"));
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return retVal;
	}
	

	public ResponsePage get(String endpoint, Map<String, String> headers, Map<String, String> parameters)
	{
		HttpGet get = new HttpGet(addParameters(endpoint, parameters));

		return this.request(get, headers);
	}


	public ResponsePage post(String endpoint, Map<String, String> headers, Map<String, String> parameters, String body)
	{
		HttpPost post = new HttpPost(addParameters(endpoint, parameters));

		if(RecurlyUtil.nullOrEmpty(body) == false)
		{
			try
			{
				post.setEntity(this.makeEntity(body));
			}
			catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		return this.request(post, headers);
	}
	
	
	public ResponsePage put(String endpoint, Map<String, String> headers, Map<String, String> parameters, String body)
	{
		HttpPut put = new HttpPut(addParameters(endpoint, parameters));

		if(RecurlyUtil.nullOrEmpty(body) == false)
		{
			try
			{
				put.setEntity(this.makeEntity(body));
			}
			catch(UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}

		return this.request(put, headers);
	}


	public ResponsePage delete(String endpoint, Map<String, String> headers, Map<String, String> parameters)
	{
		HttpDelete delete = new HttpDelete(addParameters(endpoint, parameters));

		return this.request(delete, headers);
	}
	
	
	private StringEntity makeEntity(String body) throws UnsupportedEncodingException
	{
		StringEntity entity = new StringEntity(body, "UTF-8")
		{
			@Override
			public Header getContentType()
			{
				Header h = new BasicHeader("Content-Type", "application/xml");
				return h;
			}
		};

		return entity;
	}


	private static String addParameters(String endpoint, Map<String, String> parameters)
	{
		if(parameters != null)
		{
			Set<String> keySet = new HashSet<String>();

			keySet.addAll(parameters.keySet());

			for(String key : keySet)
			{
				if((parameters.get(key) == null) || parameters.get(key).equals(""))
				{
					parameters.remove(key);
				}
			}

			if(parameters.size() > 0)
			{
				if(endpoint.contains("?") == false)
				{
					endpoint = endpoint + "?";
				}

				for(String key : parameters.keySet())
				{
					if(endpoint.contains(key + "=") == false)
					{
						endpoint = endpoint + key + "=" + parameters.get(key) + "&";
					}
				}

				if(endpoint.endsWith("&"))
				{
					endpoint = endpoint.substring(0, endpoint.length() - 1);
				}
			}
		}

		return endpoint;
	}


	private HttpRequestBase configureHttpRequestHeaders(HttpRequestBase method, Map<String, String> headers)
	{
		method.setHeader("Authorization", "Basic " + this.apiKey);
		method.setHeader("Accept", "application/xml");
		method.setHeader("Content-Type","application/xml; charset=utf-8");

		if((headers != null) && (headers.size() > 0))
		{
			for(String key : headers.keySet())
			{
					method.setHeader(key, headers.get(key));
			}
		}

		return method;
	}
}
