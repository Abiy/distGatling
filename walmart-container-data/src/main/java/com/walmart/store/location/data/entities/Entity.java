package com.walmart.store.location.data.entities;

/**
 * Model
 */

public class Entity {

    public Entity(long id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Entity() {
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    private long id;
    private String name;
    private int age;

    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
