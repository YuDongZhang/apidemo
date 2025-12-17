package com.example.apidemo.service;

import com.example.apidemo.common.BusinessException;
import com.example.apidemo.entity.Video;
import com.example.apidemo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 视频服务
 */
@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    /**
     * 获取所有视频
     */
    public List<Video> findAll() {
        return videoRepository.findAllByOrderByPublishTimeDesc();
    }

    /**
     * 根据栏目获取视频
     */
    public List<Video> findByCategoryId(Long categoryId) {
        return videoRepository.findByCategoryIdOrderByPublishTimeDesc(categoryId);
    }

    /**
     * 根据 ID 获取视频
     */
    public Video findById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("视频不存在"));
    }

    /**
     * 创建视频
     */
    public Video create(Video video) {
        return videoRepository.save(video);
    }

    /**
     * 更新视频
     */
    public Video update(Long id, Video videoData) {
        Video video = findById(id);
        video.setAuthor(videoData.getAuthor());
        video.setDescription(videoData.getDescription());
        video.setVideoUrl(videoData.getVideoUrl());
        video.setCoverUrl(videoData.getCoverUrl());
        video.setCategoryId(videoData.getCategoryId());
        if (videoData.getPublishTime() != null) {
            video.setPublishTime(videoData.getPublishTime());
        }
        return videoRepository.save(video);
    }

    /**
     * 删除视频
     */
    public void delete(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new BusinessException("视频不存在");
        }
        videoRepository.deleteById(id);
    }
}
