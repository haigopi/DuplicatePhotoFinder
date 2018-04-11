package com.allibilli.duplicate.photo.finder.structure;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Component
@Data
@Slf4j
public abstract class SaveFile {
    enum MOVIE_EXTENSTIONS {
        MOV, mov, WMV, wmv, mp4, MP4
    }

    enum IMAGE_EXTENSIONS {
        JPG, jpg, JPEG, jpeg, png, gif, GIF, PNG
    }

    public Path getPath(String fileName) {

        String fileExtension = FilenameUtils.getExtension(fileName);
        log.debug("Extension Found: {}", fileExtension);
        try {
            IMAGE_EXTENSIONS image_extension = IMAGE_EXTENSIONS.valueOf(fileExtension);
            if (!Objects.isNull(image_extension)) {
                return Paths.get(imagePathFile.getAbsolutePath() + File.separator + fileName);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Not a Image: {}", fileName);
        }
        try {
            MOVIE_EXTENSTIONS movie_extenstion = MOVIE_EXTENSTIONS.valueOf(fileExtension);
            if (!Objects.isNull(movie_extenstion)) {
                return Paths.get(moviePathFile.getAbsolutePath() + File.separator + fileName);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Not a Movie: {}", fileName);
        }

        return Paths.get(miscPathFile.getAbsolutePath() + File.separator + fileName);
    }

    public Path getDuplicateFilePath(String fileName) {
        return Paths.get(duplicateDirPathFile.getAbsolutePath() + File.separator + fileName);
    }

    @Value("${paths.imagePath}")
    public String imagePath;

    @Value("${paths.duplicateDirPath}")
    public String duplicateDirPath;

    @Value("${paths.sourcePath}")
    public String sourcePath;

    @Value("${paths.miscPath}")
    public String miscPath;

    @Value("${paths.moviePath}")
    public String moviePath;

    @Value("${paths.includeSubDirs: false}")
    private boolean toIncludeSubDirs;

    public File imagePathFile;
    public File duplicateDirPathFile;
    public File sourcePathFile;
    public File miscPathFile;
    public File moviePathFile;


    @PostConstruct
    public void init() {
        imagePathFile = new File(imagePath);
        duplicateDirPathFile = new File(duplicateDirPath);
        sourcePathFile = new File(sourcePath);
        miscPathFile = new File(miscPath);
        moviePathFile = new File(moviePath);

        if (!Objects.isNull(imagePathFile) && !imagePathFile.exists()) {
            imagePathFile.mkdir();
        }
        if (!Objects.isNull(duplicateDirPathFile) && !duplicateDirPathFile.exists()) {
            duplicateDirPathFile.mkdir();
        }
        if (!Objects.isNull(sourcePathFile) && !sourcePathFile.exists()) {
            sourcePathFile.mkdir();
        }
        if (!Objects.isNull(miscPathFile) && !miscPathFile.exists()) {
            miscPathFile.mkdir();
        }
        if (!Objects.isNull(moviePathFile) && !moviePathFile.exists()) {
            moviePathFile.mkdir();
        }
    }


}
