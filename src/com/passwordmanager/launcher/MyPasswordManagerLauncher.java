package com.passwordmanager.launcher;

import com.sebastienduche.MyLauncher;

import java.io.File;

public class MyPasswordManagerLauncher extends MyLauncher {

    private MyPasswordManagerLauncher() {
        super(new MyPasswordManagerServer(), MyPasswordManagerVersion.getLocalVersion(), "MyPasswordManager.jar");
    }

    @Override
    public void install(File[] files) {

    }

    public static void main(String[] args) {
        new MyPasswordManagerLauncher();
    }
}
