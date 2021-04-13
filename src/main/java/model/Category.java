package model;

import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Category {

    @Id
    long category_id;

    @NotNull
    String name;

}
