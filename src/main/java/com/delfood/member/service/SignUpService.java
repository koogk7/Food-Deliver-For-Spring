package com.delfood.member.service;


//import com.delfood.dto.member.MemberDTO;
//import com.delfood.member.entity.Member;
//import com.delfood.member.mapper.MemberEntityFactory;
//import com.delfood.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class SignUpService {
//    final private MemberRepository repo;
//
//    public SignUpService(MemberRepository repo) {
//        this.repo = repo;
//    }
//
//    public void insertMember(MemberDTO memberInfo) {
//        Member entity = MemberEntityFactory.of(memberInfo);
//        this.validateMailDuplication(memberInfo.getMail());
//        repo.save(entity);
//    }
//
//    public void validateMailDuplication(String mail) {
//        if (!this.repo.existsByMail(mail)) {
//            return;
//        }
//
//        throw new RuntimeException("duplicate member uuid"); // TODO - I : 예외 정의하기
//    }
}
