package com.squarecross.photoalbum.mapper;


import com.squarecross.photoalbum.domain.User;

import com.squarecross.photoalbum.dto.UserDto;

public class UserMapper {

    public static UserDto converToDto(User user){

        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setUserEmail(user.getUserEmail());
        userDto.setUserName(user.getUserName());
        userDto.setCreatedAt(user.getCreatedAt());
        userDto.setLoginAt(user.getLoginAt());


        return userDto;
    }
}
