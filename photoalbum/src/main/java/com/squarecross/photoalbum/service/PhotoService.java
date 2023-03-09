package com.squarecross.photoalbum.service;


import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.mapper.PhotoMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<PhotoDto> getPhotoList(Long albumId,String keyword, String sort, String orderBy){
        List<Photo> photos= photoRepository.findByAlbum_AlbumId(albumId);

        if(Objects.equals(sort,"byName") ){
            if(Objects.equals(orderBy,"Desc")) {
                photos = photoRepository.findByFileNameContainingOrderByFileNameDesc(keyword);
            }else {
                photos = photoRepository.findByFileNameContainingOrderByFileNameAsc(keyword);
            }

        }
        else if(Objects.equals(sort,"byDate") ){
            if(Objects.equals(orderBy,"Asc")  ) {
                photos = photoRepository.findByFileNameContainingOrderByUploadAtAsc(keyword);
            }
            else {
                photos = photoRepository.findByFileNameContainingOrderByUploadAtDesc(keyword);
            }
        }
        else {
            throw new IllegalStateException("알 수 없는 정렬 기준입니다.");
        }

        List<PhotoDto> photoDtos = PhotoMapper.convertToDtoList(photos);


        return  photoDtos;

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

    public PhotoDto changeAlbum(Long photoId, Long fromAlbumId, Long toAlbumId) throws IOException {
        Optional<Photo> optionalPhoto = this.photoRepository.findById(photoId);
        Optional<Album> optionalFromAlbum = this.albumRepository.findById(fromAlbumId);
        Optional<Album> optionalToAlbum = this.albumRepository.findById(toAlbumId);

        if(optionalPhoto.isEmpty()){
            throw new NoSuchElementException(String.format("Photo ID '%d'가 존재하지 않습니다", photoId));
        }else if(optionalFromAlbum.isEmpty()){
            throw new NoSuchElementException(String.format("Album ID '%d'가 존재하지 않습니다", fromAlbumId));
        }else if(optionalToAlbum.isEmpty()){
            throw new NoSuchElementException(String.format("Album ID '%d'가 존재하지 않습니다", toAlbumId));
        }else {
            Photo photo = optionalPhoto.get();
            String photoOrlginalUrl = photo.getOriginalUrl();
            String photoThumbUrl = photo.getThumbUrl();
            Album FromAlbum = optionalFromAlbum.get();
            Album ToAlbum = optionalToAlbum.get();


            if (!Objects.equals(photo.getAlbum().getAlbumId(), FromAlbum.getAlbumId())) {
                throw new IllegalArgumentException("사진이 해당 앨범안에 속해있지 않습니다. ");
            }
            if (Objects.equals(photo.getAlbum().getAlbumId(), ToAlbum.getAlbumId())) {
                throw new IllegalArgumentException("사진이 이미 앨범에 속해있습니다. ");
            }

            //DB에서 사진의 앨범 변경
            Photo updatePhoto = photo;
            updatePhoto.setAlbum(ToAlbum);
            Photo savedPhoto = this.photoRepository.save(updatePhoto);

            //디렉토리 내에서 파일의 앨범 이동

            Files.move(Paths.get(Constants.PATH_PREFIX + photoOrlginalUrl), Paths.get(Constants.PATH_PREFIX + "/photos/original/" + ToAlbum.getAlbumId() + "/" + photo.getFileName()));
            Files.move(Paths.get(Constants.PATH_PREFIX + photoThumbUrl), Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + ToAlbum.getAlbumId() + "/" + photo.getFileName()));


            /*Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + photoOrlginalUrl));
            Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + photoThumbUrl));*/


            /*File OriginaloldFile = new File(Constants.PATH_PREFIX + "/photos/original/" + FromAlbum.getAlbumId()+"/" +photo.getFileName() );
            File ThumboldFile = new File(Constants.PATH_PREFIX + "/photos/thumb/" + FromAlbum.getAlbumId()+"/" +photo.getFileName() );



            // 변경할 디렉토리와 파일

            File OriginalnewFile = new File(Constants.PATH_PREFIX + "/photos/original/" + ToAlbum.getAlbumId() +"/"+photo.getFileName());
            File ThumbnewFile = new File(Constants.PATH_PREFIX + "/photos/thumb/" + ToAlbum.getAlbumId() +"/"+photo.getFileName());




            // 파일 이동
            FileSystemUtils.copyRecursively(OriginaloldFile,OriginalnewFile);
            FileSystemUtils.copyRecursively(ThumboldFile,ThumbnewFile);

            //기존 파일 삭제
            OriginaloldFile.delete();
            ThumboldFile.delete();*/



            /*Files.deleteIfExists(Paths.get(OriginaloldFile.toURI()));
            Files.deleteIfExists(Paths.get(ThumboldFile));*/


            return PhotoMapper.convertToDto(savedPhoto);
        }

    }






    public void deletePhoto(Long photoId) throws IOException {
        Optional<Photo> Optionalphoto = photoRepository.findById(photoId);

        if(Optionalphoto.isEmpty()){
            throw new NoSuchElementException(String.format("Photo Id '%d'가 존재하지 않습니다", photoId));
        }
        Photo photo = Optionalphoto.get();

        photoRepository.deleteById(photoId);

        //파일 삭제
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+photo.getThumbUrl()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX+photo.getOriginalUrl()));

    }



}
