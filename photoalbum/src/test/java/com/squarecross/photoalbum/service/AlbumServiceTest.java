package com.squarecross.photoalbum.service;

import com.squarecross.photoalbum.Constants;
import com.squarecross.photoalbum.domain.Photo;
import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.mapper.AlbumMapper;
import com.squarecross.photoalbum.repository.PhotoRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AlbumServiceTest {

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    PhotoRepository photoRepository;
    @Autowired
    AlbumService albumService;

    @Test
    void getAlbum() {
        Album album = new Album();
        album.setAlbumName("테스트");
        Album savedAlbum = albumRepository.save(album);

        AlbumDto resAlbum = albumService.getAlbum(savedAlbum.getAlbumId());
        assertEquals("테스트", resAlbum.getAlbumName());
    }

    @Test
    void testPhotoCount(){

        Album album = new Album();
        album.setAlbumName("테스트1");
        Album savedAlbum = albumRepository.save(album);

        Photo photo1 = new Photo();
        photo1.setFileName("사진1");
        photo1.setAlbum(savedAlbum);
        photoRepository.save(photo1);

        Photo photo2 = new Photo();
        photo2.setFileName("사진2");
        photo2.setAlbum(savedAlbum);
        photoRepository.save(photo2);


        AlbumDto result = albumService.getAlbum(savedAlbum.getAlbumId());

        Assertions.assertThat(result.getCount()).isEqualTo(2);



    }

    @Test
    void testAlbumCreate() throws IOException {
        //given
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("string");

        //when
        AlbumDto resDto = albumService.createAlbum(albumDto);


        //then
        Assertions.assertThat(albumDto.getAlbumName()).isEqualTo("string");
        Assertions.assertThat(resDto.getAlbumId()).isNotNull();
        Assertions.assertThat(resDto.getCreatedAt()).isNotNull();


        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + resDto.getAlbumId()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + resDto.getAlbumId()));
    }


    @Test
    void testAlbumRepository() throws InterruptedException {
        Album album1 = new Album();
        Album album2 = new Album();
        album1.setAlbumName("aaaa");
        album2.setAlbumName("aaab");

        albumRepository.save(album1);
        TimeUnit.SECONDS.sleep(1); //시간차를 벌리기위해 두번째 앨범 생성 1초 딜레이
        albumRepository.save(album2);

        //최신순 정렬, 두번째로 생성한 앨범이 먼저 나와야합니다
        List<Album> resDate1 = albumRepository.findByAlbumNameContainingOrderByCreatedAtDesc("aaa");
        assertEquals("aaab", resDate1.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals("aaaa", resDate1.get(1).getAlbumName()); // 1번째 Index가 첫번째 앨범명 aaaa 인지 체크
        assertEquals(2, resDate1.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크

        //생성순 정렬, 첫번째로 생성한 앨범이 먼저 나와야합니다.
        List<Album> resDate2 = albumRepository.findByAlbumNameContainingOrderByCreatedAtAsc("aaa");
        assertEquals("aaaa", resDate2.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals("aaab", resDate2.get(1).getAlbumName()); // 1번째 Index가 첫번째 앨범명 aaab 인지 체크
        assertEquals(2, resDate2.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크

        //앨범명 정렬, aaaa -> aaab 기준으로 나와야합니다
        List<Album> resName1 = albumRepository.findByAlbumNameContainingOrderByAlbumNameAsc("aaa");
        assertEquals("aaaa", resName1.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals("aaab", resName1.get(1).getAlbumName()); // 1번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals(2, resName1.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크

        //앨범명 정렬, aaab -> aaaa 기준으로 나와야합니다
        List<Album> resName2 = albumRepository.findByAlbumNameContainingOrderByAlbumNameDesc("aaa");
        assertEquals("aaab", resName2.get(0).getAlbumName()); // 0번째 Index가 두번째 앨범명 aaaa 인지 체크
        assertEquals("aaaa", resName2.get(1).getAlbumName()); // 1번째 Index가 두번째 앨범명 aaab 인지 체크
        assertEquals(2, resName2.size()); // aaa 이름을 가진 다른 앨범이 없다는 가정하에, 검색 키워드에 해당하는 앨범 필터링 체크
    }

    @Test
    void testChangeAlbumName_my () throws IOException{
        //given
        Album album = new Album();
        album.setAlbumName("바꿔봐");
        Album savedalbum = albumRepository.save(album);

        //when
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("바꿨다");

        AlbumDto resalbumDto = albumService.changeName(savedalbum.getAlbumId(), albumDto);

        //then
        Assertions.assertThat(resalbumDto.getAlbumName()).isEqualTo("바꿨다");
    }

    @Test
    void testChangeAlbumName() throws IOException {
        //앨범 생성
        AlbumDto albumDto = new AlbumDto();
        albumDto.setAlbumName("변경전");
        AlbumDto res = albumService.createAlbum(albumDto);

        Long albumId = res.getAlbumId(); // 생성된 앨범 아이디 추출
        AlbumDto updateDto = new AlbumDto();
        updateDto.setAlbumName("변경후"); // 업데이트용 Dto 생성
        albumService.changeName(albumId, updateDto);

        AlbumDto updatedDto = albumService.getAlbum(albumId);

        //앨범명 변경되었는지 확인
        assertEquals("변경후", updatedDto.getAlbumName());

        //앨범 삭제
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/original/" + res.getAlbumId()));
        Files.deleteIfExists(Paths.get(Constants.PATH_PREFIX + "/photos/thumb/" + res.getAlbumId()));
    }


    @Test
    void testDeleteAlbum() throws IOException {
        //given
        Long albumId = 7L;

        //when
        albumService.deleteAlbum(albumId);

        //then
        List<Photo> deletePhotos = photoRepository.findByAlbum_AlbumId(albumId);

        for(Photo photo : deletePhotos){
            assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+photo.getOriginalUrl())));
            assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+photo.getThumbUrl())));
        }

        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/original/"+albumId)));
        assertFalse(Files.exists(Paths.get(Constants.PATH_PREFIX+"/photos/thumb/"+albumId)));

        assertEquals(Optional.empty(), albumRepository.findById(albumId));




    }



}