package main;

import rest.Sessions;
import rest.Users;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class RestApplication extends Application {
    @Override
    public Set<Object> getSingletons() {
        final HashSet<Object> objects = new HashSet<>();
        final AccountService accountService = (AccountService) Main.getContext().get(AccountService.class);
        objects.add(new Users(accountService));
        objects.add(new Sessions(accountService));
        return objects;
    }
}
