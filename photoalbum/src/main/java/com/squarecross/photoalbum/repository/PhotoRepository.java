package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo,Long> {
    int countByAlbum_AlbumId(Long AlbumId);
    List<Photo> findByAlbum_AlbumId(Long AlbumId);


    List<Photo> findTop4ByAlbum_AlbumIdOrderByUploadAtDesc(Long AlbumId);


    Optional<Photo> findByFileNameAndAlbum_AlbumId(String photoName, Long albumId);

}