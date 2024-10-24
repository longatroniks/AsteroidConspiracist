package dte.masteriot.mdp.asteroidconspiracist;

public class Item {
    // This class contains the actual data of each item of the dataset

    private String title;
    private String subtitle;
    private Long key; // In this app we use keys of type Long

    Item(String title, String subtitle, Long key) {
        this.title = title;
        this.subtitle = subtitle;
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Long getKey() {
        return key;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

}