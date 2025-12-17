package com.example.apidemo.repository;

import com.example.apidemo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视频数据访问层
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByCategoryIdOrderByPublishTimeDesc(Long categoryId);

    List<Video> findAllByOrderByPublishTimeDesc();
}
