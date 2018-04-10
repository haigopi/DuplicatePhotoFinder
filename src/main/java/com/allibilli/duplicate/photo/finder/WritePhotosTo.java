package com.allibilli.duplicate.photo.finder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public abstract class WritePhotosTo {
    File f;

    public WritePhotosTo(File dir) throws Exception {
        this.f = dir;
        //System.out.println(f.getCanonicalPath() + " --> exists --> " +f.exists());
        if (f != null && !f.exists()) {
            log.info("Dir Not found -- So creating now");
            f.mkdir();
        }

    }

    public void saveFile(File ef) throws IOException {
        if (ef.isDirectory()) {
            log.error("ERROR");
        }
        log.debug("Copying File : {}  to : {}", ef.getCanonicalPath(), f.getCanonicalPath());
        FileUtils.copyFileToDirectory(ef, f, true);
    }

}
