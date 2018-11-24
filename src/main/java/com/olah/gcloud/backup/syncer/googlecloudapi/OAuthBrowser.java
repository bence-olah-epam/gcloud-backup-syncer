package com.olah.gcloud.backup.syncer.googlecloudapi;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;

import java.io.IOException;

public class OAuthBrowser implements AuthorizationCodeInstalledApp.Browser {
    @Override
    public void browse(String url) throws IOException {
        System.out.println("Open url: " + url);
    }
}
