package bg.fmi.mjt.splitwise.storage.dao;

import java.util.Map;

/**
 *
 * @param <T> should be effectively immutable type
 */
public interface Dao<T extends Identifiable> {

    T find(String id);

    Map<String, T> findAll();

    void insert(T object);

    void update(T object);

    void delete(String id);
}
