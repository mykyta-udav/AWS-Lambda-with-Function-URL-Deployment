package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class HelloWorld implements RequestHandler<Object, Map<String, Object>> {

	@SuppressWarnings("unchecked")
	public Map<String, Object> handleRequest(Object input, Context context) {
		Map<String, Object> event = (Map<String, Object>) input;
		String path;
		String method;

		if (event.get("rawPath") != null &&
				event.get("requestContext") instanceof Map) {
			path = (String) event.get("rawPath");

			Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
			Map<String, Object> httpSection = (Map<String, Object>) requestContext.get("http");
			method = (httpSection != null && httpSection.get("method") != null)
					? (String) httpSection.get("method")
					: "";
		}
		else {
			path = event.get("path") != null
					? (String) event.get("path")
					: "";
			method = event.get("httpMethod") != null
					? (String) event.get("httpMethod")
					: "";
		}

		Map<String, Object> response = new HashMap<>();
		if ("/hello".equals(path) && "GET".equalsIgnoreCase(method)) {
			response.put("statusCode", 200);
			response.put("message", "Hello from Lambda");
		} else {
			response.put("statusCode", 400);
			String msg = String.format(
					"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
					path,
					method
			);
			response.put("message", msg);
		}
		return response;
	}
}