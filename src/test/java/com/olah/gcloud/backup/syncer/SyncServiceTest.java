package com.olah.gcloud.backup.syncer;

import com.olah.gcloud.backup.api.DefaultApi;
import com.olah.gcloud.backup.api.model.Photo;
import com.olah.gcloud.backup.api.model.PhotoList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SyncServiceTest {
    private SyncService syncService= new SyncService();

    @Mock
    DefaultApi defaultApi;

    @Before
    public void setup(){
        syncService.setAddOrUpdatePhoto(defaultApi);
    }

    @Test
    public void testSyncing() throws IOException {
        PhotoList photoList = new PhotoList();
        List<Photo> data = new ArrayList();
        data.add(createPhoto(Photo.StatusEnum.NOT_SYNCED, "2018.2018_rtp", "image_to_be_found1.jpeg"));
        data.add(createPhoto(Photo.StatusEnum.NOT_SYNCED, "2018.2018_rtp.nested_folder", "image_to_be_found2.jpeg"));

        photoList.setData(data);

        when(defaultApi.getPhotoByFolderAndFileName(anyObject())).thenReturn(photoList);
        syncService.synchronizeFolders(new File("./src/test/resources/syncservicetest"));
    }

    private Photo createPhoto(Photo.StatusEnum status, String folderPath, String fileName) {
        Photo result = new Photo();
        result.setStatus(status);
        result.setFolderPath(folderPath);
        result.setFileName(fileName);
        return result;
    }
}