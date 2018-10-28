package com.olah.gcloud.backup.syncer;

import com.olah.gcloud.backup.api.DefaultApi;
import com.olah.gcloud.backup.api.model.PhotoList;
import com.olah.gcloud.backup.syncer.utils.PhotoLister;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SyncService {
    private PhotoLister photoLister = new PhotoLister();
    DefaultApi defaultApi = new DefaultApi();



    public void renameMe(File photoRootFolder) throws IOException {
        List<File> foldersAndSubFolders = getFoldersRecusrively(photoRootFolder, 2);

        List<PhotoList> allPhotos = null;




    }

    private List<File> getFoldersRecusrively(File photoRootFolder, int i) {
        return null;
    }


}
