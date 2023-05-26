package com.squarecross.photoalbum.domain;


import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="user", schema="photo_album", uniqueConstraints = {@UniqueConstraint(columnNames = "user_id")})

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "user_name", unique = false, nullable = false)
    private String userName;

    @Column(name = "user_email", unique = false, nullable = false)
    private String userEmail;

    @Column(name = "created_at", unique = false, nullable = true)
    @CreationTimestamp
    private Date createdAt;

    @Column(name = "login_at", unique = false, nullable = true)
    @CreationTimestamp
    private Date loginAt;


    @OneToMany(fetch = FetchType.LAZY,mappedBy = "album",cascade = CascadeType.ALL)
    private List<Photo> photos;
}
