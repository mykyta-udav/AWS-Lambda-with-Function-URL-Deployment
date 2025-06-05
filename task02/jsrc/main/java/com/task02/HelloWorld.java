package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<Object, Map<String,Object>> {

	@SuppressWarnings("unchecked")
	public Map<String,Object> handleRequest(Object input, Context context) {
		// Cast the incoming event to a Map
		Map<String,Object> event = (Map<String,Object>) input;

		// Extract path & method (Function URL payload is HTTP API v2.0)
		String path   = "";
		String method = "";

		// 1) If rawPath + requestContext.http.method exist, use them:
		if (event.get("rawPath") != null && event.get("requestContext") instanceof Map) {
			path = (String) event.get("rawPath");
			Map<String,Object> requestContext = (Map<String,Object>) event.get("requestContext");
			Map<String,Object> httpSection    = (Map<String,Object>) requestContext.get("http");
			if (httpSection != null && httpSection.get("method") != null) {
				method = (String) httpSection.get("method");
			}
		}
		// 2) Fallback to API Gateway v1 if needed (rare for Function URLs),
		// but we include it just in case.
		else {
			if (event.get("path") != null) {
				path = (String) event.get("path");
			}
			if (event.get("httpMethod") != null) {
				method = (String) event.get("httpMethod");
			}
		}

		// Prepare the response map:
		Map<String,Object> response = new HashMap<>();

		if ("/hello".equals(path) && "GET".equalsIgnoreCase(method)) {
			// 200 case
			response.put("statusCode", 200);

			// Always set Content-Type if you intend to return JSON
			Map<String,String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			response.put("headers", headers);

			// Put the JSON string into "body"
			String jsonBody = "{\"message\":\"Hello from Lambda\"}";
			response.put("body", jsonBody);
		}
		else {
			// 400 case
			response.put("statusCode", 400);

			Map<String,String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			response.put("headers", headers);

			String errMsg = String.format(
					"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
					path, method
			);
			String jsonBody = String.format("{\"message\":\"%s\"}", escapeForJson(errMsg));
			response.put("body", jsonBody);
		}

		return response;
	}

	// Helper to escape quotes/backslashes inside errMsg if needed
	private String escapeForJson(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}