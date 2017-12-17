//package com.dk.models;
//
//import java.util.ArrayList;
//
//import io.objectbox.annotation.Entity;
//import io.objectbox.annotation.Id;
//import io.objectbox.relation.ToMany;
//import io.objectbox.relation.ToOne;
//
///**
// * Created by dk on 10/09/17.
// */
//
//@Entity
//public class Bucket {
//
//    public ToMany<Tag> tags;
//
//    public ToOne<User> user;
//
//    @Id
//    private long id;
//
//    private String name;
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public ToMany<Tag> getTags() {
//        return tags;
//    }
//
//    public void setTags(ToMany<Tag> tags) {
//        this.tags = tags;
//    }
//
//    public ToOne<User> getUser() {
//        return user;
//    }
//
//    public void setUser(ToOne<User> user) {
//        this.user = user;
//    }
//
//    public ArrayList<String> getTagLableList() {
//        ArrayList<String> tags_string = new ArrayList<>();
//        for (Tag tag : tags) {
//            tags_string.add(tag.getName());
//        }
//        return tags_string;
//    }
//
//}
