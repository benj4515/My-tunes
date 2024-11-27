package dk.easv.mytunes.BE;

public class MyTunes {

    private int id;
    private String title;
    private String artist;
    private String category;
    private String address;

    public MyTunes(int id, String title, String artist, String category, String address) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.address = address;
    }
}
