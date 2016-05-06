package rest;

import main.Main;
import main.accountservice.AccountService;
import main.UserTokenManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Singleton
@Path("session/")
public class Sessions {


    private main.config.Context context = Main.context;

    public Sessions() {
        this.accountService = (AccountService) context.get(AccountService.class);;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(String jsonString, @Context HttpHeaders headers){
        accountService.logoutUser(UserTokenManager.getSIDStringFromHeaders(headers));

        final JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (JSONException ex) {
            return WebErrorManager.badJSON();
        }

        final String login;
        final String password;
        final JSONArray errorList = WebErrorManager.showFieldsNotPresent(jsonRequest, "login","password");
        if (errorList == null){
            login = jsonRequest.get("login").toString();
            password = jsonRequest.get("password").toString();
        }
        else
            return WebErrorManager.accessForbidden(errorList);

        final UserProfile loggingUser = accountService.getUser(login);
        if (loggingUser != null) {
            if (loggingUser.getPassword().equals(password))
            {
                accountService.loginUser(loggingUser);
                return Response.ok(new JSONObject().put("id", loggingUser.getId()).toString()).cookie(UserTokenManager.getNewCookieWithSessionID(loggingUser.getSessionID())).build();
            }
            else
                return WebErrorManager.authorizationRequired("Wrong login-password pair!");
        }
        else
            return WebErrorManager.authorizationRequired("Wrong login!");

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isAuthenticated(@Context HttpHeaders headers) {
        if (accountService.hasSessionID(UserTokenManager.getSIDStringFromHeaders(headers))) {
            final UserProfile currentUser = accountService.getBySessionID(UserTokenManager.getSIDStringFromHeaders(headers));
            if (currentUser != null)
                return Response.ok(new JSONObject().put("id", currentUser.getId()).toString()).build();
            else return WebErrorManager.serverError("Session exists, but no user is assigned to.");
        }
        else
            return WebErrorManager.authorizationRequired();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response logoutUser(@Context HttpHeaders headers){
        final String sessionID = UserTokenManager.getSIDStringFromHeaders(headers);
        if (accountService.logoutUser(sessionID))
            return WebErrorManager.okRaw("You have succesfully logged out.").cookie(UserTokenManager.getNewNullCookie()).build();
        else
            return WebErrorManager.okRaw("You was not logged in.").cookie(UserTokenManager.getNewNullCookie()).build();
    }


    private final AccountService accountService;
}
