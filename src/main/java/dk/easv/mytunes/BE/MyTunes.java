package dk.easv.mytunes.BE;

public class MyTunes {

    private int id;
    private String title;
    private String artist;
    private String category;
    private String address;
    private int time;

    public MyTunes(int id, String title, String artist, String category, String address, int time) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.category = category;
        this.address = address;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
