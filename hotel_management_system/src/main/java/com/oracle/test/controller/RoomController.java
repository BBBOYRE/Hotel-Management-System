package com.oracle.test.controller;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.common.PageResult;
import com.oracle.test.common.Result;
import com.oracle.test.entity.Room;
import com.oracle.test.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired private RoomService service;

    @GetMapping
    public Result<PageResult<Room>> page(@RequestParam(required = false) Long typeId,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) Integer floorNum,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String orderBy,
                                         @RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(service.page(typeId, status, floorNum, keyword, orderBy, pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Room>> listAll() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/available")
    public Result<List<Room>> available(@RequestParam(required = false) Long typeId) {
        return Result.ok(service.listAvailable(typeId));
    }

    @GetMapping("/{id}")
    public Result<Room> get(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @PostMapping
    public Result<Long> create(@RequestBody Room room, LoginUser current) {
        current.require("room:edit");
        return Result.ok(service.create(room, current.getUserId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Room room, LoginUser current) {
        current.require("room:edit");
        room.setRoomId(id);
        service.update(room, current.getUserId());
        return Result.ok();
    }

    @PostMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @RequestBody Map<String, Integer> body,
                                     LoginUser current) {
        current.require("room:edit");
        service.updateStatus(id, body.get("status"), current.getUserId());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, LoginUser current) {
        current.require("room:delete");
        service.delete(id, current.getUserId());
        return Result.ok();
    }

    /** 房间封面图：返回原始字节流；无图返回 404 让前端切到占位 */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Map<String, Object> img = service.getImage(id);
        if (img == null) return ResponseEntity.notFound().build();
        byte[] data = (byte[]) img.get("image");
        String type = (String) img.get("imageType");
        if (type == null || type.isEmpty()) type = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, type)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=300")
                .body(data);
    }

    /** 上传/替换房间封面图：multipart/form-data，字段名 file */
    @PostMapping("/{id}/image")
    public Result<Void> uploadImage(@PathVariable Long id,
                                    @RequestParam("file") MultipartFile file,
                                    LoginUser current) throws IOException {
        current.require("room:edit");
        if (file == null || file.isEmpty()) throw new BusinessException("未选择文件");
        service.saveImage(id, file.getBytes(), file.getContentType(),
                file.getOriginalFilename(), current.getUserId());
        return Result.ok();
    }

    /** 清除房间封面图 */
    @DeleteMapping("/{id}/image")
    public Result<Void> clearImage(@PathVariable Long id, LoginUser current) {
        current.require("room:edit");
        service.clearImage(id, current.getUserId());
        return Result.ok();
    }
}
