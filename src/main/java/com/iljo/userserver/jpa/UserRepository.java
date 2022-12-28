package com.iljo.userserver.jpa;

import org.springframework.data.repository.CrudRepository;

/**
 *  CRUD를 하기위한 interface
 * */
public interface UserRepository extends CrudRepository<UserEntity, String> {
    UserEntity findByEmail(String email);
}
