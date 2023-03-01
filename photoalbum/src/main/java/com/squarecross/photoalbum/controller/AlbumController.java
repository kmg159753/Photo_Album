package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/albums")
public class AlbumController {
    @Autowired
    AlbumService albumService;

    @RequestMapping(value = "/{albumId}", method = RequestMethod.GET)// Url 경로와 메서드 정의

    public ResponseEntity<AlbumDto> getAlbum(@PathVariable("albumId") final long albumId){ // 메서드의 입출력 정의

        AlbumDto album = albumService.getAlbum(albumId);
        return new ResponseEntity<>(album, HttpStatus.OK);

    }
}
