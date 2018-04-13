package com.allibilli.duplicate.photo.finder.group;

import com.allibilli.duplicate.photo.finder.structure.SaveFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class GroupAndMoveFiles extends SaveFile {

    int totalCount = 0;
    int totalDuplicateCount = 0;
    public static String dateFormat = "MMM-yyyy";
    public static String completeDateFormat = "dd-MM-yyyy hh:mm:ss";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
    private static SimpleDateFormat compDateFormat = new SimpleDateFormat(completeDateFormat);

    public void read(Optional<File> ef) throws Exception {
        File[] allFiles;
        File sourceFrom;

        if (ef.isPresent()) {
            sourceFrom = ef.get();
        } else {
            sourceFrom = groupFilesPath;
        }

        allFiles = sourceFrom.listFiles();

        if (Objects.isNull(allFiles)) {
            log.error("There are no files found to iterate");
            System.exit(0);
        }

        log.info("Looking inside: {}  - Number of files found: {}", sourceFrom, allFiles.length);
        groupFirst(allFiles);

        log.info("Total: {} Files Processed ", totalCount);
        log.info("Total: {} Duplicate Files Processed", totalDuplicateCount);
    }


    public void groupFirst(File[] allFiles) throws Exception {

        log.info("Grouping by date", allFiles.length);

        for (File file : allFiles) {
            totalCount++;

            if (file.isDirectory()) {
                if(file.listFiles().length == 0) {
                    log.info("Empty Directory: {}",file.getPath());
                    file.delete();
                    continue;
                } else {
                    read(Optional.of(file));
                }
            }

            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long millis = attr.creationTime().toMillis();
            long modifiedMills = attr.lastModifiedTime().toMillis();

            Calendar createdCal = Calendar.getInstance();
            createdCal.setTimeInMillis(millis);

            Calendar modifiedCal = Calendar.getInstance();
            modifiedCal.setTimeInMillis(modifiedMills);


            log.info("Created Time: {}", compDateFormat.format(createdCal.getTime()));
            log.info("Modified Time: {}", compDateFormat.format(modifiedCal.getTime()));

            Calendar calendar = Calendar.getInstance();
            if(modifiedMills < millis) {
                calendar.setTimeInMillis(modifiedMills);
            } else {
                calendar.setTimeInMillis(millis);
            }

            String date = simpleDateFormat.format(calendar.getTime());

            Path path = Paths.get(groupPath + File.separator + date);
            if (!Files.exists(path)) {
                log.info("Dir doesn't exists-->{}.. Creating One", date);
                path.toFile().mkdir();
            }
            if (!file.isDirectory()) {
                if(file.toPath().equals(path.resolve(file.getName()))){
                    log.info("Paths are same for: {} , {} - {}",  file.getName(), file.toPath(), path);
                } else {
                    Files.move(file.toPath(), path.resolve(file.getName()));
                    log.info("File Moved: {} - {} -> {} ", file.getName(), file.toPath(), path);
                }
            }
        }
    }
}
