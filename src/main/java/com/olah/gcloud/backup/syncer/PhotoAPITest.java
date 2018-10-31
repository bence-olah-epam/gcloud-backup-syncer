package com.olah.gcloud.backup.syncer;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.Album;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.google.photos.library.v1.PhotosLibrarySettings.newBuilder;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;


public class PhotoAPITest {


    public static void main(String[] args) throws Exception {

        AuthorizationCodeInstalledApp.browse("");

//        com.google.api.gax.core.FixedCredentialsProvider.create();

        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient("/deployments/credentials.json");
//        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient("/Users/bence_olah/projects/credentials.json");


            // Create a new Album  with at title
            Album createdAlbum = client.createAlbum("My Album");

            // Get some properties from the album, such as its ID and product URL
            String id = createdAlbum.getId();
            String url = createdAlbum.getProductUrl();


            System.out.println("******* ID: " + id + " " + url);
    }
}
