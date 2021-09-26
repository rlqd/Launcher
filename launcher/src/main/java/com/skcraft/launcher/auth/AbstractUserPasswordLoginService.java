package com.skcraft.launcher.auth;

import java.io.IOException;

abstract public class AbstractUserPasswordLoginService implements LoginService
{
    public abstract Session login(String id, String password) throws IOException, InterruptedException, AuthenticationException;
}
