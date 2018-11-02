package com.olah.gcloud.backup.syncer;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.common.collect.ImmutableList;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * A factory class that helps initialize a {@link PhotosLibraryClient} instance.
 */
public class PhotosLibraryClientFactory {


    private static final URL resource = PhotosLibraryClientFactory.class.getResource("/");
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File("tmp", "credentials");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final int LOCAL_RECEIVER_PORT = 61984;

    private static final List<String> REQUIRED_SCOPES =
            ImmutableList.of(
                    "https://www.googleapis.com/auth/photoslibrary.readonly",
                    "https://www.googleapis.com/auth/photoslibrary.appendonly");

    private PhotosLibraryClientFactory() {
    }

    /**
     * Creates a new {@link PhotosLibraryClient} instance with credentials and scopes.
     */
    public static PhotosLibraryClient createClient(
            String credentialsPath)
            throws IOException, GeneralSecurityException {
        PhotosLibrarySettings settings =
                PhotosLibrarySettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(
                                        getUserCredentials(credentialsPath, REQUIRED_SCOPES)))
                        .build();
        return PhotosLibraryClient.initialize(settings);
    }


    /**
     * Creates a new {@link PhotosLibraryClient} instance with credentials and scopes.
     */
    public static PhotosLibraryClient createClient(
            String credentialsPath, List<String> selectedScopes)
            throws IOException, GeneralSecurityException {
        PhotosLibrarySettings settings =
                PhotosLibrarySettings.newBuilder()
                        .setCredentialsProvider(
                                FixedCredentialsProvider.create(
                                        getUserCredentials(credentialsPath, selectedScopes)))
                        .build();
        return PhotosLibraryClient.initialize(settings);
    }

    private static Credentials getUserCredentials(String credentialsPath, List<String> selectedScopes)
            throws IOException, GeneralSecurityException {
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(
                        JSON_FACTORY, new InputStreamReader(new FileInputStream(credentialsPath)));
        String clientId = clientSecrets.getDetails().getClientId();
        String clientSecret = clientSecrets.getDetails().getClientSecret();

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JSON_FACTORY,
                        clientSecrets,
                        selectedScopes)
                        .setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
                        .setAccessType("offline")
                        .build();

        VerificationCodeReceiver receiver = new VerificationCodeReceiver.Builder().setRedirectHost("localhost").setServerHost("0.0.0.0").setPort(LOCAL_RECEIVER_PORT).build();


        AuthorizationCodeInstalledApp.Browser browser = new OAuthBrowser();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver, browser).authorize("user");
        return UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(credential.getRefreshToken())
                .build();
    }
}