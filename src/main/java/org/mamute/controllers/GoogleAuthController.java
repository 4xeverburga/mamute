package org.mamute.controllers;

import javax.inject.Inject;

import org.mamute.auth.GoogleAPI;
import org.mamute.auth.SocialAPI;
import org.mamute.model.MethodType;
import org.mamute.qualifiers.Google;
import org.mamute.validators.UrlValidator;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import br.com.caelum.brutauth.auth.annotations.Public;
import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.environment.Environment;

@Public
@Controller
public class GoogleAuthController extends BaseController{
	
	@Inject @Google private OAuthService service;
	@Inject private UrlValidator urlValidator;
	@Inject private LoginMethodManager loginManager;
	@Inject private Environment env;

	// the OAuth callback arrives as /sign-up/google (no trailing slash, as
	// registered in google.redirect_uri); the old route only matched the
	// slashed variant, so the callback fell through to Jetty's 405
	@Get({"/sign-up/google/", "/sign-up/google"})
	public void signUpViaGoogle(String state, String code) {
		Token token = exchangeCodeForToken(code);
		SocialAPI googleAPI = new GoogleAPI(token, service);
	    
		loginManager.merge(MethodType.GOOGLE, googleAPI);
		
	    redirectToRightUrl(state);
	}

	/**
	 * Scribe 1.3.5's Google2Api expects the pre-2014 form-encoded token
	 * response ("access_token=..."), but Google now always answers with JSON,
	 * so service.getAccessToken throws. Exchange the code manually instead.
	 */
	private Token exchangeCodeForToken(String code) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://oauth2.googleapis.com/token");
		request.addBodyParameter("code", code);
		request.addBodyParameter("client_id", env.get("google.client_id"));
		request.addBodyParameter("client_secret", env.get("google.client_secret"));
		request.addBodyParameter("redirect_uri", env.get("host") + env.get("google.redirect_uri"));
		request.addBodyParameter("grant_type", "authorization_code");
		Response response = request.send();
		JsonObject json = new JsonParser().parse(response.getBody()).getAsJsonObject();
		if (!json.has("access_token")) {
			throw new IllegalStateException("Google token exchange failed: " + response.getBody());
		}
		return new Token(json.get("access_token").getAsString(), "");
	}
	
	private void redirectToRightUrl(String state) {
		boolean valid = urlValidator.isValid(state);
		if (!valid) {
			includeAsList("mamuteMessages", i18n("error", "error.invalid.url", state));
		}
        if (state != null && !state.isEmpty() && valid) {
            redirectTo(state);
        } else {
            redirectTo(ListController.class).home(null);
        }
	}
}
