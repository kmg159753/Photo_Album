package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PhotoService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    public PhotoDto getPhoto(Long photoId){
        Optional<Photo> photo = this.photoRepository.findById(photoId);

        if(photo.isPresent()){

            PhotoDto photoDto = PhotoMapper.convertToDto(photo.get());

            return  photoDto;
        }
        else{
            throw new EntityNotFoundException(String.format("사진을 찾을 수 없습니다."));
        }

    }

}
