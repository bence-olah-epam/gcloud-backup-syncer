package com.olah.gcloud.backup.syncer.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class PhotoListerTest {

    @Test
    public void testPictureListing() throws IOException {
        PhotoLister photoLister = new PhotoLister();
        List<String> result = photoLister.listPictures("./src/test/resources");

        Assert.assertEquals("images/armadillo.jpg", result.get(0));
    }



}
