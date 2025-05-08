package com.webkorps.main.DTO;

public class UpdateInfoRequest {
	private String favSongs;
	private String favBooks;
	private String favPlaces;
	public String getFavSongs() {
		return favSongs;
	}
	public void setFavSongs(String favSongs) {
		this.favSongs = favSongs;
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
    
    
}