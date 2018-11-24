package com.olah.gcloud.backup.syncer;

import com.google.photos.library.v1.proto.Album;
import com.olah.gcloud.backup.api.DefaultApi;
import com.olah.gcloud.backup.api.model.Photo;
import com.olah.gcloud.backup.api.model.PhotoList;
import com.olah.gcloud.backup.api.model.PhotoQueryRequest;
import com.olah.gcloud.backup.syncer.googlecloudapi.GooglePhotoAPIFacade;
import com.olah.gcloud.backup.syncer.utils.FileUtils;
import com.olah.gcloud.backup.syncer.utils.PhotoLister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyncService {
    private PhotoLister photoLister = new PhotoLister();
    private DefaultApi addOrUpdatePhoto = new DefaultApi();

    private FileUtils fileUtils = new FileUtils();
    private GooglePhotoAPIFacade photoAPIFacade = new GooglePhotoAPIFacade("/Users/bence_olah/projects/credentials.json");

    public SyncService() throws IOException, GeneralSecurityException {

        addOrUpdatePhoto.getApiClient().setBasePath("https://cayr6b3jy5.execute-api.eu-central-1.amazonaws.com/prod");
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        new SyncService().synchronizeFolders(new File("./src/test/resources/syncservicetest"));
    }


    public void synchronizeFolders(File photoRootFolder) throws IOException {
        Stream<File> folders = fileUtils.getFoldersAtDepth(photoRootFolder, 2);

        folders.forEach(folder -> {
            try {
                System.out.println("Synchronizing folder " + folder);
                synchronizeFolder(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void synchronizeFolder(File currentFolder) throws IOException {
        List<String> picturePathInCurrentFolder = photoLister.listPictures(currentFolder.getPath());
        PhotoQueryRequest request = new PhotoQueryRequest();
        String folderPath = getFolderPath(currentFolder);
        request.setFolderPath(folderPath);
        System.out.println("Get photo or folder state: " + request);

        PhotoList photoByFolderAndFileName = addOrUpdatePhoto.getPhotoByFolderAndFileName(request);
        Map<String, Photo> storedPhotos = null;
        if (photoByFolderAndFileName.getData() == null) {
            storedPhotos = new HashMap<>();
        } else {
            storedPhotos = photoByFolderAndFileName.getData().stream().collect(Collectors.toMap(Photo::getFileName, Function.identity()));

        }

        Map<String, Photo> finalStoredPhotos = storedPhotos;
        picturePathInCurrentFolder.stream().forEach(picture -> {
            String fileName = picture.substring(picture.lastIndexOf("/") + 1);
            Photo photo = finalStoredPhotos.get(fileName);
            if (photo == null) {
                System.out.println("Album name: " + currentFolder.getName());
                Album album = photoAPIFacade.getOrCreateAlbum(currentFolder.getName());
                try {
                    photoAPIFacade.uploadPicture(album, new File(picture));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                photo = new Photo();
                photo.setFileName(fileName);
                photo.setFolderPath(folderPath);
                photo.setStatus(Photo.StatusEnum.SYNCED);

                System.out.println("Updating photo state: " + photo);
                addOrUpdatePhoto.addOrUpdatePhoto(photo);
            } else {
                System.out.println("Picture is stored: " + photo.getStatus() + " path:" + photo.getFileName());
            }
        });
    }

    private String getFolderPath(File currentFolder) {
        return currentFolder.getParent() + "." + currentFolder.getName();
    }


    public void setAddOrUpdatePhoto(DefaultApi addOrUpdatePhoto) {
        this.addOrUpdatePhoto = addOrUpdatePhoto;
    }
}
