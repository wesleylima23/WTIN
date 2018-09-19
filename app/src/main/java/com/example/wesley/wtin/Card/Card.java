package com.example.wesley.wtin.Card;

public class Card {
    private String userID;
    private String name;
    private String profileImageUrl;
    private String orientation;
    private String interesse;

    public Card(String userID, String name, String profileImageUrl,String orientation, String interesse){
        this.userID = userID;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.orientation = orientation;
        this.interesse = interesse;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getInteresse() {
        return interesse;
    }

    public void setInteresse(String interesse) {
        this.interesse = interesse;
    }
}
