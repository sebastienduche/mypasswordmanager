package com.passwordmanager.launcher;

import com.sebastienduche.Server;

public class MyPasswordManagerServer extends Server {
    public MyPasswordManagerServer() {
        super("https://github.com/sebastienduche/mypasswordmanager/raw/master/Build/", "MyPasswordManagerVersion.txt", "MyPasswordManager", "MyPasswordManagerDebug");
    }
}
