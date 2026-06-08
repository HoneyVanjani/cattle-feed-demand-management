package com.amul.cattlefeed.repository;

import com.amul.cattlefeed.entity.CattleFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CattleFeedRepository extends JpaRepository<CattleFeed, Long> {


}
