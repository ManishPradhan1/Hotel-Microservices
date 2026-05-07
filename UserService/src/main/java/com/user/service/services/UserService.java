package com.user.service.services;

import com.user.service.entities.User;

import java.util.List;

public interface UserService {

    //User operations

    //create
    User saveUser(User user);

    //get all user
    List<User> getAllUser();

    //get single user of given userId
    User getUser(String userId);

}
