package com.webkorps.main.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String username;

    private String profilePhoto;
    private String favSongs;
    private String favBooks;
    private String favPlaces;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_following",
        joinColumns = @JoinColumn(name = "follower_id"),
        inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following", fetch = FetchType.EAGER)
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Post> posts = new HashSet<>();

    // Getters & Setters

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getProfilePhoto() { return profilePhoto; }

    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getFavSongs() { return favSongs; }

    public void setFavSongs(String favSongs) { this.favSongs = favSongs; }

    public String getFavBooks() { return favBooks; }

    public void setFavBooks(String favBooks) { this.favBooks = favBooks; }

    public String getFavPlaces() { return favPlaces; }

    public void setFavPlaces(String favPlaces) { this.favPlaces = favPlaces; }

    public Set<User> getFollowing() { return following; }

    public void setFollowing(Set<User> following) { this.following = following; }

    public Set<User> getFollowers() { return followers; }

    public void setFollowers(Set<User> followers) { this.followers = followers; }

    public Set<Post> getPosts() { return posts; }

    public void setPosts(Set<Post> posts) { this.posts = posts; }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               '}';
    }
}
