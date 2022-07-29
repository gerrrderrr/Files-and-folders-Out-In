import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {
    public static StringBuilder newLog = new StringBuilder();

    public static void main(String[] args) {

        create("D://Games//", Arrays.asList("src", "res", "savegames", "temp"), false);
        create("D://Games//src//", Arrays.asList("main", "test"), false);
        create("D://Games//res//", Arrays.asList("drawables", "vectors", "icons"), false);
        create("D://Games//src//main//", Arrays.asList("Main.java", "Utils.java"), true);
        createFile("D://Games//temp//", "temp.txt");


        GameProgress[] gamesToSave = {
                new GameProgress(100, 3, 2, 100.3),
                new GameProgress(80, 8, 5, 945.5),
                new GameProgress(86, 15, 9, 2345.9),
        };

        List<String> savedGames = new ArrayList<>();
        int count = 1;
        for (GameProgress x : gamesToSave) {
            String direction = "D://Games//savegames//save" + count + ".dat";
            saveGame(x, direction);
            savedGames.add(direction);
            count++;
        }

        zipFiles("D://Games//savegames//save.zip", savedGames);
        delOnlyFiles("D://Games//savegames//");
        openZip("D://Games//savegames//save.zip", "D://Games//savegames//");
        System.out.println(openProgress("D://Games//savegames//save1.dat"));
        saveLogTo("D://Games//temp//temp.txt");
    }

    public static void saveGame(GameProgress game, String dir) {
        try (FileOutputStream fos = new FileOutputStream(dir);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(game);
            String[] name = dir.split("//");
            String success = "Game " + name[name.length - 1] + " saved";
            log(success);
            System.out.println(success);
        } catch (Exception e) {
            String failed = e.getMessage();
            System.out.println(failed);
            log(failed);
        }
    }

    public static void zipFiles(String dirToArchive, List<String> dirToTheFiles) {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dirToArchive));
            String[] nameZip = dirToArchive.split("//");
            log("Zip file was created: " + nameZip[nameZip.length - 1]);
            for (String x : dirToTheFiles) {
                String[] name = x.split("//");
                out.putNextEntry(new ZipEntry("archived_" + name[name.length - 1]));
                try (FileInputStream fis = new FileInputStream(x)) {
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    out.write(buffer);
                    String success = "File " + "archived_" + name[name.length - 1] + " was added to the Zip file";
                    log(success);
                    System.out.println(success);
                }
            }
            out.close();
        } catch (Exception e) {
            String failed = e.getMessage();
            System.out.println(failed);
            log(failed);
        }
    }

    public static void openZip(String dirToArchive, String dirWhereUnzip) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(dirToArchive))) {
            ZipEntry entry;
            String[] name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName().split("_");
                FileOutputStream fout = new FileOutputStream(dirWhereUnzip + name[name.length - 1]);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                String success = "Unzipped " + entry.getName() + " file";
                log(success);
                System.out.println(success);
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        } catch (Exception e) {
            String failed = e.getMessage();
            System.out.println(failed);
            log(failed);
        }
    }

    public static GameProgress openProgress(String saveGameFileDir) {
        GameProgress gameProgress = null;
        try (FileInputStream fis = new FileInputStream(saveGameFileDir);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            gameProgress = (GameProgress) ois.readObject();
            String success = "File deserialization completed";
            log(success);
            System.out.println(success);
        } catch (Exception e) {
            String failed = e.getMessage();
            System.out.println(failed);
            log(failed);
        }
        return gameProgress;
    }

    public static void delOnlyFiles(String dirToFiles) {
        File toDel = new File(dirToFiles);
        if (toDel.isDirectory()) {
            try {
                File[] f = toDel.listFiles();
                if (f != null) {
                    for (File item : f) {
                        String[] n = item.getName().split("\\.");
                        if (!item.isDirectory() && !n[n.length - 1].equals("zip")) {
                            if (item.delete()) {
                                String success = "File deleted: " + item.getName();
                                System.out.println(success);
                                log(success);
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                String failed = e.getMessage();
                System.out.println(failed);
                log(failed);
            }
        }
    }

    public static void create(String dir, List<String> toCreate, boolean creatingFile) {
        if (creatingFile) {
            for (String file : toCreate) {
                createFile(dir, file);
            }
        } else {
            for (String folder : toCreate) {
                createFolder(dir, folder);
            }
        }
    }

    public static void createFolder(String dir, String folderName) {
        File direction = new File(dir + folderName);
        if (direction.mkdir()) {
            String success = "New folder created: " + folderName;
            System.out.println(success);
            log(success);
        } else {
            String failed = "Failed to create folder: " + folderName;
            System.out.println(failed);
            log(failed);
        }
    }

    public static void createFile(String dir, String fileName) {
        File file = new File(dir + fileName);
        try {
            if (file.createNewFile()) {
                String success = "New file created: " + fileName;
                System.out.println(success);
                log(success);
            }
        } catch (IOException e) {
            String failed = e.getMessage();
            System.out.println(failed);
            log(failed);
        }
    }

    public static void log(String info) {
        newLog.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy")));
        newLog.append(" ");
        newLog.append(info);
        newLog.append("\n");
    }

    public static void saveLogTo(String fileDir) {
        try (FileOutputStream fos = new FileOutputStream(fileDir)) {
            byte[] bytes = newLog.toString().getBytes();
            fos.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
