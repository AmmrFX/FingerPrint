package com.example.a9_11;

public class Visitor {
    private int Id;
    private String Name;
    private String Unit;
    private String Weapon;
    private String MilitaryId;
    private String Rank;
    private String Description;
    private String Image;
    private String NationalId;
    private String MilitaryStatus;
    private String Religion;
    private String address;
    private String date;
    private String CardNumber;

    public Visitor(String name, String nationalId, String rank, String weapon, String address, String religion,
                   String militaryStatus, String cardNumber, String unit, String description,
                   String militaryId) {
        this.CardNumber = cardNumber;
        this.Name = name;
        this.Unit = unit;
        this.Weapon = weapon;
        this.MilitaryId = militaryId;
        this.Rank = rank;
        this.Description = description;

        this.NationalId = nationalId;
        this.MilitaryStatus = militaryStatus;
        this.Religion = religion;
        this.address = address;

    }


    public Visitor(int id, String cardNumber, String name, String unit, String weapon, String militaryId, String rank,
                   String description, String image, String nationalId, String militaryStatus,
                   String religion, String address, String date) {
        this.CardNumber = cardNumber;
        this.Id = id;
        this.Name = name;
        this.Unit = unit;
        this.Weapon = weapon;
        this.MilitaryId = militaryId;
        this.Rank = rank;
        this.Description = description;
        this.Image = image;
        this.NationalId = nationalId;
        this.MilitaryStatus = militaryStatus;
        this.Religion = religion;
        this.address = address;
        this.date = date;
    }


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getRank() {
        return Rank;
    }

    public void setRank(String rank) {
        Rank = rank;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }


    @Override
    public String toString() {
        return "Visitor{" +
                "Name='" + Name + '\'' +
                ", Unit='" + Unit + '\'' +
                ", Weapon='" + Weapon + '\'' +
                ", MilitaryId=" + MilitaryId +
                '}';
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }

    public String getWeapon() {
        return Weapon;
    }

    public void setWeapon(String weapon) {
        Weapon = weapon;
    }

    public String getMilitaryId() {
        return MilitaryId;
    }

    public void setMilitaryId(String militaryId) {
        MilitaryId = String.valueOf(militaryId);
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getNationalId() {
        return NationalId;
    }

    public void setNationalId(String nationalId) {
        NationalId = nationalId;
    }

    public String getMilitaryStatus() {
        return MilitaryStatus;
    }

    public void setMilitaryStatus(String militaryStatus) {
        MilitaryStatus = militaryStatus;
    }

    public String getReligion() {
        return Religion;
    }

    public void setReligion(String religion) {
        Religion = religion;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCardNumber() {
        return CardNumber;
    }

    public void setCardNumber(String cardNumber) {
        CardNumber = cardNumber;
    }
}