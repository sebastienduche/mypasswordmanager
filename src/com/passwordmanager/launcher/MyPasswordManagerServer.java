package com.passwordmanager.launcher;

import com.sebastienduche.Server;

public class MyPasswordManagerServer extends Server {

    private static final MyPasswordManagerServer INSTANCE = new MyPasswordManagerServer();

    private MyPasswordManagerServer() {
        super("https://github.com/sebastienduche/mypasswordmanager/raw/master/Build/", "MyPasswordManagerVersion.txt", "MyPasswordManager", "MyPasswordManagerDebug");
    }

    public static MyPasswordManagerServer getInstance() {
        return INSTANCE;
    }

    public static void Debug(String sText) {
        getInstance().debug(sText);
    }
}
