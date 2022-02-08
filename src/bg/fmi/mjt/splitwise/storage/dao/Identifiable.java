package bg.fmi.mjt.splitwise.storage.dao;

/**
 *
 * @param <I> the type of the id
 */
public interface Identifiable {


    /**
     * @return A reproducible unique String for this object.
     */
    String id();

}
