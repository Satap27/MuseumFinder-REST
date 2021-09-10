package model;

import io.ebean.annotation.DocStore;
import io.ebean.annotation.Identity;
import io.ebean.annotation.NotNull;

import javax.persistence.*;
import java.util.Set;

@DocStore
@Entity
public class Museum {

    @NotNull
    private String name;
    //optional parameters
    private String wikiLink;
    private String website;
    private String address;
    private String location;
    private Double lat;
    private Double lng;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToMany
    @JoinTable(
            name = "museum_category",
            joinColumns = @JoinColumn(name = "fk_museum_id", referencedColumnName = "museum_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_category_id", referencedColumnName = "category_id"))
    private final Set<Category> categories;
    @OneToMany(mappedBy = "museum")
    private final Set<Image> images;
    @Id @Identity(start=10000)
    private long museumId;

    @ManyToMany(mappedBy = "ownedMuseums")
    private Set<User> owners;

    public Museum(Builder builder) {
        museumId = builder.museumId;
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

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public Set<Image> getImages() {
        return images;
    }

    public Set<User> getOwners() {
        return owners;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWikiLink(String wikiLink) {
        this.wikiLink = wikiLink;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwners(Set<User> owners) {
        this.owners = owners;
    }

    public static class Builder {
        //required parametrs
        private final long museumId;
        private final String name;
        //optional parameters
        private String wikiLink;
        private String website;
        private String location;
        private double lat;
        private double lng;
        private String description;
        private String address;
        private Set<Category> categories;
        private Set<Image> images;

        public Builder(long museumId, String name) {
            this.museumId = museumId;
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

        public Builder categories(Set<Category> categories) {
            this.categories = categories;
            return this;
        }

        public Builder images(Set<Image> images) {
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

    public void updateWith(Museum newMuseum) {
        this.name = newMuseum.getName();
        this.wikiLink = newMuseum.getWikiLink();
        this.website = newMuseum.getWebsite();
        this.address = newMuseum.getAddress();
        this.location = newMuseum.getLocation();
        this.lat = newMuseum.getLat();
        this.lng = newMuseum.getLng();
        this.description = newMuseum.getDescription();
    }
}

