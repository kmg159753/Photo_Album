package com.squarecross.photoalbum.service;


import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PhotoService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    //사용하기 편하게 original 과 thumb 디렉토리 경로를 클래스 상단에 미리 세팅
    private final String original_path = Constants.PATH_PREFIX+"/photos/original";
    private final String thumb_path = Constants.PATH_PREFIX+"/photos/thumb";


    //이미지 저장
    private void saveFile(MultipartFile file, Long AlbumId, String fileName) {
        try{
            String filepath = AlbumId + "/" + fileName;
            Files.copy(file.getInputStream(), Paths.get(original_path + "/" + filepath));

            //Scalr 라이브러리를 사용해서 오리지날 이미지를 최대 300 x 300 으로 resize
            BufferedImage thumbImg = Scalr.resize(ImageIO.read(file.getInputStream()), Constants.THUMB_SIZE, Constants.THUMB_SIZE);

            //resize 된 썸네일 이미지 저장
            File thumbFile = new File(thumb_path + "/" + filepath);
            String ext = StringUtils.getFilenameExtension(fileName);
            if (ext == null) {
                throw new IllegalArgumentException("No Extention");
            }
            ImageIO.write(thumbImg, ext, thumbFile);
        }catch (Exception e){
            throw new RuntimeException("Could not store the file.Error: " + e.getMessage());
        }
    }
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

    public PhotoDto savePhoto (MultipartFile file,Long albumId){
        //앨범 아이디가 존재하는지 확인
        Optional<Album> res = albumRepository.findById(albumId);

        if(res.isEmpty()){
            throw new EntityNotFoundException("앨범이 존재하지 않습니다");

        }
        //파일명, 파일용량 추출
        String fileName = file.getOriginalFilename();
        int fileSize = (int)file.getSize();
        fileName = getNextFileName(fileName, albumId);
        saveFile(file,albumId,fileName);

        //DB에 사진 레코드 생성 & 생성된 앨범 DTO 반환
        Photo photo = new Photo();
        photo.setOriginalUrl("/photos/original/" + albumId + "/" + fileName);
        photo.setThumbUrl("/photos/thumb/" + albumId + "/" + fileName);
        photo.setFileName(fileName);
        photo.setFileSize(fileSize);
        photo.setAlbum(res.get());
        Photo createPhoto = photoRepository.save(photo);

        return PhotoMapper.convertToDto(createPhoto);
    }


    private String getNextFileName(String fileName, Long albumId){
        //DB에 입력된 앨범안에 같은 파일명이 있는지 체크

        String fileNameNoEXT = StringUtils.stripFilenameExtension(fileName);
        String  ext = StringUtils.getFilenameExtension(fileName);

        Optional<Photo> res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);


        //파일이 존재하면 파일에 +1을 해서 저장

        int count =2;
        //파일명 조회해서 체크 -> 파일명 숫자 추가 및 변경 -> 없을때 까지 반복
        while(res.isPresent()){
            fileName = String.format("%s (%d).%s", fileNameNoEXT, count, ext);// string.format 메서드를 통해 (확장자 없는 파일명, 숫자, 확장자) 합침
            res = photoRepository.findByFileNameAndAlbum_AlbumId(fileName, albumId);
            count++;
            //while 문 끝나면 파일명 환반환

        }

        return fileName;


    }

    public  File getImageFile(Long photoId){
        Optional<Photo> res = photoRepository.findById(photoId);
        if(res.isEmpty()){
            throw new EntityNotFoundException(String.format("사진 ID %d 를 찾을 수 없습니다."));

        }
        return new File(Constants.PATH_PREFIX + res.get().getOriginalUrl());
    }

}
