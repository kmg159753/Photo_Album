package com.squarecross.photoalbum.domain;


import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.net.URL;
import java.util.Date;


@Entity
@Table(name = "photo", schema = "photo_album", uniqueConstraints = {@UniqueConstraint(columnNames = "photo_id")})
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id", unique = true, nullable = false)
    private Long photoId;

    //thumb_url, original_url, file_size, uploaded_at
    @Column(name="file_name",unique = false, nullable = true )
    private String fileName;

    @Column(name = "file_size", unique = false, nullable = true)
    private int fileSize;

    @Column(name = "thumb_url", unique = false, nullable = true)
    private String thumbUrl;

    @Column(name = "original_url", unique = false, nullable = true)
    private String originalUrl;



    @Column(name = "uploaded_at", unique = false, nullable = true)
    @CreationTimestamp
    private Date uploadAt;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name="album_id")
    private Album album;


    public Photo(){};
    public Long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(Long photoId) {
        this.photoId = photoId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Date getUploadAt() {
        return uploadAt;
    }

    public void setUploadAt(Date uploadAt) {
        this.uploadAt = uploadAt;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}