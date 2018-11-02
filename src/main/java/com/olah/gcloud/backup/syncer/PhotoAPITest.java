package com.olah.gcloud.backup.syncer;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.Credentials;
import com.google.auth.oauth2.OAuth2Credentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.*;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.rpc.Code;
import com.google.rpc.Status;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import static com.google.photos.library.v1.PhotosLibrarySettings.newBuilder;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;


public class PhotoAPITest {


    public static void main(String[] args) throws Exception {

        AuthorizationCodeInstalledApp.browse("");

//        com.google.api.gax.core.FixedCredentialsProvider.create();

//        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient("/deployments/credentials.json");
        PhotosLibraryClient client = PhotosLibraryClientFactory.createClient("/Users/bence_olah/projects/credentials.json");


            // Create a new Album  with at title
            Album createdAlbum = client.createAlbum("Test");
        UploadMediaItemRequest.Builder uploadRequestBuilder = UploadMediaItemRequest.newBuilder();
        uploadRequestBuilder
                .setFileName("happy.jpeg")
                .setDataFile(new RandomAccessFile(new File("src/main/resources/happy.jpeg").getAbsolutePath(), "r"));
        ApiFuture<UploadMediaItemResponse> uploadResponseFuture =
                client.uploadMediaItemCallable().futureCall(uploadRequestBuilder.build());

        UploadMediaItemResponse uploadMediaItemResponse = uploadResponseFuture.get();


        try {
            // Create a NewMediaItem with the uploadToken obtained from the previous upload request, and a description
            NewMediaItem newMediaItem = NewMediaItemFactory
                    .createNewMediaItem(uploadMediaItemResponse.getUploadToken().get(), "uploading a file");
            List<NewMediaItem> newItems = Arrays.asList(newMediaItem);

            //todo upload to an album
            BatchCreateMediaItemsResponse response = client.batchCreateMediaItems(createdAlbum.getId(),newItems);
            for (NewMediaItemResult itemsResponse : response.getNewMediaItemResultsList()) {
                Status status = itemsResponse.getStatus();
                if (status.getCode() == Code.OK_VALUE) {
                    // The item is successfully created in the user's library
                    MediaItem createdItem = itemsResponse.getMediaItem();
                } else {
                    // The item could not be created. Check the status and try again
                }
            }
        } catch (ApiException e) {
            // Handle error
        }



        System.out.println("******upload token: " + uploadMediaItemResponse.getUploadToken().get());

        // Get some properties from the album, such as its ID and product URL
            String id = createdAlbum.getId();
            String url = createdAlbum.getProductUrl();


            System.out.println("******* ID: " + id + " " + url);
    }
}
