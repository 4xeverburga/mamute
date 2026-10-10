package org.mamute.auth;

import static org.mamute.model.SanitizedText.fromTrustedText;

import org.mamute.model.MethodType;
import org.mamute.model.SanitizedText;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.common.base.Optional;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GoogleAPI implements SocialAPI{
	private OAuthService service;
	private Token accessToken;
	
	public GoogleAPI(Token accessToken, OAuthService service) {
		this.accessToken = accessToken;
		this.service = service;
	}
	
	public Optional<SignupInfo> getSignupInfo() {
		JsonObject jsonObject = new JsonParser().parse(makeRequest(getAccessToken()).getBody()).getAsJsonObject();
		// oauth2/v3/userinfo (the Google+ API this used to read was shut down in 2019)
		String email = getOrNull(jsonObject, "email");
		String name = getOrNull(jsonObject, "name");
		String photoUrl = getOrNull(jsonObject, "picture");

		SignupInfo signupInfo = new SignupInfo(MethodType.GOOGLE, email, fromTrustedText(name), "", photoUrl);
		return Optional.of(signupInfo);
	}

	private static String getOrNull(JsonObject json, String member) {
		return json.has(member) && !json.get(member).isJsonNull() ? json.get(member).getAsString() : "";
	}

	private Response makeRequest(Token accessToken) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v3/userinfo");
		// Bearer header instead of Scribe's OAuth2 signing, which appends the
		// token as a query parameter Google no longer honours
		request.addHeader("Authorization", "Bearer " + accessToken.getToken());
		return request.send();
	}

	public Token getAccessToken() {
		return accessToken;
	}
}
