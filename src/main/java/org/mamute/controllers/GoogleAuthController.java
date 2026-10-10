package org.mamute.controllers;

import javax.inject.Inject;

import org.mamute.auth.GoogleAPI;
import org.mamute.auth.SocialAPI;
import org.mamute.model.MethodType;
import org.mamute.qualifiers.Google;
import org.mamute.validators.UrlValidator;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import br.com.caelum.brutauth.auth.annotations.Public;
import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;

@Public
@Controller
public class GoogleAuthController extends BaseController{
	
	@Inject @Google private OAuthService service;
	@Inject private UrlValidator urlValidator;
	@Inject private LoginMethodManager loginManager;

	// the OAuth callback arrives as /sign-up/google (no trailing slash, as
	// registered in google.redirect_uri); the old route only matched the
	// slashed variant, so the callback fell through to Jetty's 405
	@Get({"/sign-up/google/", "/sign-up/google"})
	public void signUpViaGoogle(String state, String code) {
		Token token = service.getAccessToken(null, new Verifier(code));
		SocialAPI googleAPI = new GoogleAPI(token, service);
	    
		loginManager.merge(MethodType.GOOGLE, googleAPI);
		
	    redirectToRightUrl(state);
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
