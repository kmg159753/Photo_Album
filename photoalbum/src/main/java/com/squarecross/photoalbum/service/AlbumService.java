package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.AlbumRepository;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlbumService {
    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private PhotoRepository photoRepository;

    public AlbumDto getAlbum(Long albumId){
        Optional<Album> res = albumRepository.findById(albumId);

        if (res.isPresent()){
            AlbumDto albumDto = AlbumMapper.convertToDto(res.get());
            albumDto.setCount(photoRepository.countByAlbum_AlbumId(albumId));
            return albumDto;
        } else {
            throw new EntityNotFoundException(String.format("앨범 아이디 %d로 조회되지 않았습니다", albumId));
        }


    }

    public AlbumDto createAlbum(AlbumDto albumDto) throws IOException {
        Album album = AlbumMapper.convertToModel(albumDto);
        this.albumRepository.save(album);
        this.createAlbumDirectories(album);
        return AlbumMapper.convertToDto(album);

    }

    private  void createAlbumDirectories (Album album) throws IOException{
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + album.getAlbumId()));
        Files.createDirectories(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + album.getAlbumId()));
    }
    public AlbumDto getAlbumByName(String name){
        Optional<Album> res = albumRepository.findByAlbumName(name);

        Long albumId = res.get().getAlbumId();
        if (res.isPresent()){
            AlbumDto albumDto = AlbumMapper.convertToDto(res.get());

            albumDto.setCount(photoRepository.countByAlbum_AlbumId(albumId));
            return albumDto;
        } else {
            throw new EntityNotFoundException(String.format("앨범 아이디 %d로 조회되지 않았습니다", albumId ));
        }


    }

    public List<AlbumDto> getAlbumList(String keyword, String sort, String orderBy){
        List<Album> albums;

        if(Objects.equals(sort,"byName") ){
            if(Objects.equals(orderBy,"Desc")) {
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameDesc(keyword);
            }else {
                albums = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc(keyword);
            }

        }
        else if(Objects.equals(sort,"byDate") ){
            if(Objects.equals(orderBy,"Asc")  ) {
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtAsc(keyword);
            }
            else {
                albums = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc(keyword);
            }
        }
        else {
            throw new IllegalStateException("알 수 없는 정렬 기준입니다.");
        }

        List<AlbumDto> albumDtos = AlbumMapper.convertToDtoList(albums);

        for(AlbumDto albumDto : albumDtos){
            List<Photo> top4 = photoRepository.findTop4ByAlbum_AlbumIdOrderByUploadAtDesc(albumDto.getAlbumId());
            albumDto.setThumbUrls(top4.stream().map(Photo::getThumbUrl).map(c->Constants.PATH_PREFIX+c).collect(Collectors.toList()));
        }

        return  albumDtos;

    }

    public AlbumDto changeName(Long AlbumId, AlbumDto albumDto){
        Optional<Album> album = this.albumRepository.findById(AlbumId);
        if(album.isEmpty()){
            //throw new NoSuchFieldException( String.format("Album ID '%d'가 존재하지 않습니다.", AlbumId) ) ;
            throw new NoSuchElementException(String.format("Album ID '%d'가 존재하지 않습니다", AlbumId));

        }

        Album updateAlbum = album.get();
        updateAlbum.setAlbumName(albumDto.getAlbumName());
        Album savedAlbum = this.albumRepository.save(updateAlbum);
        return AlbumMapper.convertToDto(savedAlbum);

    }

    public void deleteAlbum(Long AlbumId) throws IOException {
        Optional<Album> album = this.albumRepository.findById(AlbumId);
        if(album.isEmpty()){
            throw new NoSuchFileException(String.format("삭제하려는 Album ID '%d'가 존재하지 않습니다", AlbumId));
        }
        Album targetalbum = album.get();


        /*for(Photo photo: targetalbum.getPhotos()){
            Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/original/"  + photo.getOriginalUrl()));
            Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/"     + photo.getThumbUrl()));

        }*/

        // 사진 삭제
        FileUtils.cleanDirectory(new File(Constants.PATH_PREFIX + "/photos/original/" + targetalbum.getAlbumId()));

        FileUtils.cleanDirectory(new File(Constants.PATH_PREFIX + "/photos/thumb/" + targetalbum.getAlbumId()));

        //앨범 삭제

        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + targetalbum.getAlbumId()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + targetalbum.getAlbumId()));


        //db에서 앨범삭제

        albumRepository.deleteById(targetalbum.getAlbumId());
    }
}
