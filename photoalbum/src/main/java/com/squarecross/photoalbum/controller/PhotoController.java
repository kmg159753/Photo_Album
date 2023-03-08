package com.squarecross.photoalbum.controller;

import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.PhotoDto;
import com.squarecross.photoalbum.service.AlbumService;
import com.squarecross.photoalbum.service.PhotoService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RestController
@RequestMapping("/albums/{albumId}/photos")
public class PhotoController {
    @Autowired
    AlbumService albumService;
    @Autowired
    PhotoService photoService;


    //사진 상세정보 API
    @RequestMapping(value = "/{photoId}", method = RequestMethod.GET)
    public ResponseEntity<PhotoDto> getPhoto(@PathVariable("photoId") final  Long photoId){
        PhotoDto photo = photoService.getPhoto(photoId);
        return new ResponseEntity<>(photo, HttpStatus.OK);
    }

    //사진 업로드 API
    @RequestMapping(value = "", method = RequestMethod.POST)
    public  ResponseEntity<List<PhotoDto>> uploadPhotos(@PathVariable("albumId") final  Long albumId,
                                                        @RequestParam("photos")MultipartFile[] files){
        List<PhotoDto> photos = new ArrayList<>();
        for(MultipartFile file : files){
            PhotoDto photoDto = photoService.savePhoto(file,albumId);
            photos.add(photoDto);
        }
        return new ResponseEntity<>(photos, HttpStatus.OK);
    }

    //사진 다운로드 API
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public void downloadPhotos(@RequestParam("photoIds") Long[] photoIds, HttpServletResponse response) {

        try{



            if (photoIds.length == 1) {
                // 사진이 1장이라면
                File file = photoService.getImageFile(photoIds[0]);
                OutputStream outputStream = response.getOutputStream();
                IOUtils.copy(new FileInputStream(file), outputStream);
                outputStream.close();
            } else {
                List<File> photoFiles = new ArrayList<>(); // 파일들을 저장할 List<File> 객체를 생성합니다.


                // 압축된 데이터를 담을 임시 파일을 생성합니다.
                File zipFile = File.createTempFile("photos", ".zip");
                FileOutputStream zipFileOutputStream = new FileOutputStream(zipFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(zipFileOutputStream);

                // 각 사진 파일을 zip 파일에 추가합니다.
                for (Long photoId : photoIds) {
                    File photoFile = photoService.getImageFile(photoId);
                    FileInputStream photoInputStream = new FileInputStream(photoFile);
                    ZipEntry photoEntry = new ZipEntry(photoFile.getName());
                    zipOutputStream.putNextEntry(photoEntry);
                    IOUtils.copy(photoInputStream, zipOutputStream);
                    zipOutputStream.closeEntry();
                    photoInputStream.close();
                }

                // zip 파일 스트림을 닫습니다.
                zipOutputStream.finish();
                zipOutputStream.close();
                zipFileOutputStream.close();

                // 응답 헤더를 설정하여 zip 파일을 반환할 것임을 알립니다.
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"photos.zip\"");

                // zip 파일을 응답 출력 스트림으로 복사합니다.
                OutputStream responseOutputStream = response.getOutputStream();
                IOUtils.copy(new FileInputStream(zipFile), responseOutputStream);
                responseOutputStream.flush();
                responseOutputStream.close();

                // 임시 zip 파일을 삭제합니다.
                zipFile.delete();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*@RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<AlbumDto>>
    getAlbumList(@PathVariable(value = "albumId") final Long albumId,
                 @RequestParam(value = "keyword", required = false, defaultValue = "") final String keyword,
                 @RequestParam(value = "sort", required = false,defaultValue = "byDate") final String sort,
                 @RequestParam(value = "orderBy",required = false, defaultValue = "") final String orderBy) {
        List<PhotoDto> PhotoDtos = albumService.getPhotoList(albumId,keyword,sort,orderBy);
        return new ResponseEntity<>(PhotoDtos, HttpStatus.OK);
    }*/


}

