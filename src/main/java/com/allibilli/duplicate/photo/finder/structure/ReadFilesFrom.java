package com.allibilli.duplicate.photo.finder.structure;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Data
@Slf4j
public class ReadFilesFrom extends SaveFile {

    int totalCount = 0;
    int totalDuplicateCount = 0;

    public void read(Optional<File> ef) throws Exception {
        File[] allFiles;
        File sourceFrom;

        if (ef.isPresent()) {
            sourceFrom = ef.get();
        } else {
            sourceFrom = sourcePathFile;
        }

        allFiles = sourceFrom.listFiles();

        if (Objects.isNull(allFiles)) {
            log.error("There are no files found to iterate");
            System.exit(0);
        }

        log.info("Looking inside: {}  - Number of files found: {}", sourceFrom, allFiles.length);

        for (File file : allFiles) {
            totalCount++;
            processSubDirs(file);
            boolean isDuplicate = processDuplicate(file);
            if (!isDuplicate) {
                boolean fileProcessed = processDifferentFileWithSameName(file);
                if (!fileProcessed) {
                    processNotDuplicate(file);
                }
            }
        }

        log.info("Total: {} Files Processed ", totalCount);
        log.info("Total: {} Duplicate Files Processed", totalDuplicateCount);
    }

    private void processNotDuplicate(File ef) throws Exception {

        String fileName = ef.getName();
        Path path = getPath(fileName);
        log.info("Processing: {} Not a duplicate and count: {} ", path, totalCount);
        saveFile(ef, path);
//        FileUtils.copyFileToDirectory(ef, path.toFile(), true);
    }

    private boolean processDifferentFileWithSameName(File ef) throws Exception {
        long fileSize = FileUtils.sizeOf(ef);
        String fileName = ef.getName();
        long fileCreatedTime = ef.lastModified();
        Path path = getPath(fileName);

        if (Files.exists(path) && Files.size(path) != fileSize && Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) != fileCreatedTime) {
            String generatedName = String.format("%s_%s_%s", "renamed", ef.lastModified(), fileName);
            path = getPath(generatedName);

            if (Files.exists(path)
                    && Files.size(path) == fileSize
                    && Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) == fileCreatedTime) {
                path = getDuplicateFilePath(fileName);
                saveFile(ef, path);
                totalDuplicateCount++;
            } else {
                saveFile(ef, path);
            }
            return true;
        }
        return false;
    }

    private boolean processDuplicate(File ef) throws Exception {

        if(!ef.exists()){
            return false;
        }
        long fileSize = FileUtils.sizeOf(ef);
        String fileName = ef.getName();
        long fileCreatedTime = ef.lastModified();

        Path path = getPath(fileName);
        if (Files.exists(path)
                && Files.size(path) == fileSize
                && Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) == fileCreatedTime) {
            totalDuplicateCount++;
            log.info("Duplicate Count: {} - {} - {}", totalDuplicateCount, ef.getCanonicalPath(), Files.getLastModifiedTime(path));
            //FileUtils.copyFileToDirectory(ef, path.toFile(), true);
            path = getDuplicateFilePath(fileName);
            saveFile(ef, path);

            return true;
        }
        return false;
    }

    private void saveFile(File ef, Path path) throws Exception {
        if (ef.isDirectory()) {
            log.info("Not Moving {} -> {}", ef.getAbsolutePath(), path);
            return;
        }
        log.info("Moving {} -> {}", ef.getAbsolutePath(), path);
        FileUtils.copyFile(ef, path.toFile(), true);
    }

    private void processSubDirs(File ef) throws Exception {

        if (ef.getName().endsWith("db") || ef.getName().endsWith("jar") || ef.getName().contains(" - Copy") || ef.getName().contains(".ini") || ef.getName().equalsIgnoreCase(".@__thumb")) {
            log.info("Deleting : {}", ef.getName());
            ef.delete();
            return;
        }

        if (ef.isDirectory()) {
            if (isToIncludeSubDirs()) {
                log.info("Directory Processing: {} ", ef.getAbsolutePath());
                read(Optional.of(ef));
            } else {
                log.info("Directory found and Ignoring: {}", ef.getName());
            }
        }
    }

}
