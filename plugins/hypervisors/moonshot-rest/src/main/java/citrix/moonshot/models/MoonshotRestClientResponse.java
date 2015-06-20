package citrix.moonshot.models;

import org.apache.http.HttpResponse;
import org.json.JSONObject;

public class MoonshotRestClientResponse {

	private HttpResponse httpResponse = null;

	private JSONObject jsonResponse = null;

	private Exception exception = null;

	private boolean successful;

	public MoonshotRestClientResponse(HttpResponse httpResponse,
			JSONObject jsonResponse, Exception exception) {
		this.httpResponse = httpResponse;
		this.jsonResponse = jsonResponse;
		this.exception = exception;
		if (httpResponse == null || jsonResponse == null || exception != null) {
			successful = false;
		} else {
			successful = true;
		}
	}

	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	public JSONObject getJsonResponse() {
		return jsonResponse;
	}

	public Exception getException() {
		return exception;
	}

	public boolean isSuccessful() {
		return successful;
	}

}
