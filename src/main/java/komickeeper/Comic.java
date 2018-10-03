package komickeeper;

public class Comic {
    public String path;
    public String name;




    String getName() {
        return name;
    }

    String getPath() {
        return path;
    }

    Comic(String name, String path) {
        this.path = path;
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
