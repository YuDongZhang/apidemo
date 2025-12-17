package com.example.apidemo.controller;

import com.example.apidemo.common.Result;
import com.example.apidemo.entity.Video;
import com.example.apidemo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频控制器
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    /**
     * 获取所有视频
     */
    @GetMapping
    public Result<List<Video>> list(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return Result.success(videoService.findByCategoryId(categoryId));
        }
        return Result.success(videoService.findAll());
    }

    /**
     * 获取单个视频
     */
    @GetMapping("/{id}")
    public Result<Video> get(@PathVariable Long id) {
        return Result.success(videoService.findById(id));
    }

    /**
     * 创建视频
     */
    @PostMapping
    public Result<Video> create(@RequestBody Video video) {
        return Result.success(videoService.create(video));
    }

    /**
     * 更新视频
     */
    @PutMapping("/{id}")
    public Result<Video> update(@PathVariable Long id, @RequestBody Video video) {
        return Result.success(videoService.update(id, video));
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.delete(id);
        return Result.success();
    }
}
