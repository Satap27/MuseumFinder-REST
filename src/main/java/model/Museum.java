package model;

import io.ebean.annotation.DocStore;
import io.ebean.annotation.NotNull;

import javax.persistence.*;
import java.util.List;

@DocStore
@Entity
public class Museum {

    @NotNull
    private final String name;
    //optional parameters
    private final String wikiLink;
    private final String website;
    private final String address;
    private final String location;
    private final Double lat;
    private final Double lng;
    @Column(columnDefinition = "TEXT")
    private final String description;
    @ManyToMany
    @JoinTable(
            name = "museum_category",
            joinColumns = @JoinColumn(name = "fk_museum_id", referencedColumnName = "museum_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_category_id", referencedColumnName = "category_id"))
    private final List<Category> categories;
    @OneToMany(mappedBy = "museum")
    private final List<Image> images;
    @Id
    private long museumId;

    public Museum(Builder builder) {
        name = builder.name;
        wikiLink = builder.wikiLink;
        website = builder.website;
        location = builder.location;
        lat = builder.lat;
        lng = builder.lng;
        description = builder.description;
        address = builder.address;
        categories = builder.categories;
        images = builder.images;
    }

    public long getMuseumId() {
        return museumId;
    }

    public String getName() {
        return name;
    }

    public String getWikiLink() {
        return wikiLink;
    }

    public String getWebsite() {
        return website;
    }

    public String getLocation() {
        return location;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<Image> getImages() {
        return images;
    }

    public static class Builder {
        //required parametrs
        private final String name;
        //optional parameters
        private String wikiLink;
        private String website;
        private String location;
        private double lat;
        private double lng;
        private String description;
        private String address;
        private List<Category> categories;
        private List<Image> images;

        public Builder(String name) {
            this.name = name;
        }

        public Builder wikiLink(String wikiLink) {
            this.wikiLink = wikiLink;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder categories(List<Category> categories) {
            this.categories = categories;
            return this;
        }

        public Builder images(List<Image> images) {
            this.images = images;
            return this;
        }

        public Builder lat(double lat) {
            this.lat = lat;
            return this;
        }

        public Builder lng(double lng) {
            this.lng = lng;
            return this;
        }

        public Museum build() {
            return new Museum(this);
        }
    }
}

