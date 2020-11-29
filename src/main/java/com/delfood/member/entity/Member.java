package com.delfood.member.entity;


import com.delfood.dto.address.AddressDTO;
import com.delfood.dto.member.MemberDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Entity
@Table(name = "member_tb")
public class Member extends BaseEntity {
    @NonNull
    private String name;

    private String mail;

    private String password;

    @NonNull
    private String tel;

    private MemberDTO.Status status;

    @Nullable
    private String addressCode;
    // 상세 주소(xxx호)
    private String addressDetail;

    private AddressDTO addressInfo;

    public Member(UUID uuid, LocalDateTime createdAt, LocalDateTime updatedAt) {
        super(uuid, createdAt, updatedAt);
    }

    public Member(UUID uuid) {
        super(uuid);
    }

    @Builder
    public Member(
            UUID uuid,
            String name,
            String mail,
            String password,
            String tel,
            MemberDTO.Status status,
            String addressCode, // TODO-Q : Address DTO와 합칠까?
            String addressDetail,  // TODO-Q : Address DTO와 합칠까?
            AddressDTO addressInfo
    ) {
        super(uuid);
        this.name = name;
        this.mail = mail;
        this.password = password;
        this.tel = tel;
        this.status = status;
        this.addressCode = addressCode;
        this.addressDetail = addressDetail;
        this.addressInfo = addressInfo;
    }

    public String toString(){
        return "uuid: " + super.getUuid(); // TODO-[search] : String variable interpolation
    }
}