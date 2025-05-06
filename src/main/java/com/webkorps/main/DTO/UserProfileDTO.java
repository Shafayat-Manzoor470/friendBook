package com.webkorps.main.DTO;

public class UserProfileDTO {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String profilePhoto;
    private String favBooks;
    private String favPlaces;
    private String favSongs;
    private int followerCount;
    private int followingCount;

    public UserProfileDTO() {}

    public UserProfileDTO(Long id, String fullName, String username, String email, String profilePhoto,
                          String favBooks, String favPlaces, String favSongs,
                          int followerCount, int followingCount) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.profilePhoto = profilePhoto;
        this.favBooks = favBooks;
        this.favPlaces = favPlaces;
        this.favSongs = favSongs;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfilePhoto() {
		return profilePhoto;
	}

	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}

	public String getFavBooks() {
		return favBooks;
	}

	public void setFavBooks(String favBooks) {
		this.favBooks = favBooks;
	}

	public String getFavPlaces() {
		return favPlaces;
	}

	public void setFavPlaces(String favPlaces) {
		this.favPlaces = favPlaces;
	}

	public String getFavSongs() {
		return favSongs;
	}

	public void setFavSongs(String favSongs) {
		this.favSongs = favSongs;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	public int getFollowingCount() {
		return followingCount;
	}

	public void setFollowingCount(int followingCount) {
		this.followingCount = followingCount;
	}

    
   
}
