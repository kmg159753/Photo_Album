package com.squarecross.photoalbum.mapper;


import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;

import java.util.List;
import java.util.stream.Collectors;


public class PhotoMapper {
    public static PhotoDto convertToDto(Photo photo){
        PhotoDto photoDto = new PhotoDto();
        photoDto.setPhotoId(photo.getPhotoId());
        photoDto.setFileName(photo.getFileName());
        photoDto.setFileSize(photo.getFileSize());
        photoDto.setOriginalUrl(photo.getOriginalUrl());
        photoDto.setThumbUrl(photo.getThumbUrl());

        if(photo.getAlbum() != null) {
            photoDto.setAlbumId(photo.getAlbum().getAlbumId());
        }

        return photoDto;
    }

    public static List<PhotoDto> convertToDtoList(List<Photo> photos){
        return photos.stream().map(PhotoMapper::convertToDto).collect(Collectors.toList());
    }
}
