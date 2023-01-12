package com.iljo.userserver.controller;

import com.iljo.userserver.dto.UserDto;
import com.iljo.userserver.feign.RoomClient;
import com.iljo.userserver.jpa.*;
import com.iljo.userserver.service.*;
import com.iljo.userserver.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin("http://localhost:3000")
//@CrossOrigin(origins = "127.0.0.1:8808")
public class UserController {

    UserService userService;
    RoomClient roomClient;
    EnterRepository enterRepository;
    EnterService enterService;
    FavoriteService favoriteService;
    FollowService followService;
    RoomTagService roomTagService;

    @Autowired
    public UserController(UserService userService, RoomClient roomClient, EnterRepository enterRepository, EnterService enterService, FavoriteService favoriteService, FollowService followService, RoomTagService roomTagService) {
        this.userService = userService;
        this.roomClient = roomClient;
        this.enterRepository = enterRepository;
        this.enterService = enterService;
        this.favoriteService = favoriteService;
        this.followService = followService;
        this.roomTagService = roomTagService;
    }

    /**
     * 회원가입을 위한 controller
     * */
    @PostMapping("/")
    @Transactional
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
//        System.out.println(user instanceof Object);
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        UserDto userDto1 = userService.createUser(userDto);
        ResponseUser responseUser = mapper.map(userDto1, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }


    /**
     * userId에 해당하는 회원정보를 불러오기 위한 controller
     * */
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){
        ModelMapper mapper = new ModelMapper();

        // enter에서 roomId 가져오기
        List<EnterEntity> enterEntityList = enterRepository.findAllByUserId(userId);

        List<ResponseRoomId> roomIdList = new ArrayList<>();
        enterEntityList.forEach(r -> {
            ResponseRoomId room = roomClient.getRooms(r.getRoomId());
            roomIdList.add(room);
        });

        List<User_FavoriteEntity> userFavoriteEntities = favoriteService.getFavoriteByUserId(userId);
        List<User_Follow_TagEntity> userFollowTagEntities = followService.getFollowByUserId(userId);
        List<User_Room_TagEntity> userRoomTagEntities = roomTagService.getRoomTagByUserId(userId);

        List<ResponseFavorite> responseFavorites = new ArrayList<>();
        List<ResponseFollow> responseFollows = new ArrayList<>();
        List<ResponseRoomTag> responseRoomTags = new ArrayList<>();

        userFavoriteEntities.forEach(v -> {
            responseFavorites.add(new ModelMapper().map(v, ResponseFavorite.class));
        });

        userFollowTagEntities.forEach(f -> {
            responseFollows.add(new ModelMapper().map(f, ResponseFollow.class));
        });

        userRoomTagEntities.forEach(r -> {
            responseRoomTags.add(new ModelMapper().map(r, ResponseRoomTag.class));
        });

        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser returnValue = mapper.map(userDto ,ResponseUser.class);
        returnValue.setRooms(roomIdList);
        returnValue.setUserFavorites(responseFavorites);
        returnValue.setUserFollows(responseFollows);
        returnValue.setUserRoomTags(responseRoomTags);

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);

    }

    // room에게 전달해줄 user
    @GetMapping("/{userId}/room")
    public ResponseEntity<ResponseUser> getUserForRoom(@PathVariable("userId") String userId){
        ModelMapper mapper = new ModelMapper();
        UserDto userDto = userService.getUserByUserId(userId);
        ResponseUser returnValue = mapper.map(userDto ,ResponseUser.class);
        return ResponseEntity.status(HttpStatus.OK).body(returnValue);

    }

    /**
     * 회원정보를 삭제하기 위한 controller
     * */
    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable("userId") String userId){
        userService.deleteUserByUserId(userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("삭제");
    }

    /**
     * 회원정보를 수정하기 위한 controller
     * */
    @PutMapping("/{userId}")
    @Transactional
    public ResponseEntity<ResponseUser> updateUser(@PathVariable("userId") String userId, @RequestBody RequestUser user){

        ModelMapper mapper = new ModelMapper();

        UserDto userDto = mapper.map(user, UserDto.class);
        // userService.updateUserByUserId(userId, userDto);

        ResponseUser responseUser = mapper.map(userService.updateUserByUserId(userId, userDto), ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }


    /**
     * 로그인을 위한 controller
     * */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<String> login(@RequestBody RequestUser user, String userId, String password){

        userId = user.getUserId();
        password = user.getEncryptedPwd();

        String result = userService.login(userId, password);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }



}
