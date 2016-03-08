package com.daolicloud.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.daolicloud.bean.Image;


public class ImageAuthtencation {

	private String preurl;
	private String token;
	
	public ImageAuthtencation(String ip) {
		this.preurl = "http://" + ip;
	}
	
	public String getToken() {
		return token;
	}
	
	private Map<Integer, String> response(String method, String url, String data) {
		System.out.println("Body => " + data);
		Map<Integer, String> map = new HashMap<Integer, String>();
		HttpClient httpClient = new HttpClient();
		if("post".equalsIgnoreCase(method)) {
			PostMethod post = new PostMethod(url);
			post.setRequestHeader("Content-type", "application/json");
			System.out.println("URL => " + url);
			post.setRequestBody(data);
			try {
				int statusCode = httpClient.executeMethod(post);
				System.out.println("Stauts => " + statusCode);
				map.put(statusCode, post.getResponseBodyAsString());
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			GetMethod get = new GetMethod(url);
			get.setRequestHeader("Content-type", "application/json");
			get.addRequestHeader("X-Auth-Token", data);
			try {
				int statusCode = httpClient.executeMethod(get);
				System.out.println("Stauts => " + statusCode);
				map.put(statusCode, get.getResponseBodyAsString());
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	private Map<String, String> getURL(JSONArray catalog) {
		Map<String, String> catalogURL = new HashMap<String, String>(); 
		for(int i=0;i < catalog.size();i++) {
			String type = catalog.getJSONObject(i).getString("type");
			String url = catalog.getJSONObject(i).getJSONArray("endpoints").
					getJSONObject(0).getString("publicURL");
			catalogURL.put(type, url);
		}
		return catalogURL;
	}
	
	public boolean authentication(String name, String password) {
		String data = "{\"auth\":" +
				"{\"tenantName\":\"" + "admin" + "\"," +
						"\"passwordCredentials\": {" +
						"\"username\":\"" + name + "\"," +
						"\"password\":\"" + password + "\"" +
				"}}}";
		String url = preurl + ":5000/v2.0/tokens";
		Map<Integer, String> response = response("post", url, data);
		if(response.containsKey(HttpStatus.SC_OK)) {
			JSONObject jsonObject = JSONObject.fromObject(response.get(HttpStatus.SC_OK));
			//JSONObject jsonObject = JSONObject.fromObject("{\"access\":{\"token\":{\"id\":\"MIINugYJKoZIhvcNAQcCoIINqzCCDacCAQExCTAHBgUrDgMCGjCCDJMGCSqGSIb3DQEHAaCCDIQEggyAeyJhY2Nlc3MiOiB7InRva2VuIjogeyJpc3N1ZWRfYXQiOiAiMjAxMy0xMC0zMFQwMjoyNjo1Ny44MzU0MjAiLCAiZXhwaXJlcyI6ICIyMDEzLTEwLTMxVDAyOjI2OjU3WiIsICJpZCI6ICJwbGFjZWhvbGRlciIsICJ0ZW5hbnQiOiB7ImRlc2NyaXB0aW9uIjogImFkbWluIHRlbmFudCIsICJlbmFibGVkIjogdHJ1ZSwgImlkIjogIjg3YjZlZDA3MDUxMTQzZDE5NGIwYTI2OTA3ZDU3Y2RiIiwgIm5hbWUiOiAiYWRtaW4ifX0sICJzZXJ2aWNlQ2F0YWxvZyI6IFt7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xMC4wLjMuMjE0Ojg3NzQvdjIvODdiNmVkMDcwNTExNDNkMTk0YjBhMjY5MDdkNTdjZGIiLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4Nzc0L3YyLzg3YjZlZDA3MDUxMTQzZDE5NGIwYTI2OTA3ZDU3Y2RiIiwgImlkIjogImUzODQzNjg2ODlhODRjNTI4YTBhOTNlNzg3MDIyN2RiIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4Nzc0L3YyLzg3YjZlZDA3MDUxMTQzZDE5NGIwYTI2OTA3ZDU3Y2RiIn1dLCAiZW5kcG9pbnRzX2xpbmtzIjogW10sICJ0eXBlIjogImNvbXB1dGUiLCAibmFtZSI6ICJub3ZhIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6OTY5Ni8iLCAicmVnaW9uIjogIlJlZ2lvbk9uZSIsICJpbnRlcm5hbFVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo5Njk2LyIsICJpZCI6ICI0NWIwZTNhYzg2NTQ0MDE4YjlmMDhmODgwMmZjZTRjYiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6OTY5Ni8ifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAibmV0d29yayIsICJuYW1lIjogInF1YW50dW0ifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4MDgwIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODA4MCIsICJpZCI6ICI2OTU4NzIzNTcyNzI0MmE5YWNjNDkwN2ZmYjgxOGJhNyIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODA4MCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJzMyIsICJuYW1lIjogInN3aWZ0X3MzIn0sIHsiZW5kcG9pbnRzIjogW3siYWRtaW5VUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6OTI5MiIsICJyZWdpb24iOiAiUmVnaW9uT25lIiwgImludGVybmFsVVJMIjogImh0dHA6Ly8xMC4wLjMuMjE0OjkyOTIiLCAiaWQiOiAiNmEzNmFhY2ZlODBhNDlmOWFkYTNlODcyMjEzOTdjYTAiLCAicHVibGljVVJMIjogImh0dHA6Ly8xMC4wLjMuMjE0OjkyOTIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaW1hZ2UiLCAibmFtZSI6ICJnbGFuY2UifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vbG9jYWxob3N0Ojg3NzcvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovL2xvY2FsaG9zdDo4Nzc3LyIsICJpZCI6ICIyOGIyNDEzYWNiNTg0NTdmOGQ5YzA3N2RjNzZiZTkxNyIsICJwdWJsaWNVUkwiOiAiaHR0cDovL2xvY2FsaG9zdDo4Nzc3LyJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJtZXRlcmluZyIsICJuYW1lIjogImNlaWxvbWV0ZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4Nzc2L3YxLzg3YjZlZDA3MDUxMTQzZDE5NGIwYTI2OTA3ZDU3Y2RiIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODc3Ni92MS84N2I2ZWQwNzA1MTE0M2QxOTRiMGEyNjkwN2Q1N2NkYiIsICJpZCI6ICI1YzdiMTk2NDQ0YjY0ZTkxYTlmY2UyMGI4YmRlNzZkNSIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODc3Ni92MS84N2I2ZWQwNzA1MTE0M2QxOTRiMGEyNjkwN2Q1N2NkYiJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJ2b2x1bWUiLCAibmFtZSI6ICJjaW5kZXIifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4NzczL3NlcnZpY2VzL0FkbWluIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODc3My9zZXJ2aWNlcy9DbG91ZCIsICJpZCI6ICJiMzdmNGYyYWU3YTg0ZmNhYTM5ZGFmNTlmNWEzMTQ4NiIsICJwdWJsaWNVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODc3My9zZXJ2aWNlcy9DbG91ZCJ9XSwgImVuZHBvaW50c19saW5rcyI6IFtdLCAidHlwZSI6ICJlYzIiLCAibmFtZSI6ICJub3ZhX2VjMiJ9LCB7ImVuZHBvaW50cyI6IFt7ImFkbWluVVJMIjogImh0dHA6Ly8xMC4wLjMuMjE0OjgwODAvIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6ODA4MC92MS9BVVRIXzg3YjZlZDA3MDUxMTQzZDE5NGIwYTI2OTA3ZDU3Y2RiIiwgImlkIjogIjc4YzUwMjc0MjE2YjQ5YjI5Nzg3NjcxZmVkZmNmMTVjIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo4MDgwL3YxL0FVVEhfODdiNmVkMDcwNTExNDNkMTk0YjBhMjY5MDdkNTdjZGIifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAib2JqZWN0LXN0b3JlIiwgIm5hbWUiOiAic3dpZnQifSwgeyJlbmRwb2ludHMiOiBbeyJhZG1pblVSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDozNTM1Ny92Mi4wIiwgInJlZ2lvbiI6ICJSZWdpb25PbmUiLCAiaW50ZXJuYWxVUkwiOiAiaHR0cDovLzEwLjAuMy4yMTQ6NTAwMC92Mi4wIiwgImlkIjogIjdjMzdkM2M3OTM0MDQzYWI5M2JiNDU0MGEwNmM5MThkIiwgInB1YmxpY1VSTCI6ICJodHRwOi8vMTAuMC4zLjIxNDo1MDAwL3YyLjAifV0sICJlbmRwb2ludHNfbGlua3MiOiBbXSwgInR5cGUiOiAiaWRlbnRpdHkiLCAibmFtZSI6ICJrZXlzdG9uZSJ9XSwgInVzZXIiOiB7InVzZXJuYW1lIjogImFkbWluIiwgInJvbGVzX2xpbmtzIjogW10sICJpZCI6ICJjOGY0NGRhM2JiOTc0YmM0YmFiYWMxOGI3YTlkMGIyMSIsICJyb2xlcyI6IFt7Im5hbWUiOiAiYWRtaW4ifV0sICJuYW1lIjogImFkbWluIn0sICJtZXRhZGF0YSI6IHsiaXNfYWRtaW4iOiAwLCAicm9sZXMiOiBbIjMxYjhmMGU1OWRhOTRmMTg5ZjU0ODc4NjVkMjM3YzExIl19fX0xgf8wgfwCAQEwXDBXMQswCQYDVQQGEwJVUzEOMAwGA1UECBMFVW5zZXQxDjAMBgNVBAcTBVVuc2V0MQ4wDAYDVQQKEwVVbnNldDEYMBYGA1UEAxMPd3d3LmV4YW1wbGUuY29tAgEBMAcGBSsOAwIaMA0GCSqGSIb3DQEBAQUABIGARDQW6+ibE+0oZcSalyGlIP6kSxR59v5Yr82jWaJU7TaE3q4+R68oHnN9LxTWQVNYsXWH3YBGrzlwqbG81zHZ5R4enKL+yHVxikFC3kaRieqyvsOVG7rAydcVT+HguAsW3Bkcx0gtEcbAZ8Zg4Je9VWVaGqx7ngfGR83kzAi-3m0=\"" +
			//		"},\"serviceCatalog\":[{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:8774/v2/87b6ed07051143d194b0a26907d57cdb\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:8774/v2/87b6ed07051143d194b0a26907d57cdb\",\"id\":\"e384368689a84c528a0a93e7870227db\",\"publicURL\":\"http://10.0.3.214:8774/v2/87b6ed07051143d194b0a26907d57cdb\"}],\"endpoints_links\":[],\"type\":\"compute\",\"name\":\"nova\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:9696/\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:9696/\",\"id\":\"45b0e3ac86544018b9f08f8802fce4cb\",\"publicURL\":\"http://10.0.3.214:9696/\"}],\"endpoints_links\":[],\"type\":\"network\",\"name\":\"quantum\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:8080\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:8080\",\"id\":\"69587235727242a9acc4907ffb818ba7\",\"publicURL\":\"http://10.0.3.214:8080\"}],\"endpoints_links\":[],\"type\":\"s3\",\"name\":\"swift_s3\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:9292\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:9292\",\"id\":\"6a36aacfe80a49f9ada3e87221397ca0\",\"publicURL\":\"http://10.0.3.214:9292\"}],\"endpoints_links\":[],\"type\":\"image\",\"name\":\"glance\"},{\"endpoints\":[{\"adminURL\":\"http://localhost:8777/\",\"region\":\"RegionOne\",\"internalURL\":\"http://localhost:8777/\",\"id\":\"28b2413acb58457f8d9c077dc76be917\",\"publicURL\":\"http://localhost:8777/\"}],\"endpoints_links\":[],\"type\":\"metering\",\"name\":\"ceilometer\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:8776/v1/87b6ed07051143d194b0a26907d57cdb\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:8776/v1/87b6ed07051143d194b0a26907d57cdb\",\"id\":\"5c7b196444b64e91a9fce20b8bde76d5\",\"publicURL\":\"http://10.0.3.214:8776/v1/87b6ed07051143d194b0a26907d57cdb\"}],\"endpoints_links\":[],\"type\":\"volume\",\"name\":\"cinder\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:8773/services/Admin\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:8773/services/Cloud\",\"id\":\"b37f4f2ae7a84fcaa39daf59f5a31486\",\"publicURL\":\"http://10.0.3.214:8773/services/Cloud\"}],\"endpoints_links\":[],\"type\":\"ec2\",\"name\":\"nova_ec2\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:8080/\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:8080/v1/AUTH_87b6ed07051143d194b0a26907d57cdb\",\"id\":\"78c50274216b49b29787671fedfcf15c\",\"publicURL\":\"http://10.0.3.214:8080/v1/AUTH_87b6ed07051143d194b0a26907d57cdb\"}],\"endpoints_links\":[],\"type\":\"object-store\",\"name\":\"swift\"},{\"endpoints\":[{\"adminURL\":\"http://10.0.3.214:35357/v2.0\",\"region\":\"RegionOne\",\"internalURL\":\"http://10.0.3.214:5000/v2.0\",\"id\":\"7c37d3c7934043ab93bb4540a06c918d\",\"publicURL\":\"http://10.0.3.214:5000/v2.0\"}],\"endpoints_links\":[],\"type\":\"identity\",\"name\":\"keystone\"}]}}");

			JSONObject accessObj = jsonObject.getJSONObject("access");
			token = accessObj.getJSONObject("token").getString("id");
			JSONArray catalog = accessObj.getJSONArray("serviceCatalog");

			Map<String, String> urlMap = getURL(catalog);
			System.out.println(token);
			System.out.println(urlMap.get("image"));
			return true;
		}
		return false;
	}
	
	public List<Image> getImages() {
		List<Image> images = new ArrayList<Image>();
		//ip = "122.49.1.174";
		String url = preurl +":9292/v2/images";
		if(token != null || !"".equals(token)) {
			Map<Integer, String> response = response("get", url, token);
			if(response.containsKey(HttpStatus.SC_OK)) {
				JSONObject jsonObject = JSONObject.fromObject(response.get(HttpStatus.SC_OK));
				JSONArray imgArr = jsonObject.getJSONArray("images");
				for(int i=0;i<imgArr.size();i++) {
					JSONObject imgObj = imgArr.getJSONObject(i);
					Image img = new Image();
					img.setUid(imgObj.getString("id"));
					img.setName(imgObj.getString("name"));
					img.setCname(imgObj.getString("name"));
					img.setStatus(imgObj.getString("status"));
					img.setFile(preurl + ":9292" + imgObj.getString("file"));
					img.setSize(imgObj.getLong("size"));
					img.setVisibility("public".equals(imgObj.getString("visibility")));
					images.add(img);
				}
			}
		}
		return images;
	}
	
}
