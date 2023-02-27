package com.squarecross.photoalbum.repository;

import com.squarecross.photoalbum.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static ch.qos.logback.core.joran.spi.ConsoleTarget.findByName;

@Repository
public interface AlbumRepository extends JpaRepository<Album,Long> {
    Optional<Album> findByAlbumName(String name);

}
