package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class PhotoServiceTest {

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    PhotoRepository photoRepository;
    @Autowired
    AlbumService albumService;

    @Autowired
    PhotoService photoService;

    @Test
    void testgetPhoto() {
        //given
        Photo photo = new Photo();
        photo.setFileName("테스트");
        Photo savedPhoto = photoRepository.save(photo);


        //when
        PhotoDto resDto = photoService.getPhoto(savedPhoto.getPhotoId());


        //then
        Assertions.assertThat(resDto.getFileName()).isEqualTo("테스트");


    }

    @Test
    void getPhotoList() {
    }

    @Test
    void savePhoto() {
    }

    @Test
    void getImageFile() {
    }

    /*@Test
    void TestchangeAlbum_beforeUpdateDir() {
        Album album1 = new Album();
        album1.setAlbumName("테스트1");
        Album FromAlbum = albumRepository.save(album1);

        Album album2 = new Album();
        album2.setAlbumName("테스트2");
        Album ToAlbum = albumRepository.save(album2);

        Photo photo1 = new Photo();
        photo1.setFileName("사진1");
        photo1.setAlbum(FromAlbum);
        photoRepository.save(photo1);

        PhotoDto res= photoService.changeAlbum(photo1.getPhotoId(), FromAlbum.getAlbumId(), ToAlbum.getAlbumId());


        assertEquals(album2.getAlbumId(), res.getAlbumId());
    }
*/
    /*@Test
    void TestchangeAlbum_dir() {

        Long FromId = 1L;

        Long ToId = 2L;

        Long photoId = 1L;

        PhotoDto res= photoService.changeAlbum(photoId,FromId,ToId);


        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+albumId)));

    }*/

}