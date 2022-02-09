package bg.fmi.mjt.splitwise.storage.dao;

public class IdentifiableStub implements Identifiable {

    private Integer i;
    private String data = "default";

    public IdentifiableStub(Integer i) {
        this.i = i;
    }

    public IdentifiableStub(Integer i, String data) {
        this.i = i;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IdentifiableStub that = (IdentifiableStub) o;

        return i.equals(that.i);
    }

    @Override
    public int hashCode() {
        return i.hashCode();
    }

    @Override
    public String toString() {
        return i.toString();
    }

    @Override
    public String id() {
        return i.toString();
    }

    public String getData() {
        return data;
    }

}
