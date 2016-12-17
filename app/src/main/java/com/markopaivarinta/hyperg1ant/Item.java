package com.markopaivarinta.hyperg1ant;

import java.util.Comparator;
import java.util.Date;


public class Item{

    protected String itemId;
    protected String title;
    protected int price;
    protected String image;
    protected String description;
    protected Date itemListedDate;
    protected String latitude;
    protected String longitude;
    protected String contactInfo;
    protected Double distanceFromUser;


    protected Item() {
        this.contactInfo = contactInfo;
        this.price = price;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.latitude = longitude;
    }

    public Date getItemListedDate() {
        return itemListedDate;
    }

    public void setItemListedDate(Date itemListedDate) {
        this.itemListedDate = itemListedDate;
    }

    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

}
class ItemComparator implements Comparator<Item> {
    @Override
    public int compare(Item item1, Item item2) {
        Double itemDistance1 = item1.getDistanceFromUser();
        Double itemDistance2 = item2.getDistanceFromUser();

        if (itemDistance1 > itemDistance2) {
            return 1;
        } else if (itemDistance1 < itemDistance2) {
            return -1;
        } else {
            return 0;
        }
    }
}