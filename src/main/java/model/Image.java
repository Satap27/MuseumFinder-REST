package model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Image {

    @Id
    long image_id;

    String image_url;
    String thumb_url;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_museum_id")
    Museum museum;
}
