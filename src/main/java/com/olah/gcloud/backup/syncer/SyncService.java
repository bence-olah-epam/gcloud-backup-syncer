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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

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
//        new SyncService().synchronizeFolders(new File("/Volumes/photo"));
    }


    public void synchronizeFolders(File photoRootFolder) throws IOException {
        Stream<File> folders = fileUtils.getFoldersAtDepth(photoRootFolder, 2);

        folders.filter(y -> y.toString().contains("2018")).forEach(folder -> {
            try {
                synchronizeSubFolder(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void synchronizeSubFolder(File yearFolder) throws IOException {
        File[] albums = yearFolder.listFiles(File::isDirectory);

        for (File albumFile: albums) {
            String albumName = albumFile.getParentFile().getName() + "." + albumFile.getName();
            System.out.println("************************************************************");
            System.out.println("Synchronizing album: " + albumName + " Folder: " + albumFile);

            List<String> picturePathInCurrentFolder = photoLister.listPictures(albumFile.getPath());
            System.out.println("Number of files in the folder: " + picturePathInCurrentFolder.size());

            PhotoQueryRequest request = new PhotoQueryRequest();
            request.setFolderPath(albumName);
            request.setStatus(PhotoQueryRequest.StatusEnum.SYNCED);
            System.out.println("Checking photo statuses: " + request);

            PhotoList photoByFolderAndFileName = addOrUpdatePhoto.getPhotoByFolderAndFileName(request);

            Map<String, Photo> storedPhotos = null;
            if (photoByFolderAndFileName.getData() == null) {
                storedPhotos = new HashMap<>();
            } else {
                storedPhotos = photoByFolderAndFileName.getData().stream().collect(Collectors.toMap(Photo::getFileName, Function.identity()));
                System.out.println("Statistics about stored photos:");
                photoByFolderAndFileName.getData().stream().collect(groupingBy(Photo::getStatus)).forEach( (key,value) -> {
                    System.out.println("Key: " + key + " count: " + value.size());
                });
            }

            Map<String, Photo> finalStoredPhotos = storedPhotos;
            picturePathInCurrentFolder.stream().forEach(picture -> {
                String fileName = picture.substring(picture.lastIndexOf("/") + 1);
                Photo photo = finalStoredPhotos.get(fileName);
                if (photo == null || !photo.getStatus().equals(Photo.StatusEnum.SYNCED)) {
                    Album album = photoAPIFacade.getOrCreateAlbum(albumName);
                    try {
                        photoAPIFacade.uploadPicture(album, new File(albumFile.toPath().toAbsolutePath() + File.separator + picture));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    photo = new Photo();
                    photo.setFileName(fileName);
                    photo.setFolderPath(albumName);
                    photo.setStatus(Photo.StatusEnum.SYNCED);

                    System.out.println("Updating photo state: " + photo);
                    addOrUpdatePhoto.addOrUpdatePhoto(photo);
                } else {
                    System.out.println("Picture is stored: " + photo.getStatus() + " path:" + photo.getFileName() + "album:" + albumName);
                }
            });
        }


    }

    public void setAddOrUpdatePhoto(DefaultApi addOrUpdatePhoto) {
        this.addOrUpdatePhoto = addOrUpdatePhoto;
    }
}
