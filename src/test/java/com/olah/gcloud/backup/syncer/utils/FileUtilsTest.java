package com.olah.gcloud.backup.syncer.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class FileUtilsTest {


    @Test
    public void testFirstLevelFolderOnly() {
        FileUtils fileUtils = new FileUtils();

        Stream<File> foldersRecusrively = fileUtils.getFoldersAtDepth(new File("./src/test/resources/foldersRecusivelyListTestFolders"), 1);

        Assert.assertEquals(Stream.of(new File("./src/test/resources/foldersRecusivelyListTestFolders/firstLevelFolder")), foldersRecusrively);
    }

    @Test
    public void testSecondLevelFoldersOnly() {
        FileUtils fileUtils = new FileUtils();

        Stream<File> foldersRecusrively = fileUtils.getFoldersAtDepth(new File("./src/test/resources/foldersRecusivelyListTestFolders"), 2);

        Assert.assertEquals(Stream.of(
                new File("./src/test/resources/foldersRecusivelyListTestFolders/firstLevelFolder/secondLevelFolder")), foldersRecusrively);
    }

}