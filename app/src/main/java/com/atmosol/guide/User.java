package com.atmosol.guide;

public class User {
    String id;
    String name;
    String room;

    public User(String id, String name, String room) {
        this.id = id;
        this.name = name;
        this.room = room;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", room='" + room + '\'' +
                '}';
    }
}
