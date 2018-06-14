package com.yunji.titan.manager.common;

import java.util.List;
import java.util.Map;

public class HarRequest {
		private String postData;
		private List<Map<String,String>> headers;
		private String url;
		private String cookies;
		private String method;

		public String getPostData() {
			return postData;
		}

		public void setPostData(String postData) {
			this.postData = postData;
		}

		public List<Map<String, String>> getHeaders() {
			return headers;
		}

		public void setHeaders(List<Map<String, String>> headers) {
			this.headers = headers;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getCookies() {
			return cookies;
		}

		public void setCookies(String cookies) {
			this.cookies = cookies;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}

	}