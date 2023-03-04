package com.backend.waste.repository;

import com.backend.member.domain.Member;
import com.backend.waste.domain.Waste;
import com.backend.waste.dto.response.GetWasteBrief;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WasteJpaRepository extends JpaRepository<Waste, Long> {

    Optional<List<Waste>> findAllByMemberOrderByEnrolledDateDesc(Member member);

}
