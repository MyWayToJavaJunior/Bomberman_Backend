package rest;

import main.accountservice.AccountService;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import main.UserTokenManager;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.TestOnly;
import org.json.*;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

@Singleton
@Path("user/")
public class Users {

    @Inject
    private main.config.Context context;

    public void setup() {
        if (!wasSetUp) {
            wasSetUp = true;
            @SuppressWarnings("unchecked") final Map<String, String> properties = (Map<String, String>) context.get(Properties.class);

            this.accountService = (AccountService) context.get(AccountService.class);
            this.staticPath = properties.get("static_path");
            this.userpicWidth = Integer.parseInt(properties.get("userpic_width"));
            this.userpicHeight = Integer.parseInt(properties.get("userpic_height"));
        }
    }

    @TestOnly
    public void setContext(@SuppressWarnings("SameParameterValue") main.config.Context context) {
        wasSetUp = false;
        this.context = context;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(String jsonString, @Context HttpHeaders headers){
        setup();
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

        final UserProfile newUser = accountService.createNewUser(login, password);
        if (newUser != null) {
            accountService.loginUser(newUser);
            return Response.ok(new JSONObject().put("id", newUser.getId()).toString()).cookie(UserTokenManager.getNewCookieWithSessionID(newUser.getSessionID())).build();
        }
        else
            return WebErrorManager.accessForbidden("User already exists!");

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        setup();
        final JSONArray responseJSON = new JSONArray();

        final Collection<UserProfile> userData = accountService.getAllUsers();
        if (userData == null)
            return WebErrorManager.serverError();

        for (UserProfile user : userData)
            responseJSON.put(user.toJson());

        return Response.ok(responseJSON.toString()).build();
    }

    @GET
    @Path("top10")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTop10Users() {
        setup();
        final JSONArray responseJSON = new JSONArray();

        final Collection<UserProfile> userData = accountService.getTop10Users();
        if (userData == null)
            return WebErrorManager.serverError();

        for (UserProfile user : userData)
            responseJSON.put(user.toJson());

        return Response.ok(responseJSON.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByID(@PathParam("id") Long id, @Context HttpHeaders headers) {
        setup();
        final UserProfile user = accountService.getUser(id);
        if(user == null){
            return WebErrorManager.accessForbidden();
        }else {
            if (accountService.hasSessionID(UserTokenManager.getSIDStringFromHeaders(headers))) {
                final UserProfile currentUser = accountService.getBySessionID(UserTokenManager.getSIDStringFromHeaders(headers));
                if (currentUser != null)
                    return WebErrorManager.okRaw(user.toJson().toString()).cookie(UserTokenManager.getNewCookieWithSessionID(currentUser.getSessionID())).build();
            }

            return WebErrorManager.ok(user.toJson().toString());
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(String jsonString, @Context HttpHeaders headers){
        setup();
        if (accountService.hasSessionID(UserTokenManager.getSIDStringFromHeaders(headers)))
        {
            final JSONObject jsonRequest;

            try {
                jsonRequest = new JSONObject(jsonString);
            } catch (JSONException ex) {
                return WebErrorManager.badJSON();
            }

            final String login;
            final JSONArray errorList = WebErrorManager.showFieldsNotPresent(jsonRequest, "login");
            if (errorList == null){
                login = jsonRequest.get("login").toString();
            }
            else
                return WebErrorManager.accessForbidden(errorList);


            final UserProfile user = accountService.getUser(login);
            if (user != null){
                final String password = jsonRequest.get("password").toString();
                if (password != null)
                    user.setPassword(password);

                /*final String userpic = jsonRequest.get("userpic64").toString();
                if (userpic != null)
                    user.setUserpicPath(userpic);*/

                accountService.updateUser(user);

                return Response.ok(new JSONObject().put("id", user.getId()).toString()).build();
            }
            else
                return WebErrorManager.serverError("Session exists, but user does not!");

        } else
            return WebErrorManager.authorizationRequired("Not logged in!");
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserPic(HttpServletRequest request, @Context HttpHeaders headers) {
        setup();
        if (accountService.hasSessionID(UserTokenManager.getSIDStringFromHeaders(headers))) {

            final RenderedImage newUserpic;
            //noinspection OverlyBroadCatchBlock
            try {
                final File uploadedUserpic  = (File) request.getPart("userpic");
                final BufferedImage resizableImage = ImageIO.read(uploadedUserpic);

                newUserpic = Scalr.resize(resizableImage, userpicWidth, userpicHeight);

            } catch (Exception ex) {
                return WebErrorManager.badRequest("Could not parse uploaded file!");
            }

            final UserProfile user = accountService.getBySessionID(UserTokenManager.getSIDStringFromHeaders(headers));
            if (user != null) {

                final String path;
                if (user.getUserpicPath() == null)
                    path = staticPath + "user" + user.getId() + ".jpg";
                else
                    path = user.getUserpicPath();

                final File currentUserpic = new File(path);
                try {
                    ImageIO.write(newUserpic, ".jpg", currentUserpic);
                } catch (IOException ex) {
                    WebErrorManager.serverError("Could not save new picture!");
                }
                accountService.updateUserpic(user, path);

                return Response.ok(new JSONObject().put("userpic", user.getId()).toString()).build();
            } else
                return WebErrorManager.serverError("Session exists, but user does not!");

        } else
            return WebErrorManager.authorizationRequired("Not logged in!");
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@PathParam("id") Long id, @Context HttpHeaders headers){
        setup();
        if (accountService.hasSessionID(UserTokenManager.getSIDStringFromHeaders(headers)))
        {
            final UserProfile supplicant = accountService.getBySessionID(UserTokenManager.getSIDStringFromHeaders(headers));
            if (supplicant == null)
                return WebErrorManager.serverError("Session exists, but user does not!");
            if (supplicant.getId() == id) {
                accountService.logoutUser(supplicant.getSessionID());
                accountService.deleteUser(id);
                return WebErrorManager.ok();
            }
            else return WebErrorManager.accessForbidden("Not your user!");

        } else
            return WebErrorManager.authorizationRequired("Not logged in!");
    }


    private AccountService accountService;
    private String staticPath;
    private int userpicWidth;
    private int userpicHeight;

    private boolean wasSetUp = false;
}
