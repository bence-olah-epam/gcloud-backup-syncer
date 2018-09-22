package com.olah.gcloud.backup.syncer.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhotoLister {

    public List<String> listPictures(String path) throws IOException {
        Stream<String> pathStream = Files.walk(Paths.get(path))
                .filter(Files::isRegularFile).filter(this::isPicture).map(x -> Paths.get(path).relativize(x).toString());
        return pathStream.collect(Collectors.toList());
    }

    private boolean isPicture(Path path) {
        String pathAsString = path.toString();
        return pathAsString.endsWith("jpg")
                || pathAsString.endsWith("jpeg")
                || pathAsString.endsWith("raw")
                || pathAsString.endsWith("cr2");
    }
}
