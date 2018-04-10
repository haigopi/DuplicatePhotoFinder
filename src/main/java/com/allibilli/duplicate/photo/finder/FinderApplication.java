package com.allibilli.duplicate.photo.finder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

@SpringBootApplication
@Slf4j
public class FinderApplication {

    @Autowired
    ReadPhotosFrom readPhotosFrom;

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(FinderApplication.class, args);

        FinderApplication application = context.getBean(FinderApplication.class);
        application.start();
    }

    public void start() throws Exception {

        String readDir = "J:\\New";

        String duplicateDir = "H:\\Duplicates\\duplicatePhotos";
        String regularDir = "G:\\Photos\\regularPhotos";
        String movies = "G:\\Movies";

        readPhotosFrom.setDuplicateDir(new File(duplicateDir));
        readPhotosFrom.setRegularDir(new File(regularDir));
        readPhotosFrom.setMoviesDir(new File(movies));
        readPhotosFrom.setToIncludeSubDirs(true);

        readPhotosFrom.startReading(new File(readDir));
        log.info("Total Count : {}" , readPhotosFrom.getTotalCount());
    }

}
