package com.delfood.member.repository;

import com.delfood.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUuid(UUID uuid);
    Optional<Member> findByMail(String mail);
    boolean existsByMail(String mail);
}
