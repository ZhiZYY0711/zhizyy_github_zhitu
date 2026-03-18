package com.zhitu.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.core.result.Result;
import com.zhitu.system.entity.SysDict;
import com.zhitu.system.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/system/v1")
@RequiredArgsConstructor
public class DictController {

    private final SysDictMapper dictMapper;

    /**
     * 获取字典列表
     */
    @GetMapping("/dict/{category}")
    public Result<List<SysDict>> listByCategory(@PathVariable String category) {
        List<SysDict> list = dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>()
                        .eq(SysDict::getCategory, category)
                        .orderByAsc(SysDict::getSortOrder));
        return Result.ok(list);
    }

    /**
     * 新增标签
     */
    @PostMapping("/tags")
    public Result<SysDict> addTag(@RequestBody SysDict dict) {
        dict.setCreatedAt(LocalDateTime.now());
        dict.setUpdatedAt(LocalDateTime.now());
        dictMapper.insert(dict);
        return Result.ok(dict);
    }
}
