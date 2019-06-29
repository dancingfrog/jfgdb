package org.fgdbapi.thindriver;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * init and uncompress shared libraries
 *
 * @author pfreydiere
 */
public class SharedLibrariesInitializer {

  static boolean initialized = false;

  private static void uncompressFiles(String resourcePath, String[] listFiles, File outputDir) {
    assert resourcePath != null;
    assert !resourcePath.endsWith("/");

    if (listFiles == null || listFiles.length == 0) return;

    for (String filename : listFiles) {

      InputStream is = SharedLibrariesInitializer.class.getResourceAsStream(resourcePath + "");
      if (is == null) {
        System.out.println("WARN resource " + filename + " not found");
        continue;
      } else {
        System.out.println("write " + filename);
        try {
          File outputFile = new File(outputDir, filename);

          if (outputFile.exists()) continue;

          FileOutputStream fos = new FileOutputStream(outputFile);
          try {
            byte[] buffer = new byte[1024];
            int cpt = 0;
            while ((cpt = is.read(buffer)) != -1) {
              fos.write(buffer, 0, cpt);
            }

          } finally {
            fos.close();
            is.close();
          }
        } catch (IOException ex) {
          System.err.println("could not write file " + filename);
          ex.printStackTrace(System.err);
        }
      }
    }
  }

  private static void uncompressFile(String resourcePath, String filename, File outputDir)
      throws Exception {
//    assert resourcePath != null;
//    assert !resourcePath.endsWith("/");

    if (filename == null) return;

    String basePath = resourcePath.substring(0, resourcePath.lastIndexOf("/"));
//    assert !basePath.endsWith("/");

    String libraryResourcePath = basePath + "/" + filename;
    //InputStream is = SharedLibrariesInitializer.class.getResourceAsStream(libraryResourcePath);
    InputStream is = new FileInputStream(libraryResourcePath);
    if (is == null) throw new Exception("resource \"" + libraryResourcePath + "\" not found");

    try {
      File outputFile = new File(outputDir, filename);

      if (outputFile.exists()) return;

      System.out.println("write " + filename);
      FileOutputStream fos = new FileOutputStream(outputFile);
      try {
        byte[] buffer = new byte[1024];
        int cpt = 0;
        while ((cpt = is.read(buffer)) != -1) {
          fos.write(buffer, 0, cpt);
        }

      } finally {
        fos.close();
        is.close();
      }
    } catch (IOException ex) {
      System.err.println("could not write file " + filename);
      ex.printStackTrace(System.err);
      throw ex;
    }
  }

  public static void initLibraries() {

    if (initialized) return;

    synchronized (SharedLibrariesInitializer.class) {
      if (initialized) return;

      System.out.println("initialize the shared libraries");

      File tmpFile = new File(System.getProperty("java.io.tmpdir"));
      if (!tmpFile.exists() && !tmpFile.isDirectory())
        throw new RuntimeException("java.io.tmpdir does not exists");

      String osName = System.getProperty("os.name");
      System.out.println("OS :" + osName);

      String osArch = Platform.RESOURCE_PREFIX;
      System.out.println("Architecture :" + osArch);

      InputStream fgdbversionProperties =
          SharedLibrariesInitializer.class.getResourceAsStream("/fgdbversion.properties");
      if (fgdbversionProperties == null)
        throw new RuntimeException(
            "fgdbversion.properties file not found in the build,  incorrect compile");

      String version = null;
      String wrapperLibraryPath = null;
      Properties properties = new Properties();
      try {
        // load the fgdb properties
        // and version
        properties.load(fgdbversionProperties);
        version = properties.getProperty("version");

      } catch (Exception ex) {
        throw new RuntimeException("failed to load fgdbversion.properties :" + ex.getMessage(), ex);
      }

      if (version == null) {
        throw new RuntimeException(
            "fgdbversion.properties does not contain version key, incorrect build");
      }

      // in jar, the dll are in this package
      String resourcesFilesPath =       (System.getenv("ESRI_FILE_GDB_HOME") != null) ?
          //new File(System.getenv("ESRI_FILE_GDB_HOME")).toPath().toAbsolutePath().toString() :
          new File(System.getenv("ESRI_FILE_GDB_HOME") + "/lib").toPath().toString() :
          new File("/sharedlibraries/" + version + "/" + osArch + "/files").toPath().toString();

      try {
        File[] files = Paths.get(resourcesFilesPath).toFile().listFiles();
        File outputDir = new File(tmpFile, "fgdbsharedlibs/" + version);
        outputDir.mkdirs();

        for (File file : files) {
          String filename = file.getName();

          filename = sanitizeFileName(filename);
          if (filename == null || filename.isEmpty()) continue;

          filename = filename.trim();

          System.out.println("checking " + filename);

          String libPath =
              System.getProperty("java.library.path")
                  + System.getProperty("path.separator")
                  + outputDir.getAbsolutePath();

          System.setProperty("jna.library.path", libPath);

          uncompressFile(resourcesFilesPath + "/", filename, outputDir);

          if (filename.toLowerCase().contains("wrapper")) {
            if (wrapperLibraryPath == null)
              wrapperLibraryPath = new File(outputDir, filename).getAbsolutePath();
          }

          if (filename.endsWith(".dll") || filename.endsWith(".so") || filename.endsWith(".dylib")) { // sanity

            System.out.println("loading sharedlibrary :" + wrapperLibraryPath);

            if (Platform.isWindows()) {
              // load the library
              NativeLibrary.getInstance(wrapperLibraryPath);

            } else {
              // need to load wrapper for libc

//                if (libc == null) {
//                    System.out
//                            .println("setting LD_LIBRARY_PATH to "
//                                    + outputDir
//                                            .getAbsolutePath());
//                    libc = (LibC) Native.loadLibrary("c",
//                            LibC.class);
//
//                    int status = libc.setenv("LD_LIBRARY_PATH",
//                            outputDir.getAbsolutePath(), 1);
//
//                    System.out.println("setenv returned "
//                            + status);
//
//                }
//
//                if (libdl == null) {
//                    libdl = (DL) Native.loadLibrary("c",
//                            DL.class);
//                }
//
//                int returned = libdl.dlopen(
//                        outputDir.getAbsolutePath(),
//                        0x0100 | 0x00002);
//                System.out.println("dlopen returned " + returned);
//              System.loadLibrary("FGDBJNIWrapper");
            }

//             if (filename.toLowerCase().contains("wrapper")) {
//              System.loadLibrary("FGDBJNIWrapper");
//             }

            System.out.println("successfully loaded");
          }
        }

        if (wrapperLibraryPath == null) {
          throw new Exception("wrapper library has not been extracted");
        }

        try {
          System.load(wrapperLibraryPath);
        } catch (Exception ex) {
          System.out.println("LIBRARY " + wrapperLibraryPath + " cannot be loaded");
          ex.printStackTrace();

          if (Platform.isLinux()) {
            System.out.println(
                "please launch \"export LD_LIBRARY_PATH="
                    + new File(wrapperLibraryPath).getParentFile().getAbsolutePath()
                    + "\" before running the program");
          }
        }

      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage(), ex);
      }
    }
  }

  private static String sanitizeFileName(String filename) {

    if (filename == null) return null;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < filename.length(); i++) {
      char c = filename.charAt(i);
      switch (c) {
        case '\n':
        case '\t':
        case '\r':
          continue;
        default:
          break;
      }
      sb.append(c);
    }

    return sb.toString();
  }
}
