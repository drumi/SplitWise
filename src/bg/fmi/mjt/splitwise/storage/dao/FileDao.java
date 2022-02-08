package bg.fmi.mjt.splitwise.storage.dao;

import bg.fmi.mjt.splitwise.storage.dao.exceptions.DaoException;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileDao<T extends Identifiable> implements Dao<T> {

    private final Gson gson;
    private final Path pathToDB;
    private final Class<T> clazz;

    private final Map<String, T> entries = new HashMap<>();

    public FileDao(Path pathToDB, Class<T> clazz) {
        this.pathToDB = pathToDB;
        this.clazz = clazz;

        gson = new Gson();

        init();
    }

    public FileDao(Path pathToDB, Gson gson, Class<T> clazz) {
        this.pathToDB = pathToDB;
        this.clazz = clazz;
        this.gson = gson;

        init();
    }

    private void init() {
        try {
            createDirectoryIfDoesNotExist();

            var files = Files.newDirectoryStream(pathToDB);

            for (Path file : files) {
                loadFileIntoMemory(file);
            }
        } catch (IOException e) {
            throw new DaoException("Initializing files failed", e);
        }
    }

    private void createDirectoryIfDoesNotExist() throws IOException {
        Files.createDirectories(pathToDB);
    }

    private void loadFileIntoMemory(Path p) throws IOException {
        String content = Files.readString(p);

        T object = gson.fromJson(content, clazz);

        entries.put(object.id(), object);
    }

    @Override
    public T find(String id) {
        return entries.get(id);
    }

    @Override
    public Map<String, T> findAll() {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public void insert(T object) {
        Objects.requireNonNull(object, "Inserted object cannot be null");

        try {
            Path pathToFile = pathToDB.resolve(object.id());
            Files.writeString(pathToFile, gson.toJson(object), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new DaoException("Could not insert object due to file exception", e);
        }

        entries.put(object.id(), object);
    }

    @Override
    public void update(T object) {
        Objects.requireNonNull(object, "Updated object cannot be null");

        try {
            Path pathToFile = pathToDB.resolve(object.id());
            Files.writeString(
                pathToFile,
                gson.toJson(object),
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new DaoException("Could not update object", e);
        }

        entries.put(object.id(), object);
    }

    @Override
    public void delete(String id) {
        Objects.requireNonNull(id, "id cannot be null");

        try {
            Path pathToFile = pathToDB.resolve(id);
            Files.delete(pathToFile);
        } catch (IOException e) {
            throw new DaoException("Could not delete object", e);
        }

        entries.remove(id);
    }
}
