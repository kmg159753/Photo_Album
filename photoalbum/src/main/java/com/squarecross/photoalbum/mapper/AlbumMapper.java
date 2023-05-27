package com.squarecross.photoalbum.mapper;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AlbumMapper {
    public static AlbumDto convertToDto(Album album){

        PhotoMapper photoMapper = new PhotoMapper();

        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumId(album.getAlbumId());
        albumDto.setAlbumName(album.getAlbumName());
        albumDto.setCreatedAt(album.getCreatedAt());


        List<PhotoDto> photoDTOs = new ArrayList<>();
        if (album.getPhotos() != null) {
            for (Photo photo : album.getPhotos()) {
                photoDTOs.add(photoMapper.convertToDto(photo));
            }
        }
        albumDto.setPhotos(photoDTOs);
        return albumDto;
    }

    public static Album convertToModel(AlbumDto albumDto) {
        Album album = new Album();
        album.setAlbumId(albumDto.getAlbumId());
        album.setAlbumName(albumDto.getAlbumName());
        album.setCreatedAt(albumDto.getCreatedAt());

        album.setUser(albumDto.getUserId());
        return album;
    }

    public static List<AlbumDto> convertToDtoList(List<Album> albums){
     return albums.stream().map(AlbumMapper::convertToDto).collect(Collectors.toList());
    }
}
