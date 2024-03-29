package org.crp.flowable.shell.commands;

import org.apache.commons.io.IOUtils;
import org.crp.flowable.shell.configuration.FlowableShellProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@ShellCommandGroup("Utils")
@ShellComponent
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    @Autowired
    private FlowableShellProperties configuration;

    @ShellMethod("Configure flowable rest endpoint.")
    public String configure(@ShellOption(defaultValue = "") String login,
                            @ShellOption(defaultValue = "") String password,
                            @ShellOption(defaultValue = "") String restUrl,
                            @ShellOption(defaultValue = "") String idmUrl,
                            @ShellOption(defaultValue = "") String designerUrl) {
        if (!StringUtils.isEmpty(login)) configuration.setLogin(login);
        if (!StringUtils.isEmpty(password)) configuration.setPassword(password);
        if (!StringUtils.isEmpty(restUrl)) {
            if (!restUrl.endsWith("/")) {
                restUrl += "/";
            }
            configuration.setRestURL(restUrl);
        }
        if (!StringUtils.isEmpty(idmUrl)) {
            if (!idmUrl.endsWith("/")) {
                idmUrl += "/";
            }
            configuration.setIdmURL(idmUrl);
        } else {
            configuration.setIdmURL(configuration.getRestURL());
        }

        if (!StringUtils.isEmpty(designerUrl)) {
            if (!designerUrl.endsWith("/")) {
                designerUrl += "/";
            }
            configuration.setDesignerURL(designerUrl);
        }

        LOGGER.info("Current configuration designer Url {}, restUrl {}, login {}", designerUrl, restUrl, login);
        return configuration.getLogin() + "@" + configuration.getRestURL() + "@" + configuration.getIdmURL() + "@"+configuration.getDesignerURL();
    }

    @ShellMethod("Zip directory to file.")
    public static void zip(@ShellOption String sourceDirectory, @ShellOption String targetFileName) throws IOException {
        try (ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(targetFileName))) {
            compressDirectoryToZipfile(sourceDirectory, sourceDirectory, zipFile);
        }
    }

    @ShellMethod("Unzip file to directory.")
    public static void unzip(@ShellOption String zipFile, @ShellOption String targetDirectoryName) throws IOException, IllegalAccessException {
        File targetDirectory = new File(targetDirectoryName);
        if (!targetDirectory.exists()) {
            createDirs(targetDirectory);
        }
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File entryDestination = new File(targetDirectoryName, entry.getName());
                if (!entryDestination.getCanonicalPath().startsWith(targetDirectory.getCanonicalPath() + File.separator)) {
                    throw new IllegalAccessException("Entry is outside of the target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(entryDestination)) {
                        IOUtils.copy(zis, out);
                    }
                }
            }
        }
    }

    private static void createDirs(File entryDestination) {
        if (!entryDestination.mkdirs()) {
            throw new RuntimeException("Directory " + entryDestination.getPath() + " was not created");
        }
    }

    private static void compressDirectoryToZipfile(String rootDir, String sourceDir, ZipOutputStream out) throws IOException {
        for (File file : Objects.requireNonNull(new File(sourceDir).listFiles())) {
            if (file.isDirectory()) {
                compressDirectoryToZipfile(rootDir, sourceDir + File.separator + file.getName(), out);
            } else {
                ZipEntry entry = new ZipEntry(sourceDir.replace(rootDir, "") + file.getName());
                out.putNextEntry(entry);

                try (FileInputStream in = new FileInputStream(sourceDir + File.separator + file.getName())) {
                    IOUtils.copy(in, out);
                }
            }
        }
    }


}
