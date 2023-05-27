package com.squarecross.photoalbum.mapper;


import com.squarecross.photoalbum.domain.Album;
import com.squarecross.photoalbum.domain.User;

import com.squarecross.photoalbum.dto.AlbumDto;
import com.squarecross.photoalbum.dto.UserDto;

import com.squarecross.photoalbum.mapper.AlbumMapper;

import java.util.ArrayList;
import java.util.List;


public class UserMapper {



    public static UserDto convertToDto(User user){

        AlbumMapper albumMapper = new AlbumMapper();

        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUserEmail(user.getUserEmail());
        userDto.setUserName(user.getUserName());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setLoginAt(user.getLoginAt());

        List<AlbumDto> albumDTOs = new ArrayList<>();

        if (user.getAlbums() != null) {
            for (Album album : user.getAlbums()) {
                albumDTOs.add(albumMapper.convertToDto(album));
            }
        }

        userDto.setAlbums(albumDTOs);

        return userDto;
    }
}
