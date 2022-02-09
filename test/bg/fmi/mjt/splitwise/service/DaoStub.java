package bg.fmi.mjt.splitwise.service;

import bg.fmi.mjt.splitwise.storage.dao.Dao;
import bg.fmi.mjt.splitwise.storage.dao.Identifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DaoStub<T extends Identifiable> implements Dao<T> {

    private final Map<String, T> db;

    public DaoStub(Map<String, T> db) {
        this.db = new HashMap<>(db);
    }

    @Override
    public T find(String id) {
        return db.get(id);
    }

    @Override
    public Map<String, T> findAll() {
        return Collections.unmodifiableMap(db);
    }

    @Override
    public void insert(T object) {
        db.put(object.id(), object);
    }

    @Override
    public void update(T object) {
        db.put(object.id(), object);
    }

    @Override
    public void delete(String id) {
        db.remove(id);
    }
}
