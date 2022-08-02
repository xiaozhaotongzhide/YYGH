package com.example.yygh.chat.repository;

import com.example.yygh.model.chat.MsgEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MsgRepository extends MongoRepository<MsgEntity,String> {
}
