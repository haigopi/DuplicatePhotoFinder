package com.allibilli.duplicate.photo.finder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
public class ReadPhotosFrom {
    private boolean toIncludeSubDirs;
    private File duplicateDir;
    private File regularDir;
    private File moviesDir;
    private int totalCount = 0;


    public void startReading(File readDir) throws Exception {

        File[] allFiles = readDir.listFiles();
        log.info("Looking inside: {}  - Number of files found: {}" + readDir, allFiles.length);
        WritePhotosTo duplicates = new WriteDuplicatePhotosTo(duplicateDir);
        WritePhotosTo regular = new WriteRegularPhotosTo(regularDir);
        WritePhotosTo movies = new WriteRegularMoviesTo(moviesDir);
        int count = 0;
        int duplicatesCount = 0;
        for (File ef : allFiles) {

            if (ef.isDirectory() && toIncludeSubDirs) {
                log.info("Directory found : {} ", ef.getAbsolutePath(), " Now processing...this ");
                startReading(ef);
            }

            if (ef.isDirectory()) {
                log.info("Directory found as file: Ignoring: {}", ef.getName());
                continue;
            }

            if (ef.getName().endsWith("db") || ef.getName().endsWith("jar") || ef.getName().contains(" - Copy") || ef.getName().contains(".ini")) {
                log.info("Deleting : {}", ef.getName());
                ef.delete();
                continue;
            }

            long size = FileUtils.sizeOf(ef);
            String name = ef.getName();
            long time = ef.lastModified();
            Path path;

            if (ef.getName().endsWith("MOV") || ef.getName().endsWith("mov"))
                path = Paths.get(moviesDir.getAbsolutePath() + "\\" + name);
            else
                path = Paths.get(regularDir.getAbsolutePath() + "\\" + name);

            //System.out.println( (++totalCount) +"] Processing from "+ ef.getAbsolutePath() +" To : " +path);
            if (Files.exists(path)
                    && Files.size(path) == size
                    && Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) == time) {
                log.info("{}/{} ] Duplicate: {} - {}", duplicatesCount, allFiles.length, ef.getCanonicalPath(), Files.getLastModifiedTime(path));

                //duplicates.saveFile(ef);

                ef.delete();
                ++duplicatesCount;
            } else if (Files.exists(path) && Files.size(path) != size &&  Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) != time) {
                log.info("{}/{} ] Duplicate Name wih different file: {}", duplicatesCount, allFiles.length, ef.getCanonicalPath());
                String ext = "JPG";
                String newName = String.format("%s.%s", RandomStringUtils.randomAlphanumeric(8), ext);
                File intermediate=new File(newName);
                ef.renameTo(intermediate);
                regular.saveFile(intermediate);
            } else if (Files.notExists(path) && !ef.getName().endsWith("MOV")) {
                log.info("{}/{} ] Not a duplicate : {} ", count, allFiles.length, ef.getCanonicalPath());
                regular.saveFile(ef);
            } else if (Files.notExists(path) && ef.getName().endsWith("MOV")) {
                log.info("{}/{} ] Movie Copying to : {}", count, allFiles.length, ef.getCanonicalPath());
                movies.saveFile(ef);
            }
            ++count;
        }
        if (allFiles.length != count) {
            log.error("Some files were missing");
        }
        log.info("Total: {} File Processed inside: {} ", count, readDir.getAbsolutePath());
        log.info("Total: {} Duplicates Found inside: {} ", duplicatesCount, readDir.getAbsolutePath());

    }
}
