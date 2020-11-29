package com.delfood.member.mapper;

import com.delfood.dto.member.MemberDTO;
import com.delfood.member.entity.Member;

import java.util.UUID;

public class MemberEntityFactory {

    public static Member of(MemberDTO dto){
        return Member.builder()
                .uuid(UUID.randomUUID())
                .password(dto.getEncodedPassword()) // TODO - Fix : 인터페이스 및 파라미터로 주입성 받게끔 구현
                .name(dto.getName())
                .status(dto.getStatus())
                .addressCode(dto.getAddressCode())
                .addressDetail(dto.getAddressDetail())
                .addressInfo(dto.getAddressInfo())
                .build();
    }
}
