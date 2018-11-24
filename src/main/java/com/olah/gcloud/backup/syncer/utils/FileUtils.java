package com.olah.gcloud.backup.syncer.utils;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public Stream<File> getFoldersAtDepth(File photoRootFolder, int depth) {

        if(!photoRootFolder.exists()){
            throw new IllegalArgumentException("Path does not exist:" + photoRootFolder.getAbsolutePath());
        }
        if(depth < 1 ) {
            return Stream.empty();
        }


        if(depth == 1) {
            return Stream.of(photoRootFolder);
        }

        try {
            return Files.list(photoRootFolder.toPath()).filter(Files::isDirectory)
                    .flatMap(file -> getFoldersAtDepth(file.toFile(), depth - 1));
        } catch (IOException e) {
            throw new IllegalArgumentException("Path does not exist:" + photoRootFolder.getAbsolutePath());
        }
    }
}
