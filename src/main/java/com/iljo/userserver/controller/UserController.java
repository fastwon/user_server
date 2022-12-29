package com.iljo.userserver.controller;

import com.iljo.userserver.dto.UserDto;
import com.iljo.userserver.service.UserService;
import com.iljo.userserver.vo.RequestUser;
import com.iljo.userserver.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/")
    public ResponseEntity<ResponseUser> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("userId") String userId){
        ModelMapper mapper = new ModelMapper();

        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser returnValue = mapper.map(userDto ,ResponseUser.class);


        return ResponseEntity.status(HttpStatus.OK).body(returnValue);

    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable("userId") String userId){
        userService.deleteUserByUserId(userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("삭제");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ResponseUser> updateUser(@PathVariable("userId") String userId, @RequestBody RequestUser user){

        ModelMapper mapper = new ModelMapper();

        UserDto userDto = mapper.map(user, UserDto.class);
        userService.updateUserByUserId(userId, userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }


    @PostMapping("/login")
    public String login(@RequestBody RequestUser user, String userId, String password){



        String result = userService.login(userId, password);

        return result;
    }



}
